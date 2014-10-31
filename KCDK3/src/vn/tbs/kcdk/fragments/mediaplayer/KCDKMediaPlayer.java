package vn.tbs.kcdk.fragments.mediaplayer;

import static vn.tbs.kcdk.global.Common.AUTHOR;
import static vn.tbs.kcdk.global.Common.BUFFERING_UPDATE_COMMAND;
import static vn.tbs.kcdk.global.Common.CONTENT_INFO;
import static vn.tbs.kcdk.global.Common.DURATION;
import static vn.tbs.kcdk.global.Common.MEDIA_FILE_URL;
import static vn.tbs.kcdk.global.Common.MEDIA_ID;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGE_URL;
import static vn.tbs.kcdk.global.Common.MSG_REGISTER_CLIENT;
import static vn.tbs.kcdk.global.Common.MSG_SET_STRING_VALUE;
import static vn.tbs.kcdk.global.Common.PAUSE_PLAY_COMMAND;
import static vn.tbs.kcdk.global.Common.PLAYING;
import static vn.tbs.kcdk.global.Common.PLAY_PAUSE_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.SEEKBAR_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.SPEAKER;
import static vn.tbs.kcdk.global.Common.START_PLAY_COMMAND;
import static vn.tbs.kcdk.global.Common.STOP_COMMAND;
import static vn.tbs.kcdk.global.Common.TITLE;
import static vn.tbs.kcdk.global.Common.UI_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.UPDATE_GUI_COMMAND;
import static vn.tbs.kcdk.global.Common.UPDATE_PROGRESS_COMMAND;
import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class KCDKMediaPlayer implements OnClickListener, OnTouchListener, OnBufferingUpdateListener, OnCompletionListener{


	private static final String TAG = KCDKMediaPlayer.class.getSimpleName();
	private Messenger mServiceMessenger = null;

	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler());
	public ItemSelectionListener mUpdateMediaDetailListener;

	/**
	 * Handle incoming messages from TimerService
	 */
	private class IncomingMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "C:IncomingHandler:handleMessage");
			switch (msg.what) {
			case UI_UPDATE_COMAND:
				Log.d(TAG, "C: RX MSG_SET_INT_VALUE");
				//textIntValue.setText("Int Message: " + msg.arg1);
				Bundle data = msg.getData();
				int type = data.getInt("type");
				int value = data.getInt("value");
				int sencondValue = data.getInt("sencondValue");
				if (type==BUFFERING_UPDATE_COMMAND&&mSeekBarProgress!=null) {
					mSeekBarProgress.setSecondaryProgress(value);	
				}
				if (type==SEEKBAR_UPDATE_COMAND) {
					mSeekBarProgress.setProgress(sencondValue);
					updateCurrentPlayingTime(value);
					Log.d(TAG, "lala "+value);
				}
				if (type==PLAY_PAUSE_UPDATE_COMAND) {
					int iconResource = value==PLAYING?R.drawable.media_pause:R.drawable.media_start;
					mButtonPlayPause.setImageResource(iconResource);
				}
				break;
			case MSG_SET_STRING_VALUE:
				String str1 = msg.getData().getString("str1");
				Log.d(TAG, "C:RX MSG_SET_STRING_VALUE");
				//textStrValue.setText("Str Message: " + str1);
				break;
			case UPDATE_GUI_COMMAND:
				Log.d(TAG, "C: RX MSG_SET_INT_VALUE");
				//textIntValue.setText("Int Message: " + msg.arg1);
				Bundle extras = msg.getData();
				mCurrentMediaFileUrl = extras.getString(MEDIA_FILE_URL);
				if(mUpdateMediaDetailListener!=null){
					MediaInfo item = new MediaInfo(extras);
					mUpdateMediaDetailListener.doItemSelection(item);
				}
				break;
				
			default:
				super.handleMessage(msg);
			}
		}
	}

	private static final int SEEKBAR_MAX = 100;

	private Context mContext;

	private View mMediaPlayerView;
	private ImageButton mButtonPlayPause;
	private SeekBar mSeekBarProgress;
	private TextView mDurationTextView;
	//private TextView mMediaPlayerTitle;
	private TextView mCurrentPlayingTimeTextView;
	private MediaInfo mMediaInfoItem;
	private View mMediaProgressLayout;

	private String mCurrentMediaFileUrl = null;

	

	public void setOnUpdateMediaDetailListener(
			ItemSelectionListener mUpdateMediaDetailListener) {
		this.mUpdateMediaDetailListener = mUpdateMediaDetailListener;
	}


	public void setServiceMessenger(Messenger aServiceMessenger) {
		this.mServiceMessenger = aServiceMessenger;
	}


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

		/*mKCDKMediaPlayer = new MediaPlayer();
		mKCDKMediaPlayer.setOnBufferingUpdateListener(this);
		mKCDKMediaPlayer.setOnCompletionListener(this);
		 */
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
				int progress = sb.getProgress();
				Log.i(TAG, "hehe progress "+progress);
				sendMessageToService(UPDATE_PROGRESS_COMMAND,progress);
				//int playPositionInMillisecconds = (mMediaFileLengthInMilliseconds / SEEKBAR_MAX) * sb.getProgress();
				//mKCDKMediaPlayer.seekTo(playPositionInMillisecconds);

				/*if(!mKCDKMediaPlayer.isPlaying()){
					doPlay();
				}*/
			}
		}

		Log.i(TAG, "onTouch end");
		return false;

	}

	private void sendMessageToService(int command, int value) {
		if (mServiceMessenger != null) {
			try {
				Message msg = Message.obtain(null,command, value, 0);
				msg.replyTo = mMessenger;
				mServiceMessenger.send(msg);
				Log.i(TAG, "sendMessageToService  send message");
			} catch (RemoteException e) {
				Log.e(TAG, "sendMessageToService RemoteException handle");
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick start");
		switch (v.getId()) {
		case  R.id.download_start:
			pauseOrPlay(true);
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

	private void pauseOrPlay(boolean fromClick) {
		if (mServiceMessenger != null) {
			try {
				int from = fromClick?1:0;
				Message msg = Message.obtain(null,
						PAUSE_PLAY_COMMAND, from, 0);
				msg.replyTo = mMessenger;
				mServiceMessenger.send(msg);
				Log.i(TAG, "pauseOrPlay  send message");

			} catch (RemoteException e) {
				Log.e(TAG, "pauseOrPlay RemoteException handle");
				e.printStackTrace();
			}
		}
	}


	private boolean startPlayMedia() {
		Log.i(TAG, "playMedia start");

		if (mServiceMessenger != null&&mMediaInfoItem!=null) {
			try {
				int intvaluetosend = 10;
				Message msg = Message.obtain(null,
						START_PLAY_COMMAND, intvaluetosend, 0);
				msg.replyTo = mMessenger;
				Bundle extras = new Bundle();
				extras.putString(MEDIA_FILE_URL, mCurrentMediaFileUrl);
				extras.putString(TITLE, mMediaInfoItem.getTitle());
				extras.putString(MEDIA_ID, mMediaInfoItem.getMediaId());
				extras.putString(TITLE, mMediaInfoItem.getTitle());
				extras.putString(SPEAKER, mMediaInfoItem.getSpeaker());
				extras.putString(CONTENT_INFO, mMediaInfoItem.getContentInfo());
				extras.putString(DURATION, mMediaInfoItem.getDuration());
				extras.putString(AUTHOR, mMediaInfoItem.getAuthor());
				extras.putString(MEDIA_IMAGE_URL, mMediaInfoItem.getMediaImageUrl());
				msg.setData(extras);
				mServiceMessenger.send(msg);
				Log.i(TAG, "playMedia send message");

			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException handle");
				e.printStackTrace();
			}
			Log.i(TAG, "playMedia send message end");
		}

		return true;
		/*boolean playOrPause = false;
		boolean ioError = false;

		 *//** ImageButton onClick event handler. Method which start/pause mediaplayer playing *//*
		try {
			// setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
			//			mMediaPlayer.setDataSource(mContext.getString(R.string.mp3_url_test));
			if (mCurrentUrl!=null&&mCurrentUrl.length()>0) {
				mKCDKMediaPlayer.setDataSource(mCurrentUrl );
				// you must call this method after setup the datasource in setDataSource method.
				//After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer. 
				mKCDKMediaPlayer.prepare();				
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
		mMediaFileLengthInMilliseconds = mKCDKMediaPlayer.getDuration();

		playOrPause = playOrPause();

		primarySeekBarProgressUpdater();

		if (ioError&&!playOrPause) {
			Toast.makeText(mContext, "sorry ! IOException", Toast.LENGTH_SHORT).show();
		}

		Log.i(TAG, "playMedia end");

		return playOrPause;
		  */
	}


	/*	private boolean playOrPause() {
		Log.i(TAG, "resumePlaying start");

		if(!mKCDKMediaPlayer.isPlaying()){
			doPlay();
			return true;
		}else {
			//mKCDKMediaPlayer.pause();
			mButtonPlayPause.setImageResource(R.drawable.media_start);
		}

		Log.i(TAG, "resumePlaying end");
		return false;
	}*/


	private void doPlay() {
		Log.i(TAG, "doPlay start");

		//mKCDKMediaPlayer.start();
		mButtonPlayPause.setImageResource(R.drawable.media_pause);

		Log.i(TAG, "doPlay end");
	}
	/*

	 *//** Method which updates the SeekBar primary progress by current song playing position
	 * @param aCurrentPlayingTime *//*
	private void primarySeekBarProgressUpdater() {
		if (mKCDKMediaPlayer!=null) {
			float duration = mMediaFileLengthInMilliseconds;
			duration = duration>0?duration:300000;
			int progress = (int)(((float)mKCDKMediaPlayer.getCurrentPosition()/duration )*SEEKBAR_MAX);
			updateCurrentPlayingTime();

			mSeekBarProgress.setProgress(progress ); // This math construction give a percentage of "was playing"/"song length"
			if (mKCDKMediaPlayer.isPlaying()) {
				mUpdateSeekbarCallback = new Runnable() {
					public void run() {
						primarySeekBarProgressUpdater();
					}
				};
				mHandler.postDelayed(mUpdateSeekbarCallback,1000);
			}
		}
	}
	  */

	protected void updateCurrentPlayingTime(int aCurrentPlayingTime) {
		Log.i(TAG, "updateCurrentPlayingTime start");
		aCurrentPlayingTime = Math.round((float)aCurrentPlayingTime/1000);
		int m = aCurrentPlayingTime/60;
		int s = aCurrentPlayingTime%60;
		String newTime = m+":"+String.format("%02d", s);

		mCurrentPlayingTimeTextView.setText(newTime);

		Log.i(TAG, "updateCurrentPlayingTime end");
	}


	public boolean playMedia(final String url, boolean closed) {
		Log.i(TAG, "playMedia with url  start");
		Log.e(TAG, "kaka initMediaPlayer mCurrentUrl "+url);
		if (url!=null&&url.length()>0) {
			if (!url.equals(mCurrentMediaFileUrl)||closed) {
				/*mPlayingCommandRunable = new Runnable() {
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
				 */
				//mKCDKMediaPlayer.reset();
				mCurrentMediaFileUrl = url;
				startPlayMedia();
				return true;
			}
			//case url is currently being played
			else{
				//pause then play or vice versa
				pauseOrPlay(false);

			}
		}

		Log.i(TAG, "playMedia with url  end");
		return false;

	}


	public void playMedia(MediaInfo item, boolean closed) {
		Log.i(TAG, "playMedia with MediaInfo  start");

		if (item!=null) {
			this.mMediaInfoItem = item;
			String url = mContext.getString(R.string.action_url)+item.getMediaFileUrl();
			//String url = "http://stream2.r15s91.vcdn.vn/fsfsdfdsfdserwrwq3/6de9da3107e057671ecb386c5c8bb797/539814e6/2013/12/15/4/b/4b896ff9151263672609e9cb9cc04c00.mp3";
			//url = "http://download.a2.nixcdn.com/f140e6b9dc70829347640ed2a279f9c0/543b4b67/NhacCuaTui149/CoHangXom-QuangLe_33pwh.mp3";
			boolean ok = playMedia(url,closed);
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

	/*
	public void stop() {
		Log.i(TAG, "stop start");
		if (mKCDKMediaPlayer!=null) {
			mHandler.removeCallbacks(mUpdateSeekbarCallback);
			mKCDKMediaPlayer.reset();
			mKCDKMediaPlayer.stop();
			mKCDKMediaPlayer = null;
		}

		Log.i(TAG, "stop end");
	}
	 */

	public void updateView(boolean showProgressLayout,boolean showPlayControl) {
		// TODO Auto-generated method stub
		mMediaProgressLayout.setVisibility(showProgressLayout?View.VISIBLE:View.GONE);
		mButtonPlayPause.setVisibility(showPlayControl?View.VISIBLE:View.INVISIBLE);
		//mMediaPlayerView.setVisibility(showProgressLayout?View.VISIBLE:View.GONE);
	}


	public int getSimpleModeHeight() {
		return mMediaProgressLayout!=null?mMediaProgressLayout.getHeight():150;
	}


	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	public void initServiceMessenger(IBinder service) {
		mServiceMessenger = new Messenger(service);
		//textStatus.setText("Attached.");
		Toast.makeText(mContext, "Attached", Toast.LENGTH_SHORT).show();
		try {
			Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
			msg.replyTo = mMessenger;
			Log.d(TAG, "C: TX MSG_REGISTER_CLIENT");
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do
			// anything with it
			Log.e(TAG, "RemoteException at initServiceMessenger");
			e.printStackTrace();
		}
	}


	public void stopMediaPlayer() {
		sendMessageToService(STOP_COMMAND, 0);
	}


	public void requestUpdateGUI() {
		sendMessageToService(UPDATE_GUI_COMMAND,0);
	}


}
