package vn.tbs.kcdk.fragments.mediaplayer;

import java.io.IOException;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class KCDKMediaPlayer implements OnClickListener, OnTouchListener, OnBufferingUpdateListener, OnCompletionListener {

	private static final String TAG = KCDKMediaPlayer.class.getSimpleName();

	private static final int SEEKBAR_MAX = 100;

	private Context mContext;

	private View mMediaPlayerView;
	private ImageButton mButtonPlayPause;
	private SeekBar mSeekBarProgress;
	private TextView mDurationTextView;
	//private TextView mMediaPlayerTitle;
	private TextView mCurrentPlayingTimeTextView;

	//media player 
	private MediaPlayer mMediaPlayer;
	// this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class
	private int mMediaFileLengthInMilliseconds;

	private final Handler mHandler = new Handler();

	private String mCurrentUrl = null;


	private Runnable mUpdateSeekbarCallback;

	private MediaInfo mMediaInfoItem;

	private Runnable mPlayingCommandRunable;

	private View mMediaProgressLayout;


	public KCDKMediaPlayer(Context aContext, View mediaPlayerView) {
		this.mContext = aContext;
		this.mMediaPlayerView = mediaPlayerView;
		initMediaPlayer();
	}


	private void initMediaPlayer() {
		Log.i(TAG, "initMediaPlayer start");

		//this.mCurrentUrl = mContext.getString(R.string.mp3_url_test);
		//Log.e(TAG, "kaka initMediaPlayer mCurrentUrl "+mCurrentUrl);
		//init media player
		
		mMediaProgressLayout =  mMediaPlayerView.findViewById(R.id.media_progress_layout);
		mDurationTextView = (TextView) mMediaPlayerView.findViewById(R.id.download_duration);
		mCurrentPlayingTimeTextView = (TextView) mMediaPlayerView.findViewById(R.id.download_position);

		//mMediaPlayerTitle = (TextView)mMediaPlayerView.findViewById(R.id.media_player_title_text);
		//mMediaPlayerTitle.setOnClickListener(this);

		mButtonPlayPause = (ImageButton)mMediaPlayerView.findViewById(R.id.download_start);
		mButtonPlayPause.setOnClickListener(this);

		mSeekBarProgress = (SeekBar)mMediaPlayerView.findViewById(R.id.download_progress_bar);	
		mSeekBarProgress.setMax(SEEKBAR_MAX-1); // It means 100% .0-99
		mSeekBarProgress.setOnTouchListener(this);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);

		Log.i(TAG, "initMediaPlayer end");

	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.i(TAG, "onBufferingUpdate start");

		/** Method which updates the SeekBar secondary progress by current song loading from URL position*/
		mSeekBarProgress.setSecondaryProgress(percent);

		Log.i(TAG, "onBufferingUpdate end");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "onCompletion start");

		/** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
		mButtonPlayPause.setImageResource(R.drawable.media_start);

		Log.i(TAG, "onCompletion end");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.i(TAG, "onTouch start");

		if (event.getAction()== MotionEvent.ACTION_UP) {
			if(v.getId() == R.id.download_progress_bar){
				/** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
				SeekBar sb = (SeekBar)v;
				int playPositionInMillisecconds = (mMediaFileLengthInMilliseconds / SEEKBAR_MAX) * sb.getProgress();
				mMediaPlayer.seekTo(playPositionInMillisecconds);
				
				if(!mMediaPlayer.isPlaying()){
					doPlay();
				}
			}
		}

		Log.i(TAG, "onTouch end");
		return false;

	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick start");
		switch (v.getId()) {
		case  R.id.download_start:
			playMedia();			
			break;

		case  R.id.media_player_title_text:
			//TODO
			//Common.showMediaPage(mMediaInfoItem,mKCDKActivity);
			break;

		default:
			break;
		}

		Log.i(TAG, "onClick end");
	}

	private boolean playMedia() {
		Log.i(TAG, "playMedia start");
		boolean playOrPause = false;
		boolean ioError = false;

		/** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
		try {
			// setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
			//			mMediaPlayer.setDataSource(mContext.getString(R.string.mp3_url_test));
			if (mCurrentUrl!=null&&mCurrentUrl.length()>0) {
				mMediaPlayer.setDataSource(mCurrentUrl );
				// you must call this method after setup the datasource in setDataSource method.
				//After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer. 
				mMediaPlayer.prepare();				
			}
		}
		catch (IOException e) {
			ioError = true;
			e.printStackTrace();
			Log.e(TAG, "playMedia IOException "+ e);
//			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "playMedia Exception "+ e);
		}

		// gets the song length in milliseconds from URL
		mMediaFileLengthInMilliseconds = mMediaPlayer.getDuration();

		playOrPause = playOrPause();

		primarySeekBarProgressUpdater();

		if (ioError&&!playOrPause) {
			Toast.makeText(mContext, "sorry ! IOException", Toast.LENGTH_SHORT).show();
		}

		Log.i(TAG, "playMedia end");

		return playOrPause;
	}


	private boolean playOrPause() {
		Log.i(TAG, "resumePlaying start");

		if(!mMediaPlayer.isPlaying()){
			doPlay();
			return true;
		}else {
			mMediaPlayer.pause();
			mButtonPlayPause.setImageResource(R.drawable.media_start);
		}

		Log.i(TAG, "resumePlaying end");
		return false;
	}


	private void doPlay() {
		Log.i(TAG, "doPlay start");

		mMediaPlayer.start();
		mButtonPlayPause.setImageResource(R.drawable.media_pause);

		Log.i(TAG, "doPlay end");
	}


	/** Method which updates the SeekBar primary progress by current song playing position*/
	private void primarySeekBarProgressUpdater() {
		if (mMediaPlayer!=null) {
			int progress = (int)(((float)mMediaPlayer.getCurrentPosition()/mMediaFileLengthInMilliseconds)*SEEKBAR_MAX);
			updateCurrentPlayingTime();

			mSeekBarProgress.setProgress(progress ); // This math construction give a percentage of "was playing"/"song length"
			if (mMediaPlayer.isPlaying()) {
				mUpdateSeekbarCallback = new Runnable() {
					public void run() {
						primarySeekBarProgressUpdater();
					}
				};
				mHandler.postDelayed(mUpdateSeekbarCallback,1000);
			}
		}
	}


	protected void updateCurrentPlayingTime() {
		Log.i(TAG, "updateCurrentPlayingTime start");
		int mCurrentPlayingTime = Math.round((float)mMediaPlayer.getCurrentPosition()/1000);
		int m = mCurrentPlayingTime/60;
		int s = mCurrentPlayingTime%60;
		String newTime = m+":"+String.format("%02d", s);

		mCurrentPlayingTimeTextView.setText(newTime);

		Log.i(TAG, "updateCurrentPlayingTime end");
	}


	public boolean playMedia(final String url) {
		Log.i(TAG, "playMedia with url  start");
		Log.e(TAG, "kaka initMediaPlayer mCurrentUrl "+url);
		if (url!=null&&url.length()>0) {
			if (!url.equals(mCurrentUrl)) {
				mPlayingCommandRunable = new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "mPlayingCommandRunable run start");
						
						mMediaPlayer.reset();
						mCurrentUrl = url;
						playMedia();
						 
						Log.i(TAG, "mPlayingCommandRunable run end");
					}
				};
				mHandler.postDelayed(mPlayingCommandRunable,500);				
				return true;
			}
			//case url is currently being played
			else{
				//pause then play
				if (!mMediaPlayer.isPlaying()) {
					return playMedia();
				}

			}
		}

		Log.i(TAG, "playMedia with url  end");
		return false;

	}


	public void playMedia(MediaInfo item) {
		Log.i(TAG, "playMedia with MediaInfo  start");

		if (item!=null) {
			this.mMediaInfoItem = item;
			String url = mContext.getString(R.string.action_url)+item.getMediaFileUrl();
			//String url = "http://stream2.r15s91.vcdn.vn/fsfsdfdsfdserwrwq3/6de9da3107e057671ecb386c5c8bb797/539814e6/2013/12/15/4/b/4b896ff9151263672609e9cb9cc04c00.mp3";
			
			boolean ok = playMedia(url );
			ok = true;
			if (ok) {
				updateMediaInfo(item);
			}
		}

		Log.i(TAG, "playMedia with MediaInfo  end");
	}


	private void updateMediaInfo(MediaInfo item) {
		Log.i(TAG, "updateMediaInfo start");

		mDurationTextView.setText(item.getDuration());
//		mMediaPlayerTitle.setText(item.getTitle());
		
		Log.i(TAG, "updateMediaInfo end");
	}


	public void stop() {
		Log.i(TAG, "stop start");
		if (mMediaPlayer!=null) {
			mHandler.removeCallbacks(mUpdateSeekbarCallback);
			mMediaPlayer.reset();
			mMediaPlayer.stop();
			mMediaPlayer = null;
		}

		Log.i(TAG, "stop end");
	}


	public void updateView(boolean showProgressLayout) {
		// TODO Auto-generated method stub
		mMediaPlayerView.setVisibility(showProgressLayout?View.VISIBLE:View.GONE);
	}


	public int getSimpleModeHeight() {
		return mMediaProgressLayout!=null?mMediaProgressLayout.getHeight():150;
	}

}
