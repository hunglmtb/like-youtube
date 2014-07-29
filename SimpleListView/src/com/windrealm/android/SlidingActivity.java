package com.windrealm.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.windrealm.android.MockPlaylist.MockPlaylistListener;

public class SlidingActivity extends Activity implements MockPlaylistListener, OnItemClickListener{

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
	private static final float DRAG_MOVE_RANGE = OVERLAY_HEIGHT/2;

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
	private ListView mainListView ;
	private ArrayAdapter<String> listAdapter ;
	private RelativeLayout.LayoutParams mRootRelativeLayoutParams;
	private int mYAxis = 0;
	private int mXAxis;
	private int mLastY = 0;
	private boolean mDoAnimation = true;
	private boolean mSlidingStart = false;
	private int mTopHeigh;
	private int mBottomWidth;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_sliding);

		mainListView = (ListView) findViewById( R.id.mainListView );

		// Create and populate a List of planet names.
		String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
				"Jupiter", "Saturn", "Uranus", "Neptune"};  
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
		mainListView.setAdapter( listAdapter );  
		mainListView.setOnItemClickListener(this);



		mOverlayMode = OverlayMode.HOME_SHOW;

		// Get references to all the views and add them to root view as needed.
		mAppLayout = (RelativeLayout) findViewById(R.id.app_layout);

		/*mRootLayout = (RelativeLayout) LayoutInflater.from(this).
				inflate(R.layout.service_player, null);*/

		mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);

		mContentContainerLayout = (RelativeLayout) mRootLayout.findViewById(R.id.content_container);
		mContentContainerLayout.setOnTouchListener(new TrayTouchListener());

		//mLogoLayout = (RelativeLayout) mRootLayout.findViewById(R.id.logo_layout);
		/*mAlbumCoverLayout = (RelativeLayout) mRootLayout.findViewById(R.id.cover_layout);
		mAlbumCoverHelperLayout = (RelativeLayout) mRootLayout.findViewById(R.id.cover_helper_layout);
		mTrayOpener = (ImageView) mRootLayout.findViewById(R.id.tray_opener);
		mTrayOpenerRight = (ImageView) mRootLayout.findViewById(R.id.tray_opener_right);

		mPlayerButtonsLayout = (LinearLayout) LayoutInflater.from(this).
				inflate(R.layout.viewgroup_player_buttons, null);
		mRootLayout.addView(mPlayerButtonsLayout);

		mSongInfoLayout = (LinearLayout) LayoutInflater.from(this).
				inflate(R.layout.viewgroup_song_info, null);
		mRootLayout.addView(mSongInfoLayout);*/

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
		/*	// fully inflated once these functions execute
		mRootLayout.postDelayed(new Runnable() {
			@Override
			public void run() {

				// Reusable variables
				RelativeLayout.LayoutParams params;
				InputStream is;
				Bitmap bmap;

				//tray opener
				params = (RelativeLayout.LayoutParams) mTrayOpener.getLayoutParams();
				params.width = mRootLayoutParams.width/TRAY_HIDDEN_FRACTION;
				params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,0);
				mTrayOpener.setLayoutParams(params);
				mTrayOpener.requestLayout();

				is = getResources().openRawResource(R.drawable.spot_bg);
				int containerNewWidth = (TRAY_CROP_FRACTION-1)*mLogoLayout.getHeight()/TRAY_CROP_FRACTION;
				bmap = Utils.loadMaskedBitmap(is, mLogoLayout.getHeight(), containerNewWidth);
				params = (RelativeLayout.LayoutParams) mLogoLayout.getLayoutParams();
				params.width = (bmap.getWidth() * mLogoLayout.getHeight()) / bmap.getHeight();
				//params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
				params.addRule(RelativeLayout.RIGHT_OF,R.id.tray_opener);
				mLogoLayout.setLayoutParams(params);
				mLogoLayout.requestLayout();
				mLogoLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), bmap));

				// Setup background album cover
				is=null;
				try {
					is = getAssets().open(mPlaylist.getCurrentSongInfo().mAlbumCoverPath);
				} catch (IOException e) {
					e.printStackTrace();
				}

				bmap = Utils.loadMaskedBitmap(is, mAlbumCoverLayout.getHeight(), containerNewWidth);
				params = (RelativeLayout.LayoutParams) mAlbumCoverLayout.getLayoutParams();
				params.width = (bmap.getWidth() * mAlbumCoverLayout.getHeight()) / bmap.getHeight();
				//params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,0);
				params.addRule(RelativeLayout.RIGHT_OF,R.id.tray_opener);
				mAlbumCoverLayout.setLayoutParams(params);
				mAlbumCoverLayout.requestLayout();
				mAlbumCoverHelperLayout.setLayoutParams(params);
				mAlbumCoverHelperLayout.requestLayout();
				mAlbumCoverLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), bmap));

				// Setup playback buttons
				params = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, 
						Utils.dpToPixels(BUTTONS_DIM_Y_DP, getResources()));
				params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				params.leftMargin = mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION;
				mRootLayout.updateViewLayout(mPlayerButtonsLayout, params);

				// setup song info views
				params = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.MATCH_PARENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				//params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.tray_opener);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				int marg = Utils.dpToPixels(5, getResources());
				params.setMargins(
						marg/2 + mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION, 
						marg, 
						marg*3, 
						marg);
				mRootLayout.updateViewLayout(mSongInfoLayout, params);
				mSongTitleView.setText(mPlaylist.getCurrentSongInfo().mTitle);
				mSingerView.setText(mPlaylist.getCurrentSongInfo().mSinger);


				// Setup the root layout
				mRootLayoutParams.x = getResources().getDisplayMetrics().widthPixels -mRootLayoutParams.width;
				mRootLayoutParams.y = getApplicationContext().getResources().getDisplayMetrics().heightPixels - 2*mLogoLayout.getHeight()-PADDING;

				int screenHeight = getResources().getDisplayMetrics().heightPixels;
				mYAxis = screenHeight- OVERLAY_HEIGHT;
				updateViewLayout();

				// Make everything visible
				mRootLayout.setVisibility(View.VISIBLE);

				// Animate the Tray
				mTrayAnimationTimerTask = new TrayAnimationTimerTask();
				mTrayAnimationTimer = new Timer();
				mTrayAnimationTimer.schedule(mTrayAnimationTimerTask, 0, ANIMATION_FRAME_RATE);
			}
		}, ANIMATION_FRAME_RATE);
		 */
	}



	// Listens to the touch events on the tray.
	private class TrayTouchListener implements OnTouchListener {
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			final int action = event.getActionMasked();

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
			mBottomWidth = OVERLAY_WIDTH;

			mIsFirstTimeMove = true;
			// Cancel any currently running animations/automatic tray movements.
			if (mTrayAnimationTimerTask!=null){
				mTrayAnimationTimerTask.cancel();
				mTrayAnimationTimer.cancel();
			}

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
					int slidingMidleRangeX = mBottomWidth/2;
					int slidedRangeX = mBottomWidth/3;
					boolean leftHalf = (mStartDragX<slidingMidleRangeX&&deltaX>slidedRangeX);
					boolean rightHalf = ((mStartDragX>=slidingMidleRangeX)&&x>=mBottomWidth);
					mSlidingStart = mSlidingStart||(!mOnTop);
					
					if(mSlidingStart){
						int correctDeltaX = mStartDragX<slidingMidleRangeX?mRootLayout.getWidth()/2:mRootLayout.getWidth();
						int slideXDelata = mOnTop?correctDeltaX:slidingMidleRangeX;
						mXAxis = x - slideXDelata;
						int screenWidth = mAppLayout.getWidth();
						mXAxis = Math.min(mXAxis, screenWidth-OVERLAY_WIDTH);
						mXAxis = Math.max(mXAxis, 0);
					}
					else return;
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
					mYAxis = Math.min(mYAxis, screenHeight-OVERLAY_HEIGHT);
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
			setOverlayPlace(x,y);
			doAnimation();
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

			mYAxis=Math.min(screenHeight-OVERLAY_HEIGHT, mYAxis);
			mYAxis=Math.max(mYAxis, 0);
			if (mOnTop) {
				mYAxis=0;
			}
			else{
				mYAxis=screenHeight-OVERLAY_HEIGHT;
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

					// Update coordinates of the tray
					mRootLayoutParams.x = (2*(mRootLayoutParams.x-mDestX))/3 + mDestX;
					mRootLayoutParams.y = (2*(mRootLayoutParams.y-mDestY))/3 + mDestY;


					int screenHeigh = mAppLayout.getHeight();
					updateViewLayout();
					animateButtons();

					// Cancel animation when the destination is reached
					if (mYAxis==0 || mYAxis == (screenHeigh-OVERLAY_HEIGHT)){
						TrayAnimationTimerTask.this.cancel();
						mTrayAnimationTimer.cancel();
						if (mClosed) {
							//stopSelf();
						}
					}
					/*if (Math.abs(mRootLayoutParams.x-mDestX)<2 && Math.abs(mRootLayoutParams.y-mDestY)<2){
						TrayAnimationTimerTask.this.cancel();
						mTrayAnimationTimer.cancel();
						if (mClosed) {
							//stopSelf();
						}
					}*/
				}
			});
		}
	}

	private void updateViewLayout() {
		try {

			int screenWidth = getResources().getDisplayMetrics().widthPixels;
			int screenHeight = mAppLayout.getHeight();

			int widthn = (int) (screenWidth - (screenWidth-OVERLAY_WIDTH)*mYAxis/(float)(screenHeight-OVERLAY_HEIGHT));
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
			}

			Log.i("hung", "widthn "+widthn+ " heighn "+heighn+ " mYAxis "+mYAxis+ " margin "+margin);

			mRootLayout.setLayoutParams(mRootRelativeLayoutParams);
			mRootLayout.requestLayout();
			//mAppLayout.updateViewLayout(mRootLayout, mRootRelativeLayoutParams);

		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
		}

		float alpha = 1.0f;
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
		alpha = remain/distance;
		alpha = alpha<0.0000001?0:alpha;
		if (Build.VERSION.SDK_INT < 11) {
			final AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
			long duration = 0;
			animation.setDuration(duration );
			animation.setFillAfter(true);
			//mContentContainerLayout.startAnimation(animation);
		}else{
			//mContentContainerLayout.setAlpha(alpha);
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

		float relativeDistance = (screenHeight- mYAxis)/(float)screenHeight;

		// Limit it to 0-1 if it goes beyond 0-1 for any reason.
		relativeDistance=Math.max(relativeDistance, 0);
		relativeDistance=Math.min(relativeDistance, 1);

		// Setup animations
		float fromAlpha = relativeDistance;
		float toAlpha = 1;
		AnimationSet animations = new AnimationSet(true);
		animations.setFillAfter(true);
		long duration = 140;
		animations.setDuration(duration);
		Animation animationAlpha = new AlphaAnimation(
				fromAlpha, 
				toAlpha);
		//animations.addAnimation(animationAlpha);

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
		}
		
		float fromX = (screenWidth - (screenWidth-OVERLAY_WIDTH)*mLastY/(float)(screenHeight-OVERLAY_HEIGHT))/targetW;
		float toX = 1.0f;
		float fromY = fromX;
		float toY = 1.0f;
		
		//Log.i("hung", "relativeDistance "+relativeDistance);
		Animation animationScale = new ScaleAnimation(fromX, toX, fromY, toY);
		animations.addAnimation(animationScale);

		TranslateAnimation translate = new TranslateAnimation( marginLeft, 0 , marginTop,0 );
		animations.addAnimation(translate);
		// Play the animations
		//mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);
		mRootLayout.startAnimation(animations);
	}

	private void scaleAndTranslateLay() {

		// Scale the distance between open and close states to 0-1. 
		//float relativeDistance = (mRootLayoutParams.x + mLogoLayout.getWidth())/(float)(-mRootLayout.getWidth()/TRAY_HIDDEN_FRACTION + mLogoLayout.getWidth());

		int screenHeight = mAppLayout.getHeight();

		if (mYAxis>0&&mYAxis<screenHeight){

			float relativeDistance = (screenHeight- mYAxis)/(float)screenHeight;

			// Limit it to 0-1 if it goes beyond 0-1 for any reason.
			relativeDistance=Math.max(relativeDistance, 0);
			relativeDistance=Math.min(relativeDistance, 1);

			// Setup animations
			float fromAlpha = relativeDistance;
			float toAlpha = 1;
			AnimationSet animations = new AnimationSet(true);
			animations.setFillAfter(true);
			Animation animationAlpha = new AlphaAnimation(
					fromAlpha, 
					toAlpha);
			animations.addAnimation(animationAlpha);
			//Log.i("hung", "relativeDistance "+relativeDistance);
			Animation animationScale = new ScaleAnimation(
					relativeDistance, 
					relativeDistance, 
					relativeDistance, 
					relativeDistance);
			animations.addAnimation(animationScale);
			//mRootLayoutParams.height +=2;
			//mRootLayoutParams.width +=2;
			// Play the animations
			//mWindowManager.updateViewLayout(mRootLayout, mRootLayoutParams);
			mRootLayout.startAnimation(animations);
		}
		else{
			mRootLayout.clearAnimation();
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
		boolean previousSide = mOnTop;
		int screenHeight = mAppLayout.getHeight();
		float deltaX = x-mStartDragX;
		float deltaY = y-mStartDragY;
		if (mOnTop) {
			mOnTop = deltaY<DRAG_MOVE_RANGE;
		}
		else{
			mOnTop = (y<screenHeight-DRAG_MOVE_RANGE - OVERLAY_HEIGHT);
		}
	

	}



	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {


		if (arg2%2==0) {
			SlideToAbove();
		}
		else{
			SlideToDown();
		}

	}

	public void SlideToAbove() {
		AnimationSet animations = new AnimationSet(true);
		animations.setFillAfter(true);

		Animation slide = null;
		slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -5.0f);

		slide.setDuration(1000);
		slide.setFillAfter(true);
		slide.setFillEnabled(true);


		animations.addAnimation(slide);

		int screenHeight = mAppLayout.getHeight();

		float relativeDistance = (mRootLayout.getHeight())/(float)screenHeight;
		Animation animationScale = new ScaleAnimation(
				relativeDistance, 
				relativeDistance, 
				relativeDistance, 
				relativeDistance);
		//animations.addAnimation(animationScale);

		mRootLayout.startAnimation(animations);

		slide.setAnimationListener(new AnimationListener() {


			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				mRootLayout.clearAnimation();

				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						mRootLayout.getWidth(),  mRootLayout.getHeight());//RelativeLayout.LayoutParams.MATCH_PARENT);
				// lp.setMargins(0, 0, 0, 0);
				lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				mRootLayout.setLayoutParams(lp);

			}

		});

	}

	public void SlideToDown() {
		Animation slide = null;
		slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

		slide.setDuration(1000);
		slide.setFillAfter(true);
		slide.setFillEnabled(true);
		mRootLayout.startAnimation(slide);

		slide.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				mRootLayout.clearAnimation();

				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						mRootLayout.getWidth(), mRootLayout.getHeight());
				lp.setMargins(0, mRootLayout.getWidth(), 0, 0);
				lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				mRootLayout.setLayoutParams(lp);

			}

		});

	}
}