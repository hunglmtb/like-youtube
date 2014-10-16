package vn.tbs.kcdk;

import java.util.ArrayList;
import java.util.Arrays;

import vn.tbs.kcdk.SmartViewWithMenu.OnTopListener;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.fragments.contents.media.DescriptionFragment;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class SmartKCDKActivity  extends ActionBarActivity implements OnTopListener, ItemSelectionListener,ServiceConnection  {

	private static final String TAG = SmartKCDKActivity.class.getSimpleName();
	private Fragment mContentFragment;
	public static Typeface sFont = null;

	public static int sRemainSeconds = -1;
	
	
	private static final String[] COLUMNS = {
		BaseColumns._ID,
		SearchManager.SUGGEST_COLUMN_TEXT_1,
	};

	private View mPlayerNavigation;
	private boolean mEnableActionBar;
	private ServiceConnection mConnection = this;
	//media player 
	
	
	private SmartViewWithMenu mSmartViewWithMenu;
	private PinnedHeaderMediaListFragment mPinnedHeaderMediaListFragment;
	private DescriptionFragment mDescriptionFragment;
	/** Called when the activity is first created. */
	
	boolean mIsBound;
	private KCDKMediaPlayer mKCDKMediaPlayer;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sFont = Typeface.createFromAsset(this.getAssets(),"Roboto-Light.ttf");
		
		//----------------------------------------------------------------------------
		mSmartViewWithMenu  = new SmartViewWithMenu(this);
		mSmartViewWithMenu.setOnTopListener(this);
		View view = mSmartViewWithMenu.getView();
		setContentView(view);
		
		mPinnedHeaderMediaListFragment = (PinnedHeaderMediaListFragment)getSupportFragmentManager().findFragmentById(R.id.mainFragment);
		mDescriptionFragment = (DescriptionFragment)getSupportFragmentManager().findFragmentById(R.id.secondFragment);
		
		mPinnedHeaderMediaListFragment.setOnItemSelectionListener(this);
		
		mKCDKMediaPlayer = mSmartViewWithMenu.getKCDKMediaPlayer();
		
		startService(new Intent(SmartKCDKActivity.this, KCDKMediaPlayerService.class));
		mIsBound = false; // by default set this to unbound
		automaticBind();
		doBindService();
	}
	

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "C:onServiceConnected()");
		if (mKCDKMediaPlayer!=null) {
			mKCDKMediaPlayer.initServiceMessenger(service);
		}
		
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		if (mKCDKMediaPlayer!=null) {
			Log.d(TAG, "C:onServiceDisconnected()");
			mKCDKMediaPlayer.setServiceMessenger(null);
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
			//textStatus.setText("Disconnected.");
		}
	}

	public void onBackPressed() {
		if (mSmartViewWithMenu==null||(mSmartViewWithMenu!=null&&mSmartViewWithMenu.onBackPressed())) {
			super.onBackPressed();
		}
	}
	@Override
	public void doSmartViewOnTop(int yAxis) {
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar!=null) {
			boolean hide = yAxis<=actionBar.getHeight();
			if (hide) {
				actionBar.hide();				
			}
			else{
				actionBar.show();
			}
		}
	}
	@Override
	public void doItemSelection(MediaInfo item) {
		if(mDescriptionFragment!=null&&mSmartViewWithMenu!=null){
			mDescriptionFragment.updateData(item,mSmartViewWithMenu.getMediaImage());
			mSmartViewWithMenu.showMediaContent(item);
			/*if (!isMyServiceRunning(KCDKMediaPlayerService.class)) {
				Intent intent = new Intent(getApplicationContext(),KCDKMediaPlayerService.class);
				intent.putExtra(KCDKMediaPlayerService.START_PLAY, true);
				startService(intent);
			}
			else{
				//Intent new_intent = new Intent();
		        //new_intent.setAction(ACTION_STRING_SERVICE);
		        //sendBroadcast(new_intent);
			}*/
		}
	}
	
	/**
	 * Check if the service is running. If the service is running when the
	 * activity starts, we want to automatically bind to it.
	 */
	private void automaticBind() {
		if (KCDKMediaPlayerService.isRunning()) {
			Log.d(TAG, "C:MyService.isRunning: doBindService()");
			doBindService();
		}
	}
	
	/**
	 * Bind this Activity to TimerService
	 */
	private void doBindService() {
		Log.d(TAG, "C:doBindService()");
		bindService(new Intent(this, KCDKMediaPlayerService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Toast.makeText(this, "Binding", Toast.LENGTH_SHORT).show();
//		textStatus.setText("Binding.");
	}

}
