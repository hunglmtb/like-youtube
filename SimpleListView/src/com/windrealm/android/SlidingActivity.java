package com.windrealm.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.windrealm.android.MockPlaylist.MockPlaylistListener;

public class SlidingActivity extends Activity implements MockPlaylistListener {

	public enum OverlayMode {
		APP,
		HOME_SHOW
	}

	// 
	private static final int TRAY_HIDDEN_FRACTION 			= 6; 	// Controls fraction of the tray hidden when open
	private static final int TRAY_MOVEMENT_REGION_FRACTION 	= 6;	// Controls fraction of y-axis on screen within which the tray stays.
	private static final int TRAY_CROP_FRACTION 			= 12;	// Controls fraction of the tray chipped at the right end.
	private static final int ANIMATION_FRAME_RATE 			= 30;	// Animation frame rate per second.
	private static final int TRAY_DIM_X_DP 					= 170;	// Width of the tray in dps
	private static final int TRAY_DIM_Y_DP 					= 160; 	// Height of the tray in dps
	private static final int BUTTONS_DIM_Y_DP 				= 27;	// Height of the buttons in dps
	public static final int PADDING = 5;
	private static final int OVERLAY_HEIGHT = 210;
	private static final int OVERLAY_WIDTH = 280;
	private static final float DRAG_MOVE_RANGE = 3*OVERLAY_HEIGHT/4;
	private static final int MARGIN_TOP_OVERLAY = 150;
	private static final int OVERLAY_BOTTOM_MARGIN = 50;
	private static final long ANIMATION_DURATION = 400;
	private static final int MENU_WIDTH = 500;
	private static final float FADE_ALPHA_MAX = 0.8f;
	private static final int BACK_VIEW_WIDTH = 50;

	// Layout containers for various widgets
	private WindowManager.LayoutParams 	mRootLayoutParams;		// Parameters of the root layout
	private RelativeLayout 				mRootLayout;			// Root layout
	private RelativeLayout 				mContentContainerLayout;// Contains everything other than buttons and song info
	/*private RelativeLayout 				mAlbumCoverLayout;		// Contains album cover of the active song
	private RelativeLayout 				mLogoLayout;			// Contains Cpotify logo
	private RelativeLayout 				mAlbumCoverHelperLayout;// Contains cover of the previous song. This helps with fade animations.
	private LinearLayout 				mPlayerButtonsLayout;	// Contains playback buttons
	private LinearLayout 				mSongInfoLayout;		// Contains Text information on the current song
	private ImageView 					mTrayOpener;		// Contains Text information on the current song
	private ImageView 					mTrayOpenerRight;*/


	// Widgets
	private ImageButton mPlaySongButton;
	private ImageButton mPauseSongButton;
	private TextView mSongTitleView;
	private TextView mSingerView;

	// Variables that control drag
	private int mStartDragX;
	private int mStartDragY; // Unused as yet
	private int mPrevDragX;
	private int mPrevDragY;

	private boolean mIsTrayOpen = true;

	// Controls for animations
	private Timer 					mTrayAnimationTimer;
	private TrayAnimationTimerTask 	mTrayAnimationTimerTask;
	private Handler 				mAnimationHandler = new Handler();

	// Mock song data
	private MockPlaylist mPlaylist;
	private boolean mIsSlidingX = true;
	private boolean mIsFirstTimeMove = false;
	protected boolean mOnTop = true;
	protected boolean mClosed = false;
	protected OverlayMode mOverlayMode = OverlayMode.APP;

	private RelativeLayout 				mAppLayout;			// Reference to the window
	private ListView mMainListView ;
	private ArrayAdapter<String> listAdapter ;
	private RelativeLayout.LayoutParams mRootRelativeLayoutParams;
	private int mYAxis = 0;
	private int mXAxis;
	private int mLastY = 0;
	private int mLastX = 0;

	private boolean mDoAnimation = true;
	private boolean mSlidingStart = false;
	private int mTopHeigh;
	private boolean mCloseOnRight = false;
	private boolean mEnableTouch = true;
	private RelativeLayout mSecondaryLayout;
	private ListView mSecondListView;
	private View mBackView;
	private RelativeLayout mMenuLayout;
	private ListView mMenuListView ;
	private int mSecondTopMargin = 0;
	private float mSecondLastAlpha = 1;
	private OnTouchListener mListener;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_sliding);

		mMainListView = (ListView) findViewById( R.id.mainListView );

		// Create and populate a List of planet names.
		String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
				"Jupiter", "Saturn", "Uranus", "Neptune", "Neptune", "Neptune", "Neptune", "Neptune", "Neptune"};  
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

		/*mRootLayout = (RelativeLayout) LayoutInflater.from(this).
				inflate(R.layout.service_player, null);*/

		mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);
		mSecondaryLayout = (RelativeLayout) findViewById(R.id.secondary_layout);
		mBackView = findViewById(R.id.backView);
		mMenuLayout = (RelativeLayout) findViewById(R.id.menu_layout);
		mMenuListView = (ListView) findViewById( R.id.menuListView );
		mMenuListView.setAdapter( listAdapter );  

		mRootLayout.setOnTouchListener(new TrayTouchListener());
		mListener = new OnTouchListener() {

			private boolean mFirstTimeMove = false;
			private boolean mSlidingX = false;
			private int mStartDownX;
			private int mStartDownY;
			private int mSlideXDelata = 40;
			private int mLastXposition;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int action = event.getActionMasked();
				int x = (int)event.getRawX();
				int y = (int)event.getRawY();

				switch (action) {
				case MotionEvent.ACTION_DOWN: 
					mFirstTimeMove = true;
					mStartDownX = x;
					mStartDownY = y;
					mLastXposition = x;
					return true;
				case MotionEvent.ACTION_MOVE:
					// Filter and redirect the events to dragTray()

					// Calculate position of the whole tray according to the drag, and update layout.
					float deltaX = x-mStartDownX;
					float deltaY = y-mStartDownY;

					if (mFirstTimeMove ) {
						mSlidingX =  Math.abs(deltaY)<=Math.abs(deltaX);
						mFirstTimeMove = false;
						LayoutParams lparams = ((LayoutParams)mMenuLayout.getLayoutParams());
						mSlideXDelata = mStartDownX - (lparams.width + lparams.leftMargin);
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
						animateMenu(mLastXposition);						
					}
					boolean result = mSlidingX;
					mSlidingX = false;
					return result;
				default:
					return false;
				}
			}
		};
		
		mMenuListView.setOnTouchListener(mListener);
		mBackView.setOnTouchListener(mListener);

		
		mRootLayoutParams = new WindowManager.LayoutParams(
				Utils.dpToPixels(TRAY_DIM_X_DP, getResources()),
				Utils.dpToPixels(TRAY_DIM_Y_DP, getResources()),
				WindowManager.LayoutParams.TYPE_PHONE, 
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, 
				PixelFormat.TRANSLUCENT);

		mRootLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		//mAppLayout.addView(mRootLayout, mRootLayoutParams);
		mRootRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) mRootLayout.getLayoutParams();

		/*	mPlaySongButton = (ImageButton) mPlayerButtonsLayout.findViewById(R.id.button_play);
		mPauseSongButton = (ImageButton) mPlayerButtonsLayout.findViewById(R.id.button_pause);
		mSongTitleView = (TextView) mSongInfoLayout.findViewById(R.id.song_name);
		mSingerView = (TextView) mSongInfoLayout.findViewById(R.id.singer_name);
		 */
		mPlaylist = new MockPlaylist(this);
		// Post these actions at the end of looper message queue so that the layout is
			// fully inflated once these functions execute
		/*mRootLayout.postDelayed(new Runnable() {
			@Override
			public void run() {

				
				
			}
		}, ANIMATION_FRAME_RATE);*/
		
		mSecondListView = (ListView) findViewById( R.id.secondListView );
		mSecondListView.setAdapter( listAdapter );  

		setOriginalPosition();		 
	}



	protected void animateMenu(final int mLastXposition) {		
		// secondary
		AnimationSet menuAnimations = new AnimationSet(false);
		menuAnimations.setFillAfter(true);
		menuAnimations.setDuration(ANIMATION_DURATION);

		//translate
		TranslateAnimation menuTranslate = new TranslateAnimation( 0 , 0 , 0,0){

			@Override
			protected void applyTransformation(float interpolatedTime,
					Transformation t) {
				int secondDeltaMargin  =mLastXposition<MENU_WIDTH/2?-MENU_WIDTH:MENU_WIDTH -mLastXposition ;
				int xPosition = (int) (secondDeltaMargin*interpolatedTime + mLastXposition);
				updateMenu(xPosition);
				Log.i("hung", "interpolatedTime   "+interpolatedTime+" margin "+xPosition);
				if (interpolatedTime==1) {
					mMenuLayout.clearAnimation();
					boolean hiden = xPosition<=BACK_VIEW_WIDTH;
					float alpha = hiden?0:FADE_ALPHA_MAX;
					updateBackView(hiden,OVERLAY_BOTTOM_MARGIN,alpha);
				}
			}

	          @Override
	          public boolean willChangeBounds() {
	              return true;
	          }
		};
		
		menuAnimations.addAnimation(menuTranslate);
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
	private void setAlphaValue(View aBackView, float fromAlpha) {
		fromAlpha = Math.max(fromAlpha, 0);
		fromAlpha = Math.min(fromAlpha, 1);
		
		if (Build.VERSION.SDK_INT < 11) {
			final AlphaAnimation animation = new AlphaAnimation(fromAlpha, fromAlpha);
			long duration = 0;
			animation.setDuration(duration );
			animation.setFillAfter(true);
			aBackView.startAnimation(animation);
		}else{
			aBackView.setAlpha(fromAlpha);
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
		
		float fromAlpha = FADE_ALPHA_MAX*xPosition/(float)MENU_WIDTH;
		updateBackView(false,OVERLAY_BOTTOM_MARGIN,fromAlpha);
	}



	private void setOriginalPosition() {
		mOnTop = false;
		mIsSlidingX = true;
		mClosed = false;
		mXAxis = mAppLayout.getWidth() - OVERLAY_WIDTH - OVERLAY_BOTTOM_MARGIN;
		updateViewLayout();
		//mBackView.setVisibility(View.GONE);
		updateMenu(150);
	}



	// Listens to the touch events on the tray.
	private class TrayTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			final int action = event.getActionMasked();
			if (!mEnableTouch||(mRootLayout.getAnimation()!=null&&!mRootLayout.getAnimation().hasEnded())) {
				return false;
			}
			switch (action) {
			case MotionEvent.ACTION_DOWN: 
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				// Filter and redirect the events to dragTray()
				dragTray(action, (int)event.getRawX(), (int)event.getRawY());
				break;
			default:
				return false;
			}
			return true;

		}
	}


	// Drags the tray as per touch info
	private void dragTray(int action, int x, int y){
		
		switch (action){
		case MotionEvent.ACTION_DOWN:
			mTopHeigh = mOnTop?mRootLayout.getHeight():OVERLAY_HEIGHT;

			mIsFirstTimeMove = true;
			// Cancel any currently running animations/automatic tray movements.
			if (mTrayAnimationTimerTask!=null){
				mTrayAnimationTimerTask.cancel();
				mTrayAnimationTimer.cancel();
			}
			
			mRootLayout.clearAnimation();

			// Store the start points
			mStartDragX = x;
			mStartDragY = y;
			mPrevDragX = x;
			mPrevDragY = y;

			mDoAnimation = false;
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

			if (mIsSlidingX) {  
				mRootLayoutParams.x += deltaX;
				if (!mOnTop) {
					int slideXDelata = mStartDragX - (mAppLayout.getWidth() - OVERLAY_WIDTH -OVERLAY_BOTTOM_MARGIN);
					mXAxis = x - slideXDelata;
					int screenWidth = mAppLayout.getWidth();
					mXAxis = Math.min(mXAxis, screenWidth+OVERLAY_WIDTH);
					mXAxis = Math.max(mXAxis, -1*OVERLAY_WIDTH);
				}
				else return;
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
					mRootLayoutParams.y += deltaY;
					mYAxis = y - slideYDelata;
					int screenHeight = mAppLayout.getHeight();
					mYAxis = Math.min(mYAxis, screenHeight-OVERLAY_HEIGHT -OVERLAY_BOTTOM_MARGIN);
					mYAxis = Math.max(mYAxis, 0);
					
				}
				else return;

			}

			mPrevDragX = x;
			mPrevDragY = y;

			mDoAnimation = true;

			//Log.i("hung", "mRootLayoutParams.x "+mRootLayoutParams.x+" mRootLayoutParams.y"+mRootLayoutParams.y);
			//animateButtons();
			updateViewLayout();

			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mIsFirstTimeMove = true;
			mSlidingStart = false;
			//mEnableTouch = true;
			if (mEnableTouch) {
				setOverlayPlace(x,y);
				doAnimation();				
			}
			break;
		}
	}


	private void doAnimation() {
		//animation
		if (mDoAnimation ) {
			mTrayAnimationTimerTask = new TrayAnimationTimerTask();
			mTrayAnimationTimer = new Timer();
			mTrayAnimationTimer.schedule(mTrayAnimationTimerTask, 0, ANIMATION_FRAME_RATE);			
		}
	}



	// Timer for animation/automatic movement of the tray.
	private class TrayAnimationTimerTask extends TimerTask{

		// Ultimate destination coordinates toward which the tray will move
		int mDestX;
		int mDestY;

		public TrayAnimationTimerTask(){
			// Setup destination coordinates based on the tray state. 
			super();
			setDestinationCordinate();

		}

		private void setDestinationCordinate() {
			int screenHeight = mAppLayout.getHeight();
			int screenWidth = mAppLayout.getWidth();
			switch (mOverlayMode) {
			case APP:
				break;
			case HOME_SHOW:

				// Keep upper edge of the widget within the upper limit of screen

				if (mClosed) {
					mDestY = screenHeight;					
				}
				else{
					mDestY = Math.max(screenHeight/TRAY_MOVEMENT_REGION_FRACTION,mRootLayoutParams.y);
					// Keep lower edge of the widget within the lower limit of screen
					mDestY = Math.min(((TRAY_MOVEMENT_REGION_FRACTION-1)*screenHeight)/TRAY_MOVEMENT_REGION_FRACTION - mRootLayoutParams.width,mDestY);
				}

				break;
			default:
				break;
			}

			mLastY  = mYAxis;
			mLastX = mXAxis;

			mYAxis=Math.min(screenHeight-OVERLAY_HEIGHT, mYAxis);
			mYAxis=Math.max(mYAxis, 0);
			if (mOnTop) {
				mYAxis=0;
			}
			else{
				mYAxis=screenHeight-OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN;
				mXAxis = mClosed?-1*OVERLAY_HEIGHT:screenWidth-OVERLAY_WIDTH - OVERLAY_BOTTOM_MARGIN; 
			}
		}

		// This function is called after every frame.
		@Override
		public void run() {

			// handler is used to run the function on main UI thread in order to
			// access the layouts and UI elements.
			mAnimationHandler.post(new Runnable() {
				@Override
				public void run() {
					mEnableTouch = false;

					// Update coordinates of the tray
					mRootLayoutParams.x = (2*(mRootLayoutParams.x-mDestX))/3 + mDestX;
					mRootLayoutParams.y = (2*(mRootLayoutParams.y-mDestY))/3 + mDestY;

					animate2Destination();
					// Cancel animation when the destination is reached
					TrayAnimationTimerTask.this.cancel();
					mTrayAnimationTimer.cancel();
					if (mClosed) {
						//stopSelf();
						//mRootLayout.setVisibility(View.GONE);
					}
				}
			});
		}
	}

	@SuppressLint("NewApi")
	private void updateViewLayout() {
		try {
			int screenWidth = mAppLayout.getWidth();
			int screenHeight = mAppLayout.getHeight();
			int rightMargin = 0;

			if (!mIsSlidingX) {


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
					
					mSecondTopMargin = mYAxis*MARGIN_TOP_OVERLAY/(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN);
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

			//Log.i("hung", "widthn "+mRootRelativeLayoutParams.width+ " heighn "+mRootRelativeLayoutParams.height+ " mYAxis "+mYAxis+ " rightMargin "+mRootRelativeLayoutParams.rightMargin+" bottomMargin"+mRootRelativeLayoutParams.bottomMargin);
			mRootLayout.setLayoutParams(mRootRelativeLayoutParams);

			mSecondLastAlpha  = (screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN-mYAxis)/(float)(screenHeight-OVERLAY_HEIGHT-OVERLAY_BOTTOM_MARGIN);
			updateSecondaryLayout(mSecondTopMargin,mSecondLastAlpha);

		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
		}

		float fromAlpha = 1.0f;
		if (mIsSlidingX) {
			float distance = 1;
			float remain = 1;
			switch (mOverlayMode) {
			case APP:
				int landMarkX = getResources().getDisplayMetrics().widthPixels -mRootLayoutParams.width;
				if (mRootLayoutParams.x-landMarkX>0) {
					distance = mRootLayoutParams.width;
					remain = getResources().getDisplayMetrics().widthPixels - mRootLayoutParams.x;
				}
				else{
					distance  = landMarkX;
					remain = mRootLayoutParams.x;
				}

				break;
			case HOME_SHOW:
				//int screenHeight = getResources().getDisplayMetrics().heightPixels;
				int screenHeight = mAppLayout.getHeight();

				distance = screenHeight/TRAY_MOVEMENT_REGION_FRACTION+mRootLayoutParams.width;
				remain = screenHeight-mRootLayoutParams.y;
				//(mRootLayoutParams.y>((TRAY_MOVEMENT_REGION_FRACTION-1)*screenHeight)/TRAY_MOVEMENT_REGION_FRACTION-mRootLayoutParams.width);

				break;
			default:
				break;


			}
			int screenWidth = mAppLayout.getWidth();
			distance = mXAxis>(screenWidth-OVERLAY_WIDTH)?(OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN):(screenWidth);
			fromAlpha = (1 - Math.abs(mXAxis+OVERLAY_WIDTH-screenWidth+OVERLAY_BOTTOM_MARGIN)/(float)distance);
			fromAlpha = Math.max(fromAlpha, 0);
			fromAlpha = Math.min(fromAlpha, 1);
			//Log.i("hung", "translate fromAlpha "+fromAlpha +" mXAxis "+mXAxis+" screenWidth "+ screenWidth);

			//alpha = alpha<0.0000001?0:alpha;
		}

		if (Build.VERSION.SDK_INT < 11) {
			final AlphaAnimation animation = new AlphaAnimation(fromAlpha, fromAlpha);
			long duration = 0;
			animation.setDuration(duration );
			animation.setFillAfter(true);
			mRootLayout.startAnimation(animation);
		}else{
			mRootLayout.setAlpha(fromAlpha);
		}
		
	}

	@SuppressLint("NewApi")
	private void updateSecondaryLayout(int margin, float fromAlpha) {
		if (!mIsSlidingX) {
			LayoutParams secondaryLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			secondaryLayoutParams.addRule(RelativeLayout.BELOW,R.id.root_layout);
			secondaryLayoutParams.topMargin = -(((RelativeLayout.LayoutParams)mRootLayout.getLayoutParams()).bottomMargin - margin);
			
			fromAlpha = Math.max(fromAlpha, 0);
			fromAlpha = Math.min(fromAlpha, 1);
			
			if (Build.VERSION.SDK_INT < 11) {
				final AlphaAnimation animation = new AlphaAnimation(fromAlpha, fromAlpha);
				long duration = 0;
				animation.setDuration(duration );
				animation.setFillAfter(true);
				mSecondaryLayout.startAnimation(animation);
				//mBackView.startAnimation(animation);
			}else{
				mSecondaryLayout.setAlpha(fromAlpha);
				//mBackView.setAlpha(fromAlpha);
			}
			
			mSecondaryLayout.setLayoutParams(secondaryLayoutParams);
			mSecondaryLayout.requestLayout();
			mAppLayout.updateViewLayout(mSecondaryLayout, secondaryLayoutParams);
			
			//back view
			updateBackView(false,margin,fromAlpha);
		}
	}



	// This function animates the buttons based on the position of the tray.
	private void animateButtons(){
		/*
		// Animate only if the tray is between open and close state.
		if (mRootLayoutParams.x < -mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION){

			// Scale the distance between open and close states to 0-1. 
			float relativeDistance = (mRootLayoutParams.x + mLogoLayout.getWidth())/(float)
					(-mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION + mLogoLayout.getWidth());

			// Limit it to 0-1 if it goes beyond 0-1 for any reason.
			relativeDistance=Math.max(relativeDistance, 0);
			relativeDistance=Math.min(relativeDistance, 1);

			// Setup animations
			AnimationSet animations = new AnimationSet(true);
			animations.setFillAfter(true);
			Animation animationAlpha = new AlphaAnimation(
					relativeDistance, 
					relativeDistance);
			animations.addAnimation(animationAlpha);

			Animation animationScale = new ScaleAnimation(
					relativeDistance, 
					relativeDistance, 
					relativeDistance, 
					relativeDistance);
			animations.addAnimation(animationScale);

			// Play the animations
			mPlayerButtonsLayout.startAnimation(animations);
			mSongInfoLayout.startAnimation(animations);
			mAlbumCoverLayout.startAnimation(animationAlpha);
			mRootLayout.startAnimation(animations);
		}else{

			// Clear all animations if the tray is being dragged - that is, when it is beyond the
			// normal open state.
			mPlayerButtonsLayout.clearAnimation();
			mSongInfoLayout.clearAnimation();
			mAlbumCoverLayout.clearAnimation();
			//mRootLayout.clearAnimation();
		}*/

		//scaleRootLayout();


		// Scale the distance between open and close states to 0-1. 
		//float relativeDistance = (mRootLayoutParams.x + mLogoLayout.getWidth())/(float)(-mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION + mLogoLayout.getWidth());
		int screenHeight = mAppLayout.getHeight();
		int screenWidth = mAppLayout.getWidth();
		float fromTranslateX = 0;
		float toTranslateX = 0;
		float relativeDistance = (screenHeight- mYAxis)/(float)screenHeight;

		// Limit it to 0-1 if it goes beyond 0-1 for any reason.
		relativeDistance=Math.max(relativeDistance, 0);
		relativeDistance=Math.min(relativeDistance, 1);

		long duration = 800;
		AnimationSet animations = new AnimationSet(true);
		animations.setFillAfter(true);
		animations.setDuration(duration);

		if (mOnTop) {
			mLastY -= 30*mLastY/duration;
		}
		else{
			mLastY += 8*(screenHeight-OVERLAY_HEIGHT-mLastY)/duration;
		}

		mLastY = Math.max(mLastY, 0);
		mLastY = Math.min(mLastY, screenHeight-OVERLAY_HEIGHT);

		int widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH)*mLastY/(float)(screenHeight-OVERLAY_HEIGHT));
		widthn = Math.max(widthn, OVERLAY_WIDTH);
		widthn = Math.min(widthn, screenWidth);
		int marginTop = Math.max(mLastY, 0);
		int marginLeft =  (int) ((screenWidth-OVERLAY_WIDTH)*mLastY/(float)(screenHeight-OVERLAY_HEIGHT));

		marginLeft = Math.max(marginLeft, 0);



		float targetW = 0;
		if (mOnTop) {
			targetW = screenWidth;
		}
		else{
			targetW = OVERLAY_WIDTH;
			marginTop = mLastY-screenHeight + OVERLAY_HEIGHT;
			marginLeft = OVERLAY_WIDTH - widthn;

			if (mIsSlidingX) {
				// Setup animations
				float fromAlpha = relativeDistance;
				float toAlpha = 1;

				marginTop = 0;					
				if (mClosed) {
					if (mCloseOnRight) {
						toTranslateX = OVERLAY_WIDTH;	
						marginLeft = mLastX - screenWidth + OVERLAY_WIDTH;
					}
					else{
						toTranslateX = -1*OVERLAY_WIDTH;						
						marginLeft = mLastX;
					}
					toAlpha = 0;
				}
				else{
					marginLeft = OVERLAY_WIDTH + mLastX - screenWidth;
					toAlpha = 1;
				}
				//marginLeft = OVERLAY_WIDTH + mLastX - screenWidth;

				fromAlpha = (screenWidth - Math.abs(mLastX+OVERLAY_WIDTH-screenWidth))/(float)screenWidth;
				fromAlpha = Math.max(fromAlpha, 0);
				fromAlpha = Math.min(fromAlpha, 1);

				Animation animationAlpha = new AlphaAnimation(
						fromAlpha, 
						toAlpha);
				//animations.addAnimation(animationAlpha);

			}
		}

		float fromX = (screenWidth - (screenWidth-OVERLAY_WIDTH)*mLastY/(float)(screenHeight-OVERLAY_HEIGHT))/targetW;
		float toX = 1.0f;
		float fromY = fromX;
		float toY = 1.0f;


		if (!mIsSlidingX) {
			Animation animationScale = new ScaleAnimation(fromX, toX, fromY, toY);			
			animations.addAnimation(animationScale);
		}

		fromTranslateX = marginLeft;
		TranslateAnimation translate = new TranslateAnimation( fromTranslateX , toTranslateX , marginTop,0 );
		translate.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation arg0) {
			}           
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}           
			@Override
			public void onAnimationEnd(Animation arg0) {
				mRootLayout.clearAnimation();
				updateViewLayout();
				if (mClosed) {
					//stopSelf();
					mRootLayout.setVisibility(View.GONE);
					//mainListView.setVisibility(View.GONE);
				}
			}
		});
		animations.addAnimation(translate);
		// Play the animations
		//mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);
		//Log.i("hung", "fromXfromX "+fromX+" fromTranslateX "+fromTranslateX+" toTranslateX "+toTranslateX);
		mRootLayout.startAnimation(animations);
	}

	private void animate2Destination(){
		animateRootLayout();
		animateSecondLayout();
	}



	private void animateRootLayout() {


		// Scale the distance between open and close states to 0-1. 
		int screenHeight = mAppLayout.getHeight();
		final int screenWidth = mAppLayout.getWidth();
		float fromTranslateX = 0;
		float toTranslateX = 0;
		float fromTranslateY = 0;
		float toTranslateY = 0;

		AnimationSet animations = new AnimationSet(false);
		animations.setFillAfter(true);
		animations.setDuration(ANIMATION_DURATION);

		float targetW = screenWidth;
		float currentW = (float)(mRootRelativeLayoutParams.width);
		if (mOnTop) {
			targetW = screenWidth;
			toTranslateX = mRootRelativeLayoutParams.width + mRootRelativeLayoutParams.rightMargin -screenWidth;
			toTranslateY = mRootLayout.getHeight() + mRootRelativeLayoutParams.bottomMargin - screenHeight ;

		}
		else{
			targetW = OVERLAY_WIDTH+OVERLAY_BOTTOM_MARGIN;
			targetW = OVERLAY_WIDTH;
			//currentW +=  mMargin;
			toTranslateY = mRootLayout.getHeight() + mRootRelativeLayoutParams.bottomMargin  - OVERLAY_HEIGHT - OVERLAY_BOTTOM_MARGIN ;
			toTranslateX = mRootLayout.getWidth() - OVERLAY_WIDTH +OVERLAY_BOTTOM_MARGIN;
			toTranslateX = mRootRelativeLayoutParams.width - OVERLAY_WIDTH -(OVERLAY_BOTTOM_MARGIN - mRootRelativeLayoutParams.rightMargin);

			if (mIsSlidingX) {
				// Setup animations
				float toAlpha = mClosed?0:1;
				float fromAlpha = (screenWidth - Math.abs(mLastX+OVERLAY_WIDTH-screenWidth))/(float)screenWidth;
				if (mClosed) {
					toTranslateX = mCloseOnRight?screenWidth - mLastX:-1*(mLastX+OVERLAY_WIDTH);
				}
				else{
					toTranslateX = screenWidth - OVERLAY_WIDTH - OVERLAY_BOTTOM_MARGIN - mLastX;
				}

				//fromAlpha = mCloseOnRight?Math.abs(screenWidth - mLastX)/(float)OVERLAY_WIDTH:(OVERLAY_WIDTH + mLastX)/(float)screenWidth;
				//fromAlpha = mLastAlpha;
				fromAlpha = Math.max(fromAlpha, 0);
				fromAlpha = Math.min(fromAlpha, 1);

				Animation animationAlpha = new AlphaAnimation(fromAlpha,toAlpha);
				animationAlpha.setInterpolator(new AccelerateInterpolator()); //and this
				//Log.i("hung", "animationAlpha from  "+fromAlpha+" toAlpha "+toAlpha);
				animations.addAnimation(animationAlpha);
			}
		}

		float fromY = 1;
		float toX = targetW/currentW;
		float toY = toX;
		float fromX = 1;
		if (!mIsSlidingX) {
			Animation animationScale = new ScaleAnimation(fromX, toX, fromY, toY);			
			animations.addAnimation(animationScale);
		}
		
		TranslateAnimation translate = new TranslateAnimation( fromTranslateX , toTranslateX , fromTranslateY,toTranslateY);
		translate.setAnimationListener(new Animation.AnimationListener(){
			@Override
			public void onAnimationStart(Animation arg0) {
			}           
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}           
			@Override
			public void onAnimationEnd(Animation arg0) {
				mRootLayout.clearAnimation();
				mSecondaryLayout.clearAnimation();
				updateViewLayout();
				updateBackView(true, 0,0);
				mEnableTouch = true;					
				if (mClosed) {
					//stopSelf();
					mEnableTouch = false;					
					mRootLayout.setVisibility(View.GONE);
				}
			}
		});
		
		animations.addAnimation(translate);
		// Play the animations
		mRootLayout.startAnimation(animations);
	}


	private void animateSecondLayout() {
		if (!mIsSlidingX) {
			final int screenWidth = mAppLayout.getWidth();
			
			// secondary
			AnimationSet secondAnimations = new AnimationSet(false);
			secondAnimations.setFillAfter(true);
			secondAnimations.setDuration(ANIMATION_DURATION);

			//translate
			TranslateAnimation secondTranslate = new TranslateAnimation( 0 , 0 , 0,0){

				@Override
				protected void applyTransformation(float interpolatedTime,
						Transformation t) {
					int secondDeltaMargin  =mOnTop?((int) (screenWidth*OVERLAY_HEIGHT/(float)OVERLAY_WIDTH) -  mLastY - mRootRelativeLayoutParams.height -mSecondTopMargin):MARGIN_TOP_OVERLAY+mRootRelativeLayoutParams.bottomMargin ;
					int margin = (int) (secondDeltaMargin*interpolatedTime + mSecondTopMargin);
					int destinateAlpha = mOnTop?1:0;
					float alpha = (destinateAlpha  - mSecondLastAlpha)*interpolatedTime + mSecondLastAlpha;
					updateSecondaryLayout(margin,alpha);
					Log.i("hung", "interpolatedTime   "+interpolatedTime+" margin "+margin);
					if (interpolatedTime==1) {
						mSecondaryLayout.clearAnimation();
					}
				}

		          @Override
		          public boolean willChangeBounds() {
		              return true;
		          }
			};
			
			secondAnimations.addAnimation(secondTranslate);
			mSecondaryLayout.startAnimation(secondAnimations);
		}
	}


	// Load new album cover image
	private void changeSongDisplayInfo(){
		/*
		InputStream is=null;
		try {
			is = getAssets().open(mPlaylist.getCurrentSongInfo().mAlbumCoverPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Load new bitmap
		Bitmap bmap = Utils.loadMaskedBitmap(is, mAlbumCoverLayout.getHeight(), mAlbumCoverLayout.getWidth());

		// Change backgrounds
		mAlbumCoverHelperLayout.setBackgroundDrawable(mAlbumCoverLayout.getBackground());
		mAlbumCoverLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), bmap));

		// Animate the two layouts in order to achieve the fade in/fade out effect.
		// First normalise the distance between open and closed states (0-1)
		float relativeDistance = (mRootLayoutParams.x + mLogoLayout.getWidth())/(float)
				(-mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION + mLogoLayout.getWidth());
		relativeDistance=Math.max(relativeDistance, 0);
		relativeDistance=Math.min(relativeDistance, 1);

		// Then use it to set final alpha in the animation.
		Animation fadeOutAnim = new AlphaAnimation(relativeDistance,0.f);
		fadeOutAnim.setFillAfter(true);
		fadeOutAnim.setDuration(1000);
		Animation fadeInAnim = new AlphaAnimation(0.f,relativeDistance);
		fadeInAnim.setFillAfter(true);
		fadeInAnim.setDuration(1000);
		mAlbumCoverHelperLayout.startAnimation(fadeOutAnim);
		mAlbumCoverLayout.startAnimation(fadeInAnim);

		// Set new song info
		mSongTitleView.setText(mPlaylist.getCurrentSongInfo().mTitle);
		mSingerView.setText(mPlaylist.getCurrentSongInfo().mSinger);*/
	}



	// Mock song playlist callback - Notifies the UI about song progress. 
	@Override
	public void updateSongProgress(int playheadPosition) {
		// TODO - Will be implemented later
	}

	// Mock song playlist callback - Notifies the UI to update song info. Current song has changed.
	@Override
	public void startedNextSong() {
		changeSongDisplayInfo();
	}

	// Mock song playlist callback - Provides the UI thread handler so that the callee thread could update UI
	@Override
	public Handler getHandler() {
		return new Handler();
	}

	//-------------------new update --------------------


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



	@Override
	public void onBackPressed() {
		if (mOnTop) {
			mDoAnimation = true;
			mOnTop = false;
			doAnimation();							
		}
		else{
			super.onBackPressed();
		}
	}
	
	
}