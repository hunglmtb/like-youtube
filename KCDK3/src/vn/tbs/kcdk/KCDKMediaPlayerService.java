package vn.tbs.kcdk;

import static vn.tbs.kcdk.global.Common.AUTHOR;
import static vn.tbs.kcdk.global.Common.BUFFERING_UPDATE_COMMAND;
import static vn.tbs.kcdk.global.Common.CONTENT_INFO;
import static vn.tbs.kcdk.global.Common.DURATION;
import static vn.tbs.kcdk.global.Common.IS_PLAYING;
import static vn.tbs.kcdk.global.Common.MEDIA_FILE_URL;
import static vn.tbs.kcdk.global.Common.MEDIA_ID;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGE_URL;
import static vn.tbs.kcdk.global.Common.MSG_REGISTER_CLIENT;
import static vn.tbs.kcdk.global.Common.MSG_UNREGISTER_CLIENT;
import static vn.tbs.kcdk.global.Common.NOTIFICATION_ID;
import static vn.tbs.kcdk.global.Common.PAUSE_PLAY_COMMAND;
import static vn.tbs.kcdk.global.Common.PAUSING;
import static vn.tbs.kcdk.global.Common.PLAYING;
import static vn.tbs.kcdk.global.Common.PLAY_PAUSE_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.REQUEST_CODE;
import static vn.tbs.kcdk.global.Common.REQUEST_CODE_PLAY;
import static vn.tbs.kcdk.global.Common.REQUEST_CODE_STOP;
import static vn.tbs.kcdk.global.Common.SEEKBAR_MAX;
import static vn.tbs.kcdk.global.Common.SEEKBAR_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.SPEAKER;
import static vn.tbs.kcdk.global.Common.START_PLAY;
import static vn.tbs.kcdk.global.Common.START_PLAY_COMMAND;
import static vn.tbs.kcdk.global.Common.STOP_COMMAND;
import static vn.tbs.kcdk.global.Common.TITLE;
import static vn.tbs.kcdk.global.Common.UI_UPDATE_COMAND;
import static vn.tbs.kcdk.global.Common.UPDATE_GUI_COMMAND;
import static vn.tbs.kcdk.global.Common.UPDATE_PROGRESS_COMMAND;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import vn.tbs.kcdk.fragments.mediaplayer.VideoControllerView.MediaPlayerControl;
import vn.tbs.kcdk.global.Common;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

public class KCDKMediaPlayerService extends Service implements OnBufferingUpdateListener, OnCompletionListener, MediaPlayerControl {
	private static final String TAG = KCDKMediaPlayer.class.getSimpleName();

	public static final int DEFAULT_DUARATION = 300000;

	private MediaPlayer mKCDKMediaPlayer = null;
	private Timer mTimer = new Timer();
	private int mMediaFileLengthInMilliseconds = 0;
	private static boolean isRunning = false;
	private Messenger mServiceMessenger = null;
	
	// Binder given to clients
    private IBinder mBinder = null;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	private IBinder mLocalBinder;

		public LocalBinder(IBinder binder) {
    		mLocalBinder = binder;
		}
		

		public IBinder getLocalBinder() {
			return mLocalBinder;
		}


		KCDKMediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return KCDKMediaPlayerService.this;
        }
    }
    
   /* @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }*/


	//	private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps
	// track of
	// all
	// current
	// registered
	// clients.

	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler()); // Target we publish for clients to

	private boolean mPostingEnable = false;
	// send messages to IncomingHandler.

	private RemoteViews mRemoteViews;

	private Notification mNotification;

	private NotificationManager mNotificationManager;

	//media
	private String mMediaTitle = "";
	private String mMediaId;
	private String mMediaFileUrl;
	private String mSpeaker;
	private String mContentInfo;
	private String mDuration;
	//	private String mMediaLinkUrl;
	private String mAuthor;
	//	private String mPublishedDate;
	//	private String mViewCount;
	//	private String mMediaImageThumbUrl;
	private String mMediaImageUrl;

	private PhoneStateListener mPhoneStateListener;

	private int mBufferPercentage = 0 ;

	private boolean isOkayState = false;

	//private boolean mNotificationShown = false;

	/**
	 * Handle incoming messages from MainActivity
	 */
	private class IncomingMessageHandler extends Handler { // Handler of
		// incoming messages
		// from clients.
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "S:handleMessage: " + msg.what);
			//mClients.add(msg.replyTo);
			mServiceMessenger = msg.replyTo;
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.add(msg.replyTo) ");
				//				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.remove(msg.replyTo) ");
				//				mClients.remove(msg.replyTo);
				break;
			case START_PLAY_COMMAND:
				//				mClients.add(msg.replyTo);
				Bundle data = msg.getData();
				mMediaFileUrl = data.getString(MEDIA_FILE_URL);
				//mMediaFileUrl = "http://download.a1.nixcdn.com/1e5b9e0574a804e212375655b7d42687/545733a2/NhacCuaTui856/Exodus-HoaTau-3089103.mp3";
				mMediaId = data.getString(MEDIA_ID);
				mMediaTitle = data.getString(TITLE);
				mSpeaker = data.getString(SPEAKER);
				mContentInfo = data.getString(CONTENT_INFO);
				mDuration = data.getString(DURATION);
				mAuthor = data.getString(AUTHOR);
				mMediaImageUrl = data.getString(MEDIA_IMAGE_URL);
				startPlayMedia();
				//incrementBy = msg.arg1;
				break;
			case PAUSE_PLAY_COMMAND:
				pauseOrPlay(msg.arg1!=0);
				if (!mKCDKMediaPlayer.isPlaying()) {
					mNotificationManager.cancel(NOTIFICATION_ID);
					//mNotificationShown  = false;
				}
				break;
			case STOP_COMMAND:
				pauseMediaPlayer(true,false);
				break;

			case UPDATE_PROGRESS_COMMAND:
				int progress = msg.arg1;
				updateProgress(progress);
				//incrementBy = msg.arg1;
				break;
			case UPDATE_GUI_COMMAND:
				sendMediaData2GUI();
				break;
			default:
				super.handleMessage(msg);
			}
			//mClients.clear();
		}

		private void updateProgress(int progress){
			int playPositionInMillisecconds = (int) ((mMediaFileLengthInMilliseconds /(float) SEEKBAR_MAX) * progress);
			mKCDKMediaPlayer.seekTo(playPositionInMillisecconds);
			int duration = mMediaFileLengthInMilliseconds;
			int currentPosition = progress*duration;
			Log.i(TAG, "hehe progress "+progress+" currentPosition "+currentPosition+ " duration "+duration +" playPositionInMillisecconds "+playPositionInMillisecconds);
			/*try {
			//sendMessageToUI(SEEKBAR_UPDATE_COMAND,currentPosition,progress);
			Log.d(TAG, "lele SEEKBAR_UPDATE_COMAND "+progress);

		} catch (Throwable t) { // you should always ultimately catch all
								// exceptions in timer tasks.
			Log.e("TimerTick", "Timer Tick Failed.", t);
		}*/
		}
	}
	@Override
	public void onCreate() {
		isOkayState = false;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mKCDKMediaPlayer = new MediaPlayer();
		mKCDKMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mKCDKMediaPlayer.setOnBufferingUpdateListener(this);
		mKCDKMediaPlayer.setOnCompletionListener(this);
		//mp3 will be started after completion of preparing...
		mKCDKMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer player) {
				isOkayState = true;
				player.start();
				int duration = mKCDKMediaPlayer.getDuration();
				duration = duration>0&&duration<900000000?duration:DEFAULT_DUARATION;
				int currentPosition = (int)mKCDKMediaPlayer.getCurrentPosition();
				int progress = (int)((currentPosition/(float)duration )*SEEKBAR_MAX);
				mMediaFileLengthInMilliseconds = duration;
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, mMediaFileLengthInMilliseconds);
				showControllerInNotification();
				Log.d("duration", "currentPosition " + currentPosition + " duration "+duration + " progress "+progress);
			}

		});
		isRunning = true;

		registerCallingState();


		super.onCreate();
	}

	private void registerCallingState() {
		mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				if (state == TelephonyManager.CALL_STATE_RINGING) {
					pause();
				} else if(state == TelephonyManager.CALL_STATE_IDLE) {
					playIfCan();
				} else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
					pause();
				}
				super.onCallStateChanged(state, incomingNumber);
			}
		};
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
			mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		}
	}

	protected void playIfCan() {
		//pauseOrPlay(true);
	}

/*	protected void pause() {
		if (mKCDKMediaPlayer!=null&&mKCDKMediaPlayer.isPlaying()) {
			pauseMediaPlayer(false,true);
		}
	}*/

	public void sendMediaData2GUI() {
		Log.d(TAG, "S:sendMediaData2GUI");
		//		Iterator<Messenger> messengerIterator = mClients.iterator();
		//		while (messengerIterator.hasNext()) {
		//			Messenger messenger = messengerIterator.next();
		try {
			// Send data as a String
			mDuration = Common.getDurationTextFromNumber(mMediaFileLengthInMilliseconds);
			Bundle  extras = new Bundle();
			extras.putString(MEDIA_ID, mMediaId);
			extras.putString(MEDIA_FILE_URL, mMediaFileUrl);
			extras.putString(TITLE, mMediaTitle);
			extras.putString(SPEAKER, mSpeaker);
			extras.putString(CONTENT_INFO, mContentInfo);
			extras.putString(DURATION, mDuration);
			extras.putString(AUTHOR, mAuthor);
			extras.putString(MEDIA_IMAGE_URL, mMediaImageUrl);
			extras.putBoolean(IS_PLAYING, mKCDKMediaPlayer.isPlaying());
			Message msg = Message.obtain(null, UPDATE_GUI_COMMAND);
			msg.setData(extras);
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			// The client is dead. Remove it from the list.
			//mClients.remove(messenger);
			e.printStackTrace();
		}
		//		}
	}

	public void pauseMediaPlayer(boolean isStop,boolean showNotification) {
		if (mKCDKMediaPlayer!=null) {
			mKCDKMediaPlayer.pause();
			if (showNotification) {
				showControllerInNotification();
			}
			sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, mMediaFileLengthInMilliseconds);
			if (isStop) {
				mNotificationManager.cancel(NOTIFICATION_ID);	
				//mNotificationShown  = false;
			}
			mPostingEnable = false;
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "S:onBind() - return mMessenger.getBinder()");
		 mBinder = new LocalBinder(mMessenger.getBinder());
		return mBinder;
	}


	@Override
	public void onDestroy() {
		Log.d(TAG, "S:onDestroy():Service Stopped");
		isOkayState = false;
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mKCDKMediaPlayer!=null) {
			pauseMediaPlayer(true, false);
			mKCDKMediaPlayer = null;
		}
		unregisterCallingState();
		//mNotificationShown  = false;
		isRunning = false;
	}


	private void unregisterCallingState() {
		TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(mgr != null) {
			mgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent!=null) {
			String action = intent.getAction();         
			if (action!=null&&action.length()>0){
				if (action.equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
					pauseMediaPlayer(true,false);
				}
				else if (action.equals(Common.ACTION_PLAY)) {
					pauseOrPlay(true);
				}else if(action.equals(Common.ACTION_PAUSE)) {
					pauseOrPlay(true);
				}else if(action.equals(Common.ACTION_STOP)) {
					pauseMediaPlayer(true,false);
				}
			}

			if (intent.getBooleanExtra(START_PLAY, false)) {
				//play();
				startPlayMedia();
			}
		}
		return Service.START_STICKY;	
	}


	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		Log.i(TAG, "onBufferingUpdate start");

		/** Method which updates the SeekBar secondary progress by current song loading from URL position*/
		//mSeekBarProgress.setSecondaryProgress(percent);
		sendMessageToUI(BUFFERING_UPDATE_COMMAND, percent,0);
		mBufferPercentage = percent;
		Log.i(TAG, "onBufferingUpdate percent "+percent);
		Log.i(TAG, "onBufferingUpdate end");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "onCompletion start");

		int current = mp.getCurrentPosition();
		boolean reached2End = current>=mMediaFileLengthInMilliseconds-500;

		if (reached2End) {
			mKCDKMediaPlayer.seekTo(1);
			pauseMediaPlayer(false,true);
		}
		Log.i(TAG, "onCompletion end");
	}

	/**
	 * Send the data to all registered clients.
	 * 
	 * @param intvaluetosend
	 *            The value to send.
	 * @param value 
	 * @param sencondValue 
	 */
	private void sendMessageToUI(int type, int value, int sencondValue) {
		Log.d(TAG, "S:sendMessageToUI");
		//		Iterator<Messenger> messengerIterator = mClients.iterator();
		//		while (messengerIterator.hasNext()) {
		//			Messenger messenger = messengerIterator.next();
		try {
			// Send data as a String
			Bundle bundle = new Bundle();
			bundle.putInt("value", value);
			bundle.putInt("type", type);
			bundle.putInt("sencondValue", sencondValue);
			Message msg = Message.obtain(null, UI_UPDATE_COMAND);
			msg.setData(bundle);
			Log.d(TAG, "S:TX MSG_SET_STRING_VALUE");
			mServiceMessenger.send(msg);

		} catch (RemoteException e) {
			// The client is dead. Remove it from the list.
			//mClients.remove(messenger);
			e.printStackTrace();
		}
		//		}
	}
	private boolean startPlayMedia() {
		Log.i(TAG, "playMedia start");

		isOkayState = false;
		//mKCDKMediaPlayer.release();
		boolean playOrPause = false;
		boolean ioError = false;
		mPostingEnable = true;

		/** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
		try {
			// setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
			//			mMediaPlayer.setDataSource(mContext.getString(R.string.mp3_url_test));
			if (mMediaFileUrl!=null&&mMediaFileUrl.length()>0) {
				mDuration = Common.getDurationTextFromNumber(mMediaFileLengthInMilliseconds);
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, mMediaFileLengthInMilliseconds);
				mKCDKMediaPlayer.reset();
				mKCDKMediaPlayer.setDataSource(mMediaFileUrl );
				// you must call this method after setup the datasource in setDataSource method.
				//After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer. 
				mKCDKMediaPlayer.prepareAsync();
				mBufferPercentage = 0;
			}
		}
		catch (IOException e) {
			ioError = true;
			e.printStackTrace();
			Log.e(TAG, "playMedia IOException "+ e);
			//			return false;
		} catch (IllegalStateException e) {
			ioError = true;
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			ioError = true;
			e.printStackTrace();
		}


		// gets the song length in milliseconds from URL
		//mMediaFileLengthInMilliseconds = mKCDKMediaPlayer.getDuration();

		//mKCDKMediaPlayer.start();
		//playOrPause = playOrPause();

		primarySeekBarProgressUpdater();

		if (ioError&&!playOrPause) {
			//Toast.makeText(mContext, "sorry ! IOException", Toast.LENGTH_SHORT).show();
		}

		Log.i(TAG, "playMedia end");

		return playOrPause;
	}

	private boolean pauseOrPlay(boolean inverse) {
		Log.i(TAG, "resumePlaying start");

		boolean result = false;
		if (mKCDKMediaPlayer!=null) {
			try {
				if(!mKCDKMediaPlayer.isPlaying()){
					//mKCDKMediaPlayer.start();
					start();
					sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, mMediaFileLengthInMilliseconds);
					showControllerInNotification();
					result =  true;
				}else {
					/*if (inverse) {
						pauseMediaPlayer(false,true);
					}*/
					pause();
					sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, mMediaFileLengthInMilliseconds);
				}
			} catch (IllegalStateException e) {
				Log.e(TAG, "playMedia IllegalStateException ");
				e.printStackTrace();
			}
		}
		Log.i(TAG, "resumePlaying end");
		return result;
	}


	/** Method which updates the SeekBar primary progress by current song playing position*/
	private void primarySeekBarProgressUpdater() {
		if (mKCDKMediaPlayer!=null&&mTimer!=null&&mPostingEnable) {
			mTimer.scheduleAtFixedRate(new MyTask(), 10, 1000L);
		}
	}

	private void showControllerInNotification() {       
		PendingIntent pendingIntent = null;
		Intent intent = null;


		//Inflate a remote view with a layout which you want to display in the notification bar.
		if (mRemoteViews == null) {
			mRemoteViews = new RemoteViews(getPackageName(),
					R.layout.player_notification);
		}   

		//String playOrPauseText = mKCDKMediaPlayer.isPlaying()?"Playing":"Pause";

		mRemoteViews.setTextViewText(R.id.player_notification_text, mMediaTitle);
		int id = mKCDKMediaPlayer.isPlaying()?R.drawable.media_pause:R.drawable.media_start;
		mRemoteViews.setImageViewResource(R.id.player_notification_play, id);
		//		Bitmap bmp = null;
		//		mRemoteViews.setBitmap(R.id.player_notification, "", bmp);
		//Define what you want to do after clicked the button in notification.
		//Here we launcher a service by an action named "ACTION_STOP" which will stop the music play.

		intent = new Intent(Common.ACTION_PLAY);       
		pendingIntent = PendingIntent.getService(getApplicationContext(),
				REQUEST_CODE_PLAY, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		//In R.layout.notification_control_bar,there is a button view identified by bar_btn_stop
		//We bind a pendingIntent with this button view so when user click the button,it will excute the intent action.
		mRemoteViews.setOnClickPendingIntent(R.id.player_notification_play,
				pendingIntent);

		intent = new Intent(Common.ACTION_STOP);   
		PendingIntent deletePendingIntent = PendingIntent.getService(getApplicationContext(),
				REQUEST_CODE_STOP, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		/*
		Intent notificationIntent = new Intent(getApplicationContext(), SmartKCDKActivity.class);

	    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

	    PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0,
	            notificationIntent, 0);

	    mNotification.setLatestEventInfo(context, title, message, pIntent);
	    mNotification.flags |= Notification.FLAG_AUTO_CANCEL;*/

		Intent notificationIntent = new Intent(getApplicationContext(), SmartKCDKActivity.class);
		notificationIntent.setAction(Common.ACTION_LAUNCH);

		Bundle  extras = new Bundle();
		extras.putString(MEDIA_ID, mMediaId);
		extras.putString(MEDIA_FILE_URL, mMediaFileUrl);
		extras.putString(TITLE, mMediaTitle);
		extras.putString(SPEAKER, mSpeaker);
		extras.putString(CONTENT_INFO, mContentInfo);
		extras.putString(DURATION, mDuration);
		extras.putString(AUTHOR, mAuthor);
		extras.putString(MEDIA_IMAGE_URL, mMediaImageUrl);
		notificationIntent.putExtras(extras);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		//Create the notification instance.
		mNotification = new NotificationCompat.Builder(getApplicationContext())
		.setSmallIcon(R.drawable.ic_launcher).setOngoing(false)
		.setWhen(System.currentTimeMillis())                
		.setContent(mRemoteViews)
		.setDeleteIntent(deletePendingIntent)
		.setContentIntent(pendingIntent)
		.build();

		//mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		//mNotification.setLatestEventInfo(getApplicationContext(), "title", "mess", pendingIntent);

		//Show the notification in the notification bar.
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);
		//mNotificationShown = true;
	}   
	/**
	 * The task to run...
	 */
	private class MyTask extends TimerTask {
		@Override
		public void run() {
			if (mKCDKMediaPlayer!=null&&mKCDKMediaPlayer.isPlaying()) {
				int duration = mKCDKMediaPlayer.getDuration();
				duration = (duration>0&&duration<1000000000)?duration:DEFAULT_DUARATION;
				int currentPosition = (int)mKCDKMediaPlayer.getCurrentPosition();
				int progress = (int)((currentPosition/(float)duration )*SEEKBAR_MAX);
				Log.d("duration", "TimerTask currentPosition " + currentPosition + " duration "+duration + " progress "+progress);
				try {
					sendMessageToUI(SEEKBAR_UPDATE_COMAND,currentPosition,progress);
					Log.d(TAG, "lele SEEKBAR_UPDATE_COMAND "+progress);

				} catch (Throwable t) { // you should always ultimately catch all
					// exceptions in timer tasks.
					Log.e("TimerTick", "Timer Tick Failed.", t);
				}

			}
		}
	}
	public KCDKMediaPlayerService getService() {
		return KCDKMediaPlayerService.this;
	}
	

    // Implement MediaPlayer.OnPreparedListener
/*    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        player.start();
    }*/
    // End MediaPlayer.OnPreparedListener

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public int getCurrentPosition() {
        return mKCDKMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
    	if (isOkayState) {
    		return mKCDKMediaPlayer.getDuration();
		}
    	else{
    		return 0;
    	}
    }

    @Override
    public boolean isPlaying() {
        return mKCDKMediaPlayer.isPlaying();
    }

    @Override
    public void pause() {
    	mKCDKMediaPlayer.pause();
    	showControllerInNotification();
    }

    @Override
    public void seekTo(int i) {
    	mKCDKMediaPlayer.seekTo(i);
    }

    @Override
    public void start() {
    	mKCDKMediaPlayer.start();
    	//sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, mMediaFileLengthInMilliseconds);
		showControllerInNotification();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        
    }
}
