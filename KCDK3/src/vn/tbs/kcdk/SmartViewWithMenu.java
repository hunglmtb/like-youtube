package vn.tbs.kcdk;


import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import vn.tbs.kcdk.fragments.mediaplayer.VideoControllerView.ViewControl;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ImageViewTopCrop;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class SmartViewWithMenu implements OnClickListener, ViewControl {

	public interface OnTopListener {

		void doSmartViewOnTop(int yAxis, boolean reachBottom, boolean onTOp);

	}

	public enum OverlayMode {
		APP,
		HOME_SHOW
	}
	//values
	public static final int PADDING = 5;
	private static final int OVERLAY_HEIGHT = 150;
	private static final int OVERLAY_WIDTH = 270;
	private static final float DRAG_MOVE_RANGE = 3*OVERLAY_HEIGHT/4;
	private static final int MARGIN_TOP_OVERLAY = 150;
	private static final int OVERLAY_BOTTOM_MARGIN = 50;
	private static final long ANIMATION_DURATION = 400;
	private static final int MENU_WIDTH = 500;
	private static final float BACKVIEW_ALPHA_MAX = 0.8f;
	private static final int BACK_VIEW_WIDTH = 50;
	private static final int OVERLAY_MENU_MARGIN_LEFT = 20;
	protected static final int SWITCH_RANGE = 90;
	protected static final int SIMPLE_MODE_HEIGHT = 150;

	//fields
	protected boolean mInSimpleMode = false;
	protected boolean mOnTop = true;
	protected boolean mClosed = false;
	private boolean mCloseOnRight = false;
	private boolean mMenuHiden = true;
	private boolean mIsRootLayoutAnimating = false;
	private boolean mOtherAnimating = false;
	protected OverlayMode mOverlayMode = OverlayMode.APP;

	// Layout containers for various widgets
	private RelativeLayout					mLayout;
	private RelativeLayout 					mRootLayout;			// Root layout
	private ImageViewTopCrop				mMediaImage;
	private RelativeLayout 					mAppLayout;			// Reference to the window
	//private ListView						mMainListView ;
	private RelativeLayout.LayoutParams 	mRootRelativeLayoutParams;
	private RelativeLayout 					mSecondaryLayout;
	//private ListView 						mSecondListView;
	private View 							mBackView;
	private RelativeLayout 					mMenuLayout;
	private MenuListView 					mMenuListView ;
	private Context 						mContext;
	private RelativeLayout 					mMainLayout;
	private View 							mActionBarView;
	private boolean 						mShowDetailMedia = false;
	private Button 							mCloseThirdFragment;
	private RelativeLayout 					mThirdLayout;
	private FrameLayout 					mThirdFragmentContainer;

	private boolean 						mThirdFragmentContainerShowing = false; 
	//kcdk data
	private SmartMenu 						mSmartMenu;
	private KCDKMediaPlayer 				mKCDKMediaPlayer;
	private OnGlobalLayoutListener 			mOnGlobalLayoutListener;
	private OnTopListener					mOnTopListener;
	//listener
	private OnTouchListener 				mTouchListener = new OnTouchListener() {

		private boolean mFirstTimeMove = false;
		private boolean mSlidingX = false;
		private int mStartDownX;
		private int mStartDownY;
		private int mSlideXDelata = 40;
		private int mLastXposition = mStartDownX;


		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mIsRootLayoutAnimating||mOtherAnimating) {
				return true;
			}
			final int action = event.getActionMasked();
			int x = (int)event.getRawX();
			int y = (int)event.getRawY();
			Log.i("keke", "here");

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				mMenuLayout.clearAnimation();
				mBackView.clearAnimation();
				mRootLayout.clearAnimation();
				mFirstTimeMove = true;
				mStartDownX = x;
				mStartDownY = y;
				mLastXposition = x;
				return v.getId()==mMenuListView.getId()?mMenuHiden:true;
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
					mMenuListView.setScrollingDisable(true);
					return false;
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
				mMenuListView.setScrollingDisable(false);
				return v.getId()==mMenuListView.getId()?false:result;
			default:
				return false;
			}
		}
	};
	private Animation mAnimationFadeIn;
	private AnimationListener mFadeListener = new AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {
			mOtherAnimating = true;
			if (mThirdFragmentContainerShowing) {
				Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
			}
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			Log.i("hung", "onAnimationEnd   here");
			Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
			mThirdLayout.clearAnimation();
			mOtherAnimating = false;
		}
	};

	public KCDKMediaPlayer getKCDKMediaPlayer() {
		return mKCDKMediaPlayer;
	}

	public ImageView getMediaImage() {
		return mMediaImage;
	}

	public RelativeLayout getView() {
		return mLayout;
	}



	public boolean isShowDetailMedia() {
		return mShowDetailMedia;
	}

	public SmartViewWithMenu(Context aContext, final Intent intent, OnTopListener onTopListener) {
		super();

		this.mContext = aContext;
		setOnTopListener(onTopListener);

		mLayout = (RelativeLayout) LayoutInflater.from(mContext).inflate(R.layout.main_sliding, null);
		mMainLayout = (RelativeLayout) mLayout.findViewById(R.id.app_layout);
		//View playerNavigation = LayoutInflater.from(mContext).inflate(R.layout.media_player_panel_layout, null);

		RelativeLayout playerNavigation = (RelativeLayout) mLayout.findViewById(R.id.media_player_layout);
		mActionBarView = playerNavigation;
		mKCDKMediaPlayer = new KCDKMediaPlayer(mContext,playerNavigation);
		mKCDKMediaPlayer.setOnUpdateMediaDetailListener((ItemSelectionListener) aContext);

		//mMainListView = (ListView) mMainLayout.findViewById( R.id.mainListView );
		// Set the ArrayAdapter as the ListView's adapter.
		//mMainListView.setAdapter( listAdapter );  

		mOverlayMode = OverlayMode.HOME_SHOW;

		// Get references to all the views and add them to root view as needed.
		mAppLayout = (RelativeLayout) mMainLayout.findViewById(R.id.app_layout);
		mRootLayout = (RelativeLayout) mMainLayout.findViewById(R.id.root_layout);
		mMediaImage = (ImageViewTopCrop) mMainLayout.findViewById(R.id.media_image);
		mSecondaryLayout = (RelativeLayout) mMainLayout.findViewById(R.id.secondary_layout);
		mBackView = mMainLayout.findViewById(R.id.backView);
		mMenuLayout = (RelativeLayout) mMainLayout.findViewById(R.id.menu_layout);
		mMenuListView = (MenuListView) mMainLayout.findViewById( R.id.menuListView );
		mMenuLayout.setVisibility(View.GONE);
		mCloseThirdFragment = (Button) mMainLayout.findViewById( R.id.close_fragment);
		mCloseThirdFragment.setOnClickListener(this);
		mThirdLayout = (RelativeLayout) mMainLayout.findViewById( R.id.container);
		mThirdFragmentContainer = (FrameLayout) mMainLayout.findViewById( R.id.fragment_container);

		mRootRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) mRootLayout.getLayoutParams();

		mRootLayout.setOnTouchListener(new TrayTouchListener());


		mMenuListView.setOnTouchListener(mTouchListener);
		mBackView.setOnTouchListener(mTouchListener);

		if (intent!=null) {
			String action = intent.getAction();
			mShowDetailMedia = action!=null&&action.length()>0&&action.equals(Common.ACTION_LAUNCH);
		}

		//if (mShowDetailMedia) {
		ViewTreeObserver vto = mAppLayout.getViewTreeObserver(); 
		mOnGlobalLayoutListener = new OnGlobalLayoutListener() { 
			@Override 
			public void onGlobalLayout() { 
				if (mKCDKMediaPlayer!=null) {
					mKCDKMediaPlayer.requestUpdateGUI();
				}
				mAppLayout.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
				mShowDetailMedia = false;
			} 
		};
		vto.addOnGlobalLayoutListener(mOnGlobalLayoutListener);
		//		}
		//		else {
		//			
		//		}
		setOriginalPosition(false);			
		//kcdk init values
		mSmartMenu = new SmartMenu(mMenuListView, mContext);
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
				Log.i("hung", "interpolatedTime   "+interpolatedTime+" margin "+xPosition);
				updateMenu(xPosition);
				if (interpolatedTime==1) {
					mMenuLayout.clearAnimation();
					float alpha = aHiden?0:BACKVIEW_ALPHA_MAX;
					updateBackView(aHiden,OVERLAY_BOTTOM_MARGIN,alpha);
					mMenuHiden = aHiden;
					mOtherAnimating = animateThirdFragment(mThirdFragmentContainerShowing);
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
		mOtherAnimating = true;
	}

	protected boolean animateThirdFragment(boolean show) {

		mThirdLayout.clearAnimation();
		if (show) {
			if (mAnimationFadeIn==null) {
				mAnimationFadeIn = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
				mAnimationFadeIn.setAnimationListener(mFadeListener);
			}
			
			mThirdLayout.startAnimation(mAnimationFadeIn);
	        
		/*
			// secondary
			AnimationSet thirdAnimations = new AnimationSet(false);
			thirdAnimations.setFillAfter(true);
			thirdAnimations.setDuration(ANIMATION_DURATION);
			
			//translate
			TranslateAnimation thirdTranslate = new TranslateAnimation( 0 , 0 , 0,0){
				
				private int mThirdFragmentFoot;
				
				@Override
				protected void applyTransformation(float interpolatedTime,
						Transformation t) {
					if (mThirdFragmentFoot<=0) {
						mThirdFragmentFoot = mThirdLayout!=null?mThirdLayout.getLayoutParams().height -30:60;
					}
					if (mThirdFragmentContainerShowing) {
						Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
					}
					mThirdFragmentFoot =  mAppLayout.getHeight();
					int secondDeltaMargin  =mThirdFragmentContainerShowing?-mThirdFragmentFoot:mThirdFragmentFoot ;
					float aLastXposition = mThirdFragmentContainerShowing?mThirdFragmentFoot:0 ;
					int yPosition = (int) (secondDeltaMargin*interpolatedTime + aLastXposition + 100);
					updateThirdLayout(yPosition);
					Log.i("animate", "interpolatedTime   "+interpolatedTime+" margin "+yPosition);
					if (interpolatedTime==1) {
						mThirdLayout.clearAnimation();
						float alpha = mThirdFragmentContainerShowing?0:BACKVIEW_ALPHA_MAX;
						//updateBackView(aHiden,OVERLAY_BOTTOM_MARGIN,alpha);
						//mMenuHiden = aHiden;
						mOtherAnimating = false;
						Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
					}
				}
				
				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};*/
			
			/*thirdAnimations.addAnimation(thirdTranslate);
			mThirdLayout.startAnimation(thirdAnimations);*/
		}

		return show;
	}

	protected void updateThirdLayout(int yPosition) {
		//int xPosition = Math.min(x, MENU_WIDTH);
		//xPosition = Math.max(xPosition, 0);

		LayoutParams thirdParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		thirdParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		thirdParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		//thirdParams.addRule(RelativeLayout.BELOW,R.id.action_bar_view);
		thirdParams.topMargin = yPosition;
		mThirdLayout.setLayoutParams(thirdParams);
		//thirdParams.topMargin = 90;
		//mThirdLayout.requestLayout();
		//mAppLayout.updateViewLayout(mThirdLayout, thirdParams);
		//		mThirdLayout.setVisibility(View.VISIBLE);
		/*float fromAlpha = BACKVIEW_ALPHA_MAX*xPosition/(float)MENU_WIDTH;
		updateBackView(x<=0,OVERLAY_BOTTOM_MARGIN,fromAlpha);
		updatePrimaryView(xPosition);*/
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
		updateBackView(x<=0,OVERLAY_BOTTOM_MARGIN,fromAlpha);
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



	public void setOriginalPosition(boolean showDetailMedia) {
		mOnTop = showDetailMedia;
		mClosed = !showDetailMedia;
		mMenuHiden = true;
		//		int xAxis = mAppLayout.getWidth() - OVERLAY_WIDTH - OVERLAY_BOTTOM_MARGIN;
		int xAxis = 0;
		int yAxis = 0;
		boolean aIsSlidingX = !showDetailMedia;
		mInSimpleMode = false;
		mCloseOnRight = !showDetailMedia;
		updateViewLayout(false,aIsSlidingX,xAxis,yAxis);
		updateMenu(0);
		mRootLayout.setVisibility(mClosed?View.INVISIBLE:View.VISIBLE);
		mThirdFragmentContainerShowing = false;
		Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
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

				if (!mMenuHiden||mInSimpleMode) {
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
				else if (!mIsSlidingX&&((!mOnTop&&y>=mStartDragY)||(mInSimpleMode&&y<=mStartDragY))) return true;

				boolean simpleModeSwitched = mInSimpleMode&&!mIsSlidingX&&(y>mStartDragY);
				mInSimpleMode = mInSimpleMode||(!mIsSlidingX&&(mOnTop&&y<=mStartDragY));

				setOverlayPlace(x,y);
				animateRootLayout(mIsSlidingX,mXAxis,mYAxis,simpleModeSwitched);
				break;
			default:
				return false;
			}
			return true;

		}


		private void setOverlayPlace(int x, int y) {
			mYAxis = mInSimpleMode?mTopHeigh:mYAxis;

			if (mOnTop) {
				int screenHeight = mAppLayout.getHeight();
				mOnTop = (screenHeight - mRootRelativeLayoutParams.bottomMargin - mRootRelativeLayoutParams.height)<DRAG_MOVE_RANGE;
				mClosed = false;
			}
			else{
				mOnTop = (mRootRelativeLayoutParams.bottomMargin)>DRAG_MOVE_RANGE;
				int screenWidth = mAppLayout.getWidth();
				int leftPointerX = mStartDragX - (screenWidth - OVERLAY_WIDTH/2 );
				mCloseOnRight = mIsSlidingX&&((x - leftPointerX)>=screenWidth/2&&mRootRelativeLayoutParams.leftMargin>=(screenWidth - 2*OVERLAY_WIDTH/3 ));
				mClosed = mIsSlidingX&&((x - leftPointerX)<screenWidth/2||mCloseOnRight);
				//mClosed = false;
			}
			mOnTop= mInSimpleMode||mOnTop;
		}
	}

	private void animateRootLayout(final boolean aIsSlidingX,final int aXAxis,final int aYAxis, final boolean simpleModeSwitched) {

		// Scale the distance between open and close states to 0-1. 
		mRootLayout.clearAnimation();
		final int screenHeight = mAppLayout.getHeight();
		final int screenWidth = mAppLayout.getWidth();

		// secondary
		AnimationSet animations = new AnimationSet(false);
		animations.setFillAfter(true);
		animations.setDuration(ANIMATION_DURATION);

		//translate
		Animation animation = new Animation(){

			private int mY0 = aYAxis;
			private int mY1 = simpleModeSwitched?(int) (screenWidth*OVERLAY_HEIGHT/(float)OVERLAY_WIDTH):0;
			private int mX0 = aXAxis;

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				int delta  = 0;
				int newYAxis =  aYAxis;
				int newXAxis =  aXAxis;
				if (!aIsSlidingX) {
					delta  =mOnTop?-mY0:screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN-mY0;
					delta  =mInSimpleMode?mY1-mY0:delta;
					newYAxis = (int) (delta*interpolatedTime + mY0);					
				}
				else{
					delta  =mClosed?(mCloseOnRight?screenWidth-mX0:-OVERLAY_WIDTH-mX0):screenWidth-OVERLAY_WIDTH-OVERLAY_BOTTOM_MARGIN-mX0;
					newXAxis = (int) (delta*interpolatedTime + mX0);					
				}
				updateViewLayout(true,aIsSlidingX,newXAxis,newYAxis);
				//updateSecondaryLayout(margin, fromAlpha);
				if (interpolatedTime==1) {
					mIsRootLayoutAnimating = false;
					mRootLayout.clearAnimation();
					mKCDKMediaPlayer.updateView(mInSimpleMode||(mOnTop&&newYAxis==0),true);
					if (mClosed) {
						mKCDKMediaPlayer.stopMediaPlayer();						
					}
					mRootLayout.setVisibility(mClosed?View.GONE:View.VISIBLE);
					mSecondaryLayout.setVisibility(mClosed?View.GONE:View.VISIBLE);
					updateBackView(true,BACK_VIEW_WIDTH,0);
					mInSimpleMode = mInSimpleMode&&!simpleModeSwitched;
					mOnTop = mInSimpleMode||mOnTop;

					if (mOnTopListener!=null&&!aIsSlidingX&&!mInSimpleMode) {
						mOnTopListener.doSmartViewOnTop(newYAxis,!mOnTop&&!mInSimpleMode,mOnTop);
					}
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		animations.addAnimation(animation);
		// Play the animations
		mRootLayout.startAnimation(animations);
		mIsRootLayoutAnimating = true;
	}


	@SuppressLint("NewApi")
	private void updateViewLayout(boolean aWithAlpha, boolean aIsSlidingX, int mXAxis, int mYAxis) {
		Log.e("hung", "hehe aIsSlidingX "+aIsSlidingX+" mXAxis "+mXAxis+" mYAxis "+mYAxis);
		int screenWidth = mAppLayout.getWidth();
		int screenHeight = mAppLayout.getHeight();
		int rightMargin = 0;
		int secondTopMargin = 0;
		mMediaImage.setKeepRatio(mInSimpleMode);
		if (!aIsSlidingX) {


			int widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN)*mYAxis/(float)(screenHeight-OVERLAY_HEIGHT));
			widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH)*mYAxis/(float)(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN));
			widthn = Math.max(widthn, OVERLAY_WIDTH);
			widthn = Math.min(widthn, screenWidth);
			int height = (int) (widthn*OVERLAY_HEIGHT/(float)OVERLAY_WIDTH);
			int margin = (int) (screenHeight - mYAxis - height);

			height = mInSimpleMode?mYAxis:height;
			widthn = mInSimpleMode?screenWidth:widthn;

			mRootRelativeLayoutParams =  new RelativeLayout.LayoutParams(widthn,height);

			if (mYAxis == screenHeight) {
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			}
			else{
				mRootRelativeLayoutParams.bottomMargin = margin;

				if (mInSimpleMode) {
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				else{
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					secondTopMargin = mYAxis*MARGIN_TOP_OVERLAY/(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN);
					rightMargin = mYAxis*OVERLAY_BOTTOM_MARGIN/(screenHeight - OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN);
					mRootRelativeLayoutParams.rightMargin = rightMargin;
				}
			}
			Log.e("hung", "haha mInSimpleMode "+mInSimpleMode+" widthn "+widthn+" height "+height +" screenHeight "+screenHeight);

		}
		else{
			if (mClosed) {
				mRootRelativeLayoutParams =  new RelativeLayout.LayoutParams(OVERLAY_WIDTH,OVERLAY_HEIGHT);
				mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				if (mCloseOnRight ) {
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				}
				else{
					mRootRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				}
				mRootRelativeLayoutParams.setMargins(mXAxis, 0, screenWidth - OVERLAY_WIDTH - mXAxis, OVERLAY_BOTTOM_MARGIN);
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
		secondLastAlpha = mInSimpleMode?1:secondLastAlpha;
		updateSecondaryLayout(secondTopMargin,secondLastAlpha,aIsSlidingX);

		float fromAlpha = 1.0f;
		if (aIsSlidingX) {
			float distance = mXAxis>(screenWidth-OVERLAY_WIDTH)?(OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN):(screenWidth);
			if (aWithAlpha) {
				fromAlpha = (1 - Math.abs(mXAxis+OVERLAY_WIDTH-screenWidth+OVERLAY_BOTTOM_MARGIN)/(float)distance);
			}
		}
		setAlphaValue(mRootLayout, fromAlpha);

		//mKCDKMediaPlayer.updateView(mInSimpleMode||(mOnTop&&mYAxis==0),false);

		if (mOnTopListener!=null&&!aIsSlidingX&&!mInSimpleMode) {
			mOnTopListener.doSmartViewOnTop(mYAxis,false,mOnTop);
		}
	}

	@SuppressLint("NewApi")
	private void updateSecondaryLayout(int margin, float fromAlpha, boolean aIsSlidingX) {
		if (!aIsSlidingX) {
			LayoutParams secondaryLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			secondaryLayoutParams.addRule(RelativeLayout.BELOW,R.id.root_layout);
			secondaryLayoutParams.topMargin = -(((RelativeLayout.LayoutParams)mRootLayout.getLayoutParams()).bottomMargin - margin);
			setAlphaValue(mSecondaryLayout, fromAlpha);
			mSecondaryLayout.setLayoutParams(secondaryLayoutParams);
			//mSecondaryLayout.requestLayout();
			//mAppLayout.updateViewLayout(mSecondaryLayout, secondaryLayoutParams);
			Log.e("hung keke", "topmargin "+secondaryLayoutParams.topMargin);

			//back view
			updateBackView(false,margin,fromAlpha);
			//setAlphaValue(mActionBarView,1-fromAlpha);
		}
	}



	public boolean onBackPressed() {
		if (mIsRootLayoutAnimating) {
			return false;			
		}
		if (mOnTop) {
			//int y0 = mInSimpleMode?SIMPLE_MODE_HEIGHT:0;
			animateRootLayout(false,0,0,true);							
			mOnTop = mInSimpleMode;
		}
		else{
			if (mMenuHiden) {
				return true;				
			}
			else{
				animateMenu(MENU_WIDTH, true);
				mMenuHiden = true;
			}
		}
		return false;
	}

	public void setOnTopListener(OnTopListener aOnTopListener) {
		this.mOnTopListener = aOnTopListener;
	}

	public void showMediaContent(MediaInfo item, boolean reset, boolean animate) {
		// TODO Auto-generated method stub
		//loadImage(item);
		mClosed = false;
		mOnTop = true;
		mRootLayout.setVisibility(View.VISIBLE);
		mSecondaryLayout.setVisibility(View.VISIBLE);
		if (animate) {
			int screenHeight = mAppLayout.getHeight();
			animateRootLayout(false,0,screenHeight-OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN,false);			
		}
		if (mKCDKMediaPlayer!=null) {
			mKCDKMediaPlayer.playMedia(item,mClosed,reset);						
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.close_fragment:
			mThirdFragmentContainerShowing = false;
			Common.setVisible(mThirdLayout, mThirdFragmentContainerShowing);
			//animateThirdFragment(true);
			break;

		default:
			break;
		}
	}


	public SmartMenu  getSmartMenu() {
		return mSmartMenu;
	}

	public void doMenuItemSelection(CategoryRow item) {
		animateMenu(MENU_WIDTH, true);
		mMenuHiden = true;
		//TODO update later
		mThirdFragmentContainerShowing = item!=null?"CATEGORY01".equals(item.getCategoryId()):false;
	}

	@Override
	public void viewDetail() {
		if (mMenuHiden) {
			if ((!mOnTop||mClosed)) {
				
				mOnTop = true;
				mClosed = false;
				mRootLayout.setVisibility(View.VISIBLE);
				int screenHeight = mAppLayout.getHeight();
				animateRootLayout(false,0,screenHeight-OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN,false);			
			}
			else if (mInSimpleMode) {
				swith2FullMode();
			}
			
		}
	}

	private void swith2FullMode() {
		if (mOnTop) {
			animateRootLayout(false,0,0,true);							
		}
	}
}