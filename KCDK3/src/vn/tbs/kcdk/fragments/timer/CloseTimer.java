package vn.tbs.kcdk.fragments.timer;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class CloseTimer {
	private static final String TAG = CloseTimer.class.getSimpleName();
	private static final long INTERVAL = 1000;
	private TextView mTimeStatus;
	private int mRemainSeconds;
	private CountDownTimer mTimer;
	private OnFinishListener mOnFinishListener;

	public interface OnFinishListener{
		public void doFinish(); 
	}

	public CloseTimer(TextView mTimeStatus, int mRemainSeconds) {
		this.mTimeStatus = mTimeStatus;
		this.mRemainSeconds = mRemainSeconds;
	}


	public void setOnFinishListener(OnFinishListener mOnFinishListener) {
		this.mOnFinishListener = mOnFinishListener;
	}


	public void setTimeStatus(TextView mTimeStatus) {
		this.mTimeStatus = mTimeStatus;
	}


	private void setTimeText(int duration) {
		Log.i(TAG, "setTimeText start");

		if (duration>0) {
			int hours = duration/3600;
			int mins = (duration/60)%60;
			int seconds = (duration%60);
			if (mTimeStatus!=null) {
				mTimeStatus.setVisibility(View.VISIBLE);
				String text ="";
				if (hours>0) {
					text = hours+":"+mins+":"+seconds;
				}
				else{
					text = mins+":"+seconds;
				}
				mTimeStatus.setText(text);
			}
		}

		Log.i(TAG, "setTimeText end");
	}


	public void startTimer(int duration) {
		Log.i(TAG, "startTimer start");

		cancelTimer();
		this.mRemainSeconds = duration;
		runTimer();

		Log.i(TAG, "startTimer end");
	}


	private void runTimer() {
		Log.i(TAG, "runTimer start");


		mTimer = new CountDownTimer(mRemainSeconds*INTERVAL, INTERVAL) {

			@Override
			public void onFinish() {
				Log.i(TAG, "onFinish");
				if (mOnFinishListener!=null) {
					mOnFinishListener.doFinish();
				}
				mRemainSeconds= -1;
			}
			@Override
			public void onTick(long millisUntilFinished) {
				mRemainSeconds = (int) (millisUntilFinished/1000);
				setTimeText(mRemainSeconds);

			}

		};

		mTimer.start();

		Log.i(TAG, "runTimer end");
	}


	public void cancelTimer() {
		if (mTimer!=null) {
			mTimer.cancel();
		}
		mRemainSeconds = -1;
	}


	public boolean isRunning() {
		return mRemainSeconds>0;

	}


	public int getRemainTime() {
		return mRemainSeconds;
	}

}
