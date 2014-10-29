package vn.tbs.kcdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class KCDKMediaPlayerService extends Service implements OnBufferingUpdateListener, OnCompletionListener {
	private static final String TAG = KCDKMediaPlayer.class.getSimpleName();

	public static final String ACTION_STOP="xxx.yyy.zzz.ACTION_STOP";
	public static final String ACTION_PLAY="xxx.yyy.zzz.ACTION_PLAY";
	public static final String ACTION_PAUSE="xxx.yyy.zzz.ACTION_PAUSE";

	private MediaPlayer mKCDKMediaPlayer = null;
	private boolean      isPlaying = false;
	private Timer mTimer = new Timer();
	private int mMediaFileLengthInMilliseconds = 300000;

	private String mCurrentUrl = null;
	private static boolean isRunning = false;

	private static int classID = 579; // just a number

	public static String START_PLAY = "START_PLAY";
	private static final int SEEKBAR_MAX = 100;

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int START_PLAY_COMMAND = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	public static final int PAUSE_PLAY_COMMAND = 5;
	public static final int UPDATE_PROGRESS_COMMAND = 6;
	public static final int STOP_COMMAND = 7;

	public static final int BUFFERING_UPDATE_COMMAND = 100;
	public static final int SEEKBAR_UPDATE_COMAND = 101;
	public static final int PLAY_PAUSE_UPDATE_COMAND = 102;

	public static final int UI_UPDATE_COMAND = 1000;

	public static final int PLAYING = 10;
	public static final int PAUSING = 11;

	private static final int NOTIFICATION_ID = 0;

	private static final int REQUEST_CODE_STOP = 0;

	private static final int REQUEST_CODE_PLAY = 1;


	private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps
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

	private String mMediaTitle = "";

	/**
	 * Handle incoming messages from MainActivity
	 */
	private class IncomingMessageHandler extends Handler { // Handler of
		// incoming messages
		// from clients.
		@Override
		public void handleMessage(Message msg) {
			Log.d(TAG, "S:handleMessage: " + msg.what);
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.add(msg.replyTo) ");
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.d(TAG, "S: RX MSG_REGISTER_CLIENT:mClients.remove(msg.replyTo) ");
				mClients.remove(msg.replyTo);
				break;
			case START_PLAY_COMMAND:
				Bundle data = msg.getData();
				mCurrentUrl = data.getString("url");
				mMediaTitle = data.getString("title");
				startPlayMedia();
				//incrementBy = msg.arg1;
				break;
			case PAUSE_PLAY_COMMAND:
				pauseOrPlay(msg.arg1!=0);
				if (!mKCDKMediaPlayer.isPlaying()) {
					mNotificationManager.cancel(NOTIFICATION_ID);				
				}
				break;
			case STOP_COMMAND:
				pauseMediaPlayer(true);
				break;

			case UPDATE_PROGRESS_COMMAND:
				int progress = msg.arg1;
				updateProgress(progress);
				//incrementBy = msg.arg1;
				break;
			default:
				super.handleMessage(msg);
			}
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
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mKCDKMediaPlayer = new MediaPlayer();
		mKCDKMediaPlayer.setOnBufferingUpdateListener(this);
		mKCDKMediaPlayer.setOnCompletionListener(this);
		//mp3 will be started after completion of preparing...
		mKCDKMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer player) {
				player.start();
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, 0);
				showControllerInNotification();

			}

		});
		isRunning = true;



		super.onCreate();
	}

	public void pauseMediaPlayer(boolean isStop) {
		if (mKCDKMediaPlayer!=null) {
			mPostingEnable = false;
			mKCDKMediaPlayer.pause();
			showControllerInNotification();
			sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, 0);
			if (isStop) {
				mNotificationManager.cancel(NOTIFICATION_ID);				
			}
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "S:onBind() - return mMessenger.getBinder()");

		// getBinder()
		// Return the IBinder that this Messenger is using to communicate with
		// its associated Handler; that is, IncomingMessageHandler().

		return mMessenger.getBinder();
	}


	@Override
	public void onDestroy() {
		Log.d(TAG, "S:onDestroy():Service Stopped");
		super.onDestroy();
		if (mTimer != null) {
			mTimer.cancel();
		}
		isRunning = false;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent!=null&&intent.getBooleanExtra(START_PLAY, false)) {
			//play();
			startPlayMedia();
		}

		if (intent != null) {
			String action = intent.getAction();         
			if (action!=null&&action.length()>0) {
				if (action.equals(ACTION_PLAY)) {
					pauseOrPlay(true);
				}else if(action.equals(ACTION_PAUSE)) {
					pauseOrPlay(true);
				}else if(action.equals(ACTION_STOP)) {
					pauseMediaPlayer(true);
				}
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
		Log.i(TAG, "onBufferingUpdate percent "+percent);
		Log.i(TAG, "onBufferingUpdate end");
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		Log.i(TAG, "onCompletion start");

		/** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/
		//mButtonPlayPause.setImageResource(R.drawable.media_start);

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
		Iterator<Messenger> messengerIterator = mClients.iterator();
		while (messengerIterator.hasNext()) {
			Messenger messenger = messengerIterator.next();
			try {
				// Send data as a String
				Bundle bundle = new Bundle();
				bundle.putInt("value", value);
				bundle.putInt("type", type);
				bundle.putInt("sencondValue", sencondValue);
				Message msg = Message.obtain(null, UI_UPDATE_COMAND);
				msg.setData(bundle);
				Log.d(TAG, "S:TX MSG_SET_STRING_VALUE");
				messenger.send(msg);

			} catch (RemoteException e) {
				// The client is dead. Remove it from the list.
				mClients.remove(messenger);
			}
		}
	}
	private boolean startPlayMedia() {
		Log.i(TAG, "playMedia start");

		//mKCDKMediaPlayer.release();
		boolean playOrPause = false;
		boolean ioError = false;
		mPostingEnable = true;

		/** ImageButton onClick event handler. Method which start/pause mediaplayer playing */
		try {
			// setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
			//			mMediaPlayer.setDataSource(mContext.getString(R.string.mp3_url_test));
			if (mCurrentUrl!=null&&mCurrentUrl.length()>0) {
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, 0);
				mKCDKMediaPlayer.reset();
				mKCDKMediaPlayer.setDataSource(mCurrentUrl );
				// you must call this method after setup the datasource in setDataSource method.
				//After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer. 
				mKCDKMediaPlayer.prepareAsync();				
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
		try {
			if(!mKCDKMediaPlayer.isPlaying()){
				mKCDKMediaPlayer.start();
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, 0);
				showControllerInNotification();
				result =  true;
			}else {
				if (inverse) {
					pauseMediaPlayer(false);
				}
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "playMedia IllegalStateException ");
			e.printStackTrace();
		}
		Log.i(TAG, "resumePlaying end");
		return result;
	}


	/** Method which updates the SeekBar primary progress by current song playing position*/
	private void primarySeekBarProgressUpdater() {
		if (mKCDKMediaPlayer!=null&&mTimer!=null&&mPostingEnable) {
			mTimer.scheduleAtFixedRate(new MyTask(), 0, 1000L);
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

		intent = new Intent(ACTION_PLAY);       
		pendingIntent = PendingIntent.getService(getApplicationContext(),
				REQUEST_CODE_PLAY, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		//In R.layout.notification_control_bar,there is a button view identified by bar_btn_stop
		//We bind a pendingIntent with this button view so when user click the button,it will excute the intent action.
		mRemoteViews.setOnClickPendingIntent(R.id.player_notification_play,
				pendingIntent);

		intent = new Intent(ACTION_STOP);   
		PendingIntent deletePendingIntent = PendingIntent.getService(getApplicationContext(),
				REQUEST_CODE_STOP, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		
		//Create the notification instance.
		mNotification = new NotificationCompat.Builder(getApplicationContext())
		.setSmallIcon(R.drawable.ic_launcher).setOngoing(false)
		.setWhen(System.currentTimeMillis())                
		.setContent(mRemoteViews)
		.setDeleteIntent(deletePendingIntent)
		.build();

		//Show the notification in the notification bar.
		mNotificationManager.notify(NOTIFICATION_ID, mNotification);      
	}   
	/**
	 * The task to run...
	 */
	private class MyTask extends TimerTask {
		@Override
		public void run() {
			int duration = mMediaFileLengthInMilliseconds;
			duration = (duration>0&&duration<1000000000)?duration:100000;
			int currentPosition = (int)mKCDKMediaPlayer.getCurrentPosition();
			int progress = (int)((currentPosition/(float)duration )*SEEKBAR_MAX);

			Log.d(TAG, "currentPosition " + currentPosition + " duration "+duration);
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
