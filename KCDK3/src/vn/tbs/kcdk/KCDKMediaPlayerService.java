package vn.tbs.kcdk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import android.app.Service;
import android.content.Intent;
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

public class KCDKMediaPlayerService extends Service implements OnBufferingUpdateListener, OnCompletionListener {
	private static final String TAG = KCDKMediaPlayer.class.getSimpleName();
	
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

	public static final int BUFFERING_UPDATE_COMMAND = 100;
	public static final int SEEKBAR_UPDATE_COMAND = 101;
	public static final int PLAY_PAUSE_UPDATE_COMAND = 102;
	
	public static final int UI_UPDATE_COMAND = 1000;

	public static final int PLAYING = 10;
	public static final int PAUSING = 11;

	private List<Messenger> mClients = new ArrayList<Messenger>(); // Keeps
	// track of
	// all
	// current
	// registered
	// clients.
	
	private final Messenger mMessenger = new Messenger(
			new IncomingMessageHandler()); // Target we publish for clients to
											// send messages to IncomingHandler.

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
				startPlayMedia();
				//incrementBy = msg.arg1;
				break;
			case PAUSE_PLAY_COMMAND:
				pauseOrPlay();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	@Override
	public void onCreate() {
		mKCDKMediaPlayer = new MediaPlayer();
		mKCDKMediaPlayer.setOnBufferingUpdateListener(this);
		mKCDKMediaPlayer.setOnCompletionListener(this);
		 //mp3 will be started after completion of preparing...
		mKCDKMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

           @Override
           public void onPrepared(MediaPlayer player) {
               player.start();
               sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, 0);
				
           }

       });
		isRunning = true;
		
		

		super.onCreate();
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
		if (intent.getBooleanExtra(START_PLAY, false)) {
			//play();
			startPlayMedia();
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
		mMediaFileLengthInMilliseconds = mKCDKMediaPlayer.getDuration();

		//mKCDKMediaPlayer.start();
		//playOrPause = playOrPause();

		primarySeekBarProgressUpdater();

		if (ioError&&!playOrPause) {
			//Toast.makeText(mContext, "sorry ! IOException", Toast.LENGTH_SHORT).show();
		}

		Log.i(TAG, "playMedia end");

		return playOrPause;
	}
	
	private boolean pauseOrPlay() {
		Log.i(TAG, "resumePlaying start");

		try {
			if(!mKCDKMediaPlayer.isPlaying()){
				mKCDKMediaPlayer.start();
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PLAYING, 0);
				return true;
			}else {
				mKCDKMediaPlayer.pause();
				sendMessageToUI(PLAY_PAUSE_UPDATE_COMAND, PAUSING, 0);
				//mButtonPlayPause.setImageResource(R.drawable.media_start);
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, "playMedia IllegalStateException ");
			e.printStackTrace();
		}
		Log.i(TAG, "resumePlaying end");
		return false;
	}
	

	/** Method which updates the SeekBar primary progress by current song playing position*/
	private void primarySeekBarProgressUpdater() {
		if (mKCDKMediaPlayer!=null&&mTimer!=null) {
			mTimer.scheduleAtFixedRate(new MyTask(), 0, 1000L);
		}
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
