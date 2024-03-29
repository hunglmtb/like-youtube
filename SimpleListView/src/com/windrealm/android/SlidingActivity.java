package com.windrealm.android;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class SlidingActivity extends Activity {

	public enum OverlayMode {
		APP,
		HOME_SHOW
	}
	// 
	public static final int PADDING = 5;
	private static final int OVERLAY_HEIGHT = 210;
	private static final int OVERLAY_WIDTH = 280;
	private static final float DRAG_MOVE_RANGE = 3*OVERLAY_HEIGHT/4;
	private static final int MARGIN_TOP_OVERLAY = 150;
	private static final int OVERLAY_BOTTOM_MARGIN = 50;
	private static final long ANIMATION_DURATION = 400;
	private static final int MENU_WIDTH = 500;
	private static final float BACKVIEW_ALPHA_MAX = 0.8f;
	private static final int BACK_VIEW_WIDTH = 50;
	private static final int OVERLAY_MENU_MARGIN_LEFT = 20;
	protected static final int SWITCH_RANGE = 90;

	// Layout containers for various widgets

	protected boolean mOnTop = true;
	protected boolean mClosed = false;
	private boolean mCloseOnRight = false;
	private boolean mMenuHiden = true;
	private boolean mIsRootLayoutAnimating = false;
	protected OverlayMode mOverlayMode = OverlayMode.APP;

	private RelativeLayout 				mRootLayout;			// Root layout
	private RelativeLayout 				mAppLayout;			// Reference to the window
	private ListView mMainListView ;
	private ArrayAdapter<String> listAdapter ;
	private RelativeLayout.LayoutParams mRootRelativeLayoutParams;


	private RelativeLayout mSecondaryLayout;
	private ListView mSecondListView;
	private View mBackView;
	private RelativeLayout mMenuLayout;
	private ListView mMenuListView ;

	private OnTouchListener mTouchListener = new OnTouchListener() {

		private boolean mFirstTimeMove = false;
		private boolean mSlidingX = false;
		private int mStartDownX;
		private int mStartDownY;
		private int mSlideXDelata = 40;
		private int mLastXposition = mStartDownX;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mIsRootLayoutAnimating) {
				return true;
			}
			final int action = event.getActionMasked();
			int x = (int)event.getRawX();
			int y = (int)event.getRawY();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mMenuLayout.clearAnimation();
				mBackView.clearAnimation();
				mRootLayout.clearAnimation();
				mFirstTimeMove = true;
				mStartDownX = x;
				mStartDownY = y;
				mLastXposition = x;
				return true;
			case MotionEvent.ACTION_MOVE:
				// Calculate position of the whole tray according to the drag, and update layout.
				float deltaX = x-mStartDownX;
				float deltaY = y-mStartDownY;

				if (mFirstTimeMove ) {
					mSlidingX =  Math.abs(deltaY)<=Math.abs(deltaX);
					mFirstTimeMove = false;
					LayoutParams lparams = ((LayoutParams)mMenuLayout.getLayoutParams());
					mSlideXDelata = mStartDownX - (lparams.width + lparams.leftMargin);
					mStartDownX = x - mSlideXDelata;
				}

				if (mSlidingX) {
					int xPosition = x - mSlideXDelata;
					mLastXposition = xPosition;
					updateMenu(xPosition);
					return true;
				}
				else return false;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if(mSlidingX){
					boolean switched = Math.abs(mLastXposition-mStartDownX)>=SWITCH_RANGE&&((mMenuHiden&&mLastXposition>mStartDownX)||(!mMenuHiden&&mLastXposition<mStartDownX)); 
					boolean hiden = (mMenuHiden&&!switched)||(!mMenuHiden&&switched);
					animateMenu(mLastXposition,hiden);						
				}
				boolean result = mSlidingX;
				mSlidingX = false;
				return result;
			default:
				return false;
			}
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_sliding);

		mMainListView = (ListView) findViewById( R.id.mainListView );

		// Create and populate a List of planet names.
		String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
				"Jupiter", "Saturn", "Uranus", "Neptune", "Venus", "Mars", "Venus", "Neptune", "Neptune"};  
		ArrayList<String> planetList = new ArrayList<String>();
		planetList.addAll( Arrays.asList(planets) );

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);

		// Add more planets. If you passed a String[] instead of a List<String> 
		// into the ArrayAdapter constructor, you must not add more items. 
		// Otherwise an exception will occur.
		listAdapter.add( "Ceres" );
		listAdapter.add( "Pluto" );
		listAdapter.add( "Haumea" );
		listAdapter.add( "Makemake" );
		listAdapter.add( "Eris" );

		// Set the ArrayAdapter as the ListView's adapter.
		mMainListView.setAdapter( listAdapter );  

		mOverlayMode = OverlayMode.HOME_SHOW;

		// Get references to all the views and add them to root view as needed.
		mAppLayout = (RelativeLayout) findViewById(R.id.app_layout);
		mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);
		mSecondaryLayout = (RelativeLayout) findViewById(R.id.secondary_layout);
		mBackView = findViewById(R.id.backView);
		mMenuLayout = (RelativeLayout) findViewById(R.id.menu_layout);
		mMenuListView = (ListView) findViewById( R.id.menuListView );
		mMenuLayout.setVisibility(View.GONE);
		mMenuListView.setAdapter( listAdapter );  
		mRootRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) mRootLayout.getLayoutParams();

		mRootLayout.setOnTouchListener(new TrayTouchListener());


		mMenuListView.setOnTouchListener(mTouchListener);
		mBackView.setOnTouchListener(mTouchListener);
		

		mSecondListView = (ListView) findViewById( R.id.secondListView );
		mSecondListView.setAdapter( listAdapter );  

		setOriginalPosition();		 
	}



	protected void animateMenu(final int aLastXposition, final boolean aHiden) {		
		// secondary
		AnimationSet menuAnimations = new AnimationSet(false);
		menuAnimations.setFillAfter(true);
		menuAnimations.setDuration(ANIMATION_DURATION);

		//translate
		TranslateAnimation menuTranslate = new TranslateAnimation( 0 , 0 , 0,0){

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				int secondDeltaMargin  =aHiden?-MENU_WIDTH:MENU_WIDTH -aLastXposition ;
				int xPosition = (int) (secondDeltaMargin*interpolatedTime + aLastXposition);
				updateMenu(xPosition);
				Log.i("hung", "interpolatedTime   "+interpolatedTime+" margin "+xPosition);
				if (interpolatedTime==1) {
					mMenuLayout.clearAnimation();
					float alpha = aHiden?0:BACKVIEW_ALPHA_MAX;
					updateBackView(aHiden,OVERLAY_BOTTOM_MARGIN,alpha);
					mMenuHiden = aHiden;
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		menuAnimations.addAnimation(menuTranslate);
		mMenuHiden = false;
		mMenuLayout.startAnimation(menuAnimations);
	}

	protected void updateBackView(boolean hiden, int margin, float fromAlpha) {
		mBackView.setVisibility(View.VISIBLE);
		LayoutParams backViewParams = null;

		if (hiden) {
			//back view
			mBackView.clearAnimation();
			backViewParams = new RelativeLayout.LayoutParams(BACK_VIEW_WIDTH,RelativeLayout.LayoutParams.MATCH_PARENT);
		}
		else{
			//back view
			backViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			backViewParams.bottomMargin = -margin;
		}
		backViewParams.addRule(RelativeLayout.ALIGN_BOTTOM,R.id.root_layout);
		backViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		backViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		setAlphaValue(mBackView,fromAlpha);
		mBackView.setLayoutParams(backViewParams);
	}



	@SuppressLint("NewApi")
	private void setAlphaValue(View aView, float alpha) {
		alpha = Math.max(alpha, 0);
		alpha = Math.min(alpha, 1);

		if (Build.VERSION.SDK_INT < 11) {
			final AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
			long duration = 0;
			animation.setDuration(duration);
			animation.setFillAfter(true);
			aView.startAnimation(animation);
		}else{
			aView.setAlpha(alpha);
		}
	}



	@SuppressLint("NewApi")
	protected void updateMenu(int x) {
		int xPosition = Math.min(x, MENU_WIDTH);
		xPosition = Math.max(xPosition, 0);

		LayoutParams menuParams = new RelativeLayout.LayoutParams(MENU_WIDTH,RelativeLayout.LayoutParams.MATCH_PARENT);
		menuParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		menuParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		menuParams.leftMargin = xPosition>=MENU_WIDTH?0:xPosition - MENU_WIDTH;
		mMenuLayout.setLayoutParams(menuParams);
		mMenuLayout.setVisibility(View.VISIBLE);

		float fromAlpha = BACKVIEW_ALPHA_MAX*xPosition/(float)MENU_WIDTH;
		updateBackView(false,OVERLAY_BOTTOM_MARGIN,fromAlpha);
		updatePrimaryView(xPosition);
	}


	private void updatePrimaryView(int xPosition) {
		if (!mOnTop) {
			int screenWidth = mAppLayout.getWidth();
			if (screenWidth>0) {
				int xAxis = Math.max(xPosition+OVERLAY_MENU_MARGIN_LEFT, screenWidth-OVERLAY_WIDTH-OVERLAY_BOTTOM_MARGIN);
				updateViewLayout(false,true,xAxis,0);				
			}
		}
	}



	private void setOriginalPosition() {
		mOnTop = false;
		mClosed = false;
		int xAxis = mAppLayout.getWidth() - OVERLAY_WIDTH - OVERLAY_BOTTOM_MARGIN;
		updateViewLayout(false,true,xAxis,0);
		updateMenu(0);
	}



	// Listens to the touch events on the tray.
	private class TrayTouchListener implements OnTouchListener {
		private boolean mIsFirstTimeMove = false;

		// Variables that control drag
		private int mStartDragX;
		private int mStartDragY; // Unused as yet
		private int mPrevDragX;
		private int mPrevDragY;
		private boolean mIsSlidingX = true;
		private boolean mSlidingStart = false;

		private int mYAxis = 0;
		private int mXAxis = 0;
		private int mTopHeigh;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (mIsRootLayoutAnimating||(mRootLayout.getAnimation()!=null&&!mRootLayout.getAnimation().hasEnded())) {
				return false;
			}

			final int action = event.getActionMasked();
			int x = (int)event.getRawX();
			int y = (int)event.getRawY();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mMenuLayout.clearAnimation();
				mBackView.clearAnimation();
				mRootLayout.clearAnimation();
				
				mTopHeigh = mOnTop?mRootLayout.getHeight():OVERLAY_HEIGHT;
				mIsFirstTimeMove = true;
				mRootLayout.clearAnimation();
				// Store the start points
				mStartDragX = x;
				mStartDragY = y;
				mPrevDragX = x;
				mPrevDragY = y;
				break;
			case MotionEvent.ACTION_MOVE:
				// Calculate position of the whole tray according to the drag, and update layout.
				float deltaX = x-mPrevDragX;
				float deltaY = y-mPrevDragY;



				if (mOverlayMode==OverlayMode.APP) {
					deltaY = deltaY>=0?0:deltaY;				
				}
				if (mIsFirstTimeMove) {
					mIsSlidingX =  Math.abs(deltaY)<=Math.abs(deltaX);
					mIsFirstTimeMove = false;
				}

				if (!mMenuHiden) {
					return true;
				}

				if (mIsSlidingX) {  
					if (!mOnTop) {
						int slideXDelata = mStartDragX - (mAppLayout.getWidth() - OVERLAY_WIDTH -OVERLAY_BOTTOM_MARGIN);
						mXAxis = x - slideXDelata;
						int screenWidth = mAppLayout.getWidth();
						mXAxis = Math.min(mXAxis, screenWidth+OVERLAY_WIDTH);
						mXAxis = Math.max(mXAxis, -1*OVERLAY_WIDTH);
					}
					else return true;
				}
				else{
					int slidingMidleRangeY =mTopHeigh /3;
					int slidedRangeY = mTopHeigh/4;
					boolean aboveHalf = (mStartDragY<slidingMidleRangeY&&deltaY>slidedRangeY);
					boolean bottomHalf = ((mStartDragY>=slidingMidleRangeY)&&y>=(slidingMidleRangeY+slidedRangeY));
					mSlidingStart = mSlidingStart||
							((mOnTop&&deltaY>0&&(aboveHalf||bottomHalf )))||
							(!mOnTop&&deltaY<0&&y<(mAppLayout.getHeight() - mRootLayout.getHeight()/2));
					if(mSlidingStart){
						int correctDeltaY = mStartDragY<slidingMidleRangeY?3*mRootLayout.getHeight()/4:mRootLayout.getHeight();
						int slideYDelata = mOnTop?correctDeltaY:slidingMidleRangeY;
						mYAxis = y - slideYDelata;
						int screenHeight = mAppLayout.getHeight();
						mYAxis = Math.min(mYAxis, screenHeight-OVERLAY_HEIGHT -OVERLAY_BOTTOM_MARGIN);
						mYAxis = Math.max(mYAxis, 0);

					}
					else return true;

				}

				mPrevDragX = x;
				mPrevDragY = y;
				updateViewLayout(true,mIsSlidingX,mXAxis,mYAxis);
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mIsFirstTimeMove = true;
				mSlidingStart = false;
				if (mIsSlidingX&&mOnTop) return true;

				if (!mMenuHiden) {
					if ((mIsSlidingX&&x-mStartDragX>0)||(!mIsSlidingX&&y-mStartDragY>0)) {
						LayoutParams lparams = ((LayoutParams)mMenuLayout.getLayoutParams());
						animateMenu(lparams.width,true);
					}
					return true;
				}
				else if (!mIsSlidingX&&!mOnTop&&y-mStartDragY>0) return true;

				setOverlayPlace(x,y);
				animateRootLayout(mIsSlidingX,mXAxis,mYAxis);
				break;
			default:
				return false;
			}
			return true;

		}


		private void setOverlayPlace(int x, int y) {
			if (mOnTop) {
				int screenHeight = mAppLayout.getHeight();
				mOnTop = (screenHeight - mRootRelativeLayoutParams.bottomMargin - mRootRelativeLayoutParams.height)<DRAG_MOVE_RANGE;
				mClosed = false;
			}
			else{
				mOnTop = (mRootRelativeLayoutParams.bottomMargin)>DRAG_MOVE_RANGE;
				int screenWidth = mAppLayout.getWidth();
				int leftPointerX = mStartDragX - (screenWidth - OVERLAY_WIDTH/2 );
				mCloseOnRight = (x - leftPointerX)>=screenWidth/2&&mRootRelativeLayoutParams.leftMargin>=(screenWidth - 2*OVERLAY_WIDTH/3 );
				mClosed = (x - leftPointerX)<screenWidth/2||mCloseOnRight;
				mClosed = false;
			}

		}
	}

	private void animateRootLayout(final boolean aIsSlidingX,final int aXAxis,final int aYAxis) {

		// Scale the distance between open and close states to 0-1. 
		final int screenHeight = mAppLayout.getHeight();
		final int screenWidth = mAppLayout.getWidth();

		// secondary
		AnimationSet animations = new AnimationSet(false);
		animations.setFillAfter(true);
		animations.setDuration(ANIMATION_DURATION);

		//translate
		Animation animation = new Animation(){

			private int mY0 = aYAxis;
			private int mX0 = aXAxis;

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				int delta  = 0;
				int newYAxis =  aYAxis;
				int newXAxis =  aXAxis;
				if (!aIsSlidingX) {
					delta  =mOnTop?-mY0:screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN-mY0;
					newYAxis = (int) (delta*interpolatedTime + mY0);					
				}
				else{
					delta  =mClosed?(mCloseOnRight?screenWidth-mX0:-OVERLAY_WIDTH-mX0):screenWidth-OVERLAY_WIDTH-OVERLAY_BOTTOM_MARGIN-mX0;
					newXAxis = (int) (delta*interpolatedTime + mX0);					
				}
				updateViewLayout(true,aIsSlidingX,newXAxis,newYAxis);
				//updateSecondaryLayout(margin, fromAlpha);
				if (interpolatedTime==1) {
					mRootLayout.clearAnimation();
					mIsRootLayoutAnimating = false;
					updateBackView(true,BACK_VIEW_WIDTH,0);
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		animations.addAnimation(animation);
		// Play the animations
		mIsRootLayoutAnimating = true;
		mRootLayout.startAnimation(animations);
	}


	@SuppressLint("NewApi")
	private void updateViewLayout(boolean aWithAlpha, boolean aIsSlidingX, int mXAxis, int mYAxis) {
		int screenWidth = mAppLayout.getWidth();
		int screenHeight = mAppLayout.getHeight();
		int rightMargin = 0;
		int secondTopMargin = 0;
		if (!aIsSlidingX) {


			int widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN)*mYAxis/(float)(screenHeight-OVERLAY_HEIGHT));
			widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH)*mYAxis/(float)(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN));
			widthn = Math.max(widthn, OVERLAY_WIDTH);
			widthn = Math.min(widthn, screenWidth);
			int heighn = (int) (widthn*OVERLAY_HEIGHT/(float)OVERLAY_WIDTH);
			int margin = (int) (screenHeight - mYAxis - heighn);

			mRootRelativeLayoutParams =  new RelativeLayout.LayoutParams(widthn,heighn);

			if (mYAxis == screenHeight) {
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);			
			}
			else{
				mRootRelativeLayoutParams.bottomMargin = margin;
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

				secondTopMargin = mYAxis*MARGIN_TOP_OVERLAY/(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN);
			}
			rightMargin = mYAxis*OVERLAY_BOTTOM_MARGIN/(screenHeight - OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN);
			mRootRelativeLayoutParams.rightMargin = rightMargin;
		}
		else{
			if (mClosed) {
				mRootRelativeLayoutParams =  new RelativeLayout.LayoutParams(OVERLAY_WIDTH,OVERLAY_HEIGHT);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				if (mCloseOnRight ) {
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					mRootRelativeLayoutParams.leftMargin =  -1* OVERLAY_WIDTH;												
				}
				else{
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					mRootRelativeLayoutParams.rightMargin =  -1* OVERLAY_WIDTH;						
				}
			}
			else{
				mRootRelativeLayoutParams =  new RelativeLayout.LayoutParams(OVERLAY_WIDTH,OVERLAY_HEIGHT);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				mRootRelativeLayoutParams.setMargins(mXAxis, 0, screenWidth - OVERLAY_WIDTH - mXAxis, OVERLAY_BOTTOM_MARGIN);
			}
		}

		mRootLayout.setLayoutParams(mRootRelativeLayoutParams);

		float secondLastAlpha = (screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN-mYAxis)/(float)(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN);
		updateSecondaryLayout(secondTopMargin,secondLastAlpha,aIsSlidingX);

		float fromAlpha = 1.0f;
		if (aIsSlidingX) {
			float distance = mXAxis>(screenWidth-OVERLAY_WIDTH)?(OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN):(screenWidth);
			if (aWithAlpha) {
				fromAlpha = (1 - Math.abs(mXAxis+OVERLAY_WIDTH-screenWidth+OVERLAY_BOTTOM_MARGIN)/(float)distance);
			}
		}
		setAlphaValue(mRootLayout, fromAlpha);
	}

	@SuppressLint("NewApi")
	private void updateSecondaryLayout(int margin, float fromAlpha, boolean aIsSlidingX) {
		if (!aIsSlidingX) {
			LayoutParams secondaryLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			secondaryLayoutParams.addRule(RelativeLayout.BELOW,R.id.root_layout);
			secondaryLayoutParams.topMargin = -(((RelativeLayout.LayoutParams)mRootLayout.getLayoutParams()).bottomMargin - margin);
			setAlphaValue(mSecondaryLayout, fromAlpha);
			mSecondaryLayout.setLayoutParams(secondaryLayoutParams);
			mSecondaryLayout.requestLayout();
			mAppLayout.updateViewLayout(mSecondaryLayout, secondaryLayoutParams);

			//back view
			updateBackView(false,margin,fromAlpha);
		}
	}




	@Override
	public void onBackPressed() {
		if (mOnTop) {
			mOnTop = false;
			animateRootLayout(false,0,0);							
		}
		else{
			if (mMenuHiden) {
				super.onBackPressed();				
			}
			else{
				animateMenu(MENU_WIDTH, true);
				mMenuHiden = true;
			}
		}
	}
}