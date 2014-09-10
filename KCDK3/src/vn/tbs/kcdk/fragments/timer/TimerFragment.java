package vn.tbs.kcdk.fragments.timer;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.timer.CloseTimer.OnFinishListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

public class TimerFragment extends Fragment implements OnClickListener, OnCheckedChangeListener{
	private static final String TAG = TimerFragment.class.getSimpleName();

	private View mDoneButton;
	private TextView mTimeStatus;
	private View mTimeOnLayout;
	private TimePicker mTimePicker;
	private ToggleButton mTimeOnOff;
	private View mTimePickerLayout;
	private View mTimeReset;
	private View m10Mins;
	private View mMore;
	private View m15Mins;
	private View m1H;
	private View m20Mins;
	private View m30Mins;
	private View m45Mins;

	private CloseTimer mTimer = null;

	private OnFinishListener finishListener;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TODO comment and rename for merge code
		//mTimer = KCDKActivity.sTimer;
		mTimer = null;
		try {
			finishListener = (OnFinishListener) getActivity();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.timer_layout, null);
		mDoneButton = view.findViewById(R.id.time_done);
		mTimeStatus = (TextView) view.findViewById(R.id.time_label);
		mTimeOnLayout = view.findViewById(R.id.time_on_layout);
		mTimeOnOff = (ToggleButton) view.findViewById(R.id.time_on_off);
		mTimePicker = (TimePicker) view.findViewById(R.id.time_picker);
		mTimePickerLayout = view.findViewById(R.id.time_picker_layout);
		mTimeReset = view.findViewById(R.id.time_reset);
		m10Mins = view.findViewById(R.id.time_10mins);
		m15Mins = view.findViewById(R.id.time_15mins);
		m1H = view.findViewById(R.id.time_1h);
		m20Mins = view.findViewById(R.id.time_20mins);
		m30Mins = view.findViewById(R.id.time_30mins);
		m45Mins = view.findViewById(R.id.time_45mins);
		mMore = view.findViewById(R.id.time_more);

		//hide some view
		mTimeOnLayout.setVisibility(View.GONE);
		mTimePickerLayout.setVisibility(View.GONE);

		//set some properties
		mTimePicker.setIs24HourView(true);
		
		boolean onoff = mTimer!=null&&mTimer.isRunning();
		mTimeStatus.setVisibility(View.INVISIBLE);			
		mTimeOnOff.setChecked(!onoff);
		if (onoff) {
			mTimer.setTimeStatus(mTimeStatus);
		}
		
		//handle click event
		mTimeOnOff.setOnCheckedChangeListener(this);

		mMore.setOnClickListener(this);
		mDoneButton.setOnClickListener(this);
		mTimeReset.setOnClickListener(this);
		m10Mins.setOnClickListener(this);
		m15Mins.setOnClickListener(this);
		m20Mins.setOnClickListener(this);
		m30Mins.setOnClickListener(this);
		m45Mins.setOnClickListener(this);
		m1H.setOnClickListener(this);
		mTimeStatus.setOnClickListener(this);

		return view;
	}


	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.time_more:
			if (mTimePickerLayout!=null) {
				mTimePickerLayout.setVisibility(View.VISIBLE);
			}
			mMore.setVisibility(View.GONE);
			break;

		case R.id.time_done:
			int duration = getRemainTimeDuration();
			startTimer(duration);
			break;

		case R.id.time_10mins:
			startTimer(600);
			break;
		case R.id.time_15mins:
			startTimer(900);
			break;
		case R.id.time_20mins:
			startTimer(1200);
			break;
		case R.id.time_30mins:
			startTimer(1800);
			break;
		case R.id.time_45mins:
			startTimer(2700);
			break;
		case R.id.time_1h:
			startTimer(3600);

		case R.id.time_reset:
			setTimer();
			break;

		case R.id.time_label:
			editTimer();
			break;
		default:
			break;
		}

	}

	private void editTimer() {
		Log.i(TAG, "editTimer start");

		showPicker();
		setTimer();
		Log.i(TAG, "editTimer end");
	}

	private void setTimer() {
		Log.i(TAG, "setTimer start");

		int seconds = mTimer!=null?mTimer.getRemainTime():-1;
		
		setPickerTime(seconds);

		Log.i(TAG, "setTimer end");
	}

	private void showPicker() {
		Log.i(TAG, "showPicker start");

		if (mTimeOnLayout.getVisibility()==View.GONE) {
			mTimePickerLayout.setVisibility(View.GONE);
			mMore.setVisibility(View.VISIBLE);			
		}
		
		if (mTimeOnLayout!=null) {
			mTimeOnLayout.setVisibility(View.VISIBLE);
		}

		Log.i(TAG, "showPicker end");
	}

	private void startTimer(int duration) {
		Log.i(TAG, "startTimer start");

		setPickerTime(duration);
		showStatus(duration);
		startTimeCount(duration);

		Log.i(TAG, "startTimer end");
	}

	private void setPickerTime(int seconds) {
		Log.i(TAG, "setPickerTime start");
		
		if (seconds>0) {
			Integer hours = seconds/3600;
			Integer mins = (seconds/60)%60;
			mTimePicker.setCurrentHour(hours);
			mTimePicker.setCurrentMinute(mins);
		}
		else{
			mTimePicker.setCurrentHour(0);
			mTimePicker.setCurrentMinute(10);	
		}

		
		Log.i(TAG, "setPickerTime end");
	}

	private void startTimeCount(int duration) {
		Log.i(TAG, "startTimeCount start");

		if (mTimer!=null) {
			mTimer.setTimeStatus(mTimeStatus);
		}
		else{
			mTimer = new CloseTimer(mTimeStatus,duration);
			//TODO comment for merge code
			//KCDKActivity.sTimer = mTimer;
		}
		mTimer.setOnFinishListener(finishListener);
		mTimer.startTimer(duration);

		Log.i(TAG, "startTimeCount end");
	}

	private int getRemainTimeDuration() {
		Log.i(TAG, "getRemainTimeDuration start");

		if (mTimePicker!=null) {
			return mTimePicker.getCurrentHour()*3600+mTimePicker.getCurrentMinute()*60;
		}
		return 0;

	}

	private void showStatus(int duration) {
		if (mTimeOnLayout!=null) {
			mTimeOnLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Log.i(TAG, "onCheckedChanged isChecked "+isChecked);

		if (mTimeOnLayout!=null) {
			mTimeOnLayout.setVisibility(isChecked?View.GONE:View.VISIBLE);
		}
		mMore.setVisibility(isChecked?View.GONE:View.VISIBLE);
		if (isChecked) {
			if (mTimer!=null) {
				mTimer.cancelTimer();
			}
			mTimeStatus.setVisibility(View.INVISIBLE);			
			mTimePickerLayout.setVisibility(View.GONE);
		}
	}

}
