package vn.tbs.kcdk;

import vn.tbs.kcdk.SmartMenu.ItemSelectedListener;
import vn.tbs.kcdk.SmartViewWithMenu.OnTopListener;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.fragments.contents.media.DescriptionFragment;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.mediaplayer.KCDKMediaPlayer;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SmartKCDKActivity  extends ActionBarActivity implements OnTopListener, ItemSelectionListener,ServiceConnection  {

	private static final String TAG = SmartKCDKActivity.class.getSimpleName();
	public static Typeface sFont = null;

	public static int sRemainSeconds = -1;


	private static final String[] COLUMNS = {
		BaseColumns._ID,
		SearchManager.SUGGEST_COLUMN_TEXT_1,
	};

	private ServiceConnection mConnection = this;
	//media player 


	private SmartViewWithMenu mSmartViewWithMenu;
	private PinnedHeaderMediaListFragment mPinnedHeaderMediaListFragment;
	private DescriptionFragment mDescriptionFragment;
	/** Called when the activity is first created. */

	boolean mIsBound;
	private KCDKMediaPlayer mKCDKMediaPlayer;
	private AdditionalFragment mThirdFragment;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sFont = Typeface.createFromAsset(this.getAssets(),"Roboto-Light.ttf");
		Intent intent = getIntent();
		//----------------------------------------------------------------------------
		mSmartViewWithMenu  = new SmartViewWithMenu(this,intent,this);
		View view = mSmartViewWithMenu.getView();
		setContentView(view);

		mSmartViewWithMenu.getSmartMenu().setItemSelectedListener( new ItemSelectedListener() {
			@Override
			public void doSelectMenuItem(CategoryRow item) {
				if (item!=null) {
					//TODO update later
					mSmartViewWithMenu.doMenuItemSelection(item);
					if (item.getCategoryId()=="dtdk") {
						if (mThirdFragment==null) {
							mThirdFragment = new AdditionalFragment();
							//		mThirdFragment.setArguments(getIntent().getExtras());					
						}
						mThirdFragment.showOriginWebview(true);
						mThirdFragment.refreshWebView(true);
						getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mThirdFragment).commit();					}
					else{
						if (mPinnedHeaderMediaListFragment!=null) {
							mPinnedHeaderMediaListFragment.setEnableLoading(true);
							mPinnedHeaderMediaListFragment.reloadMediaList(item);
						}
					}
				}

			}
		});

		mPinnedHeaderMediaListFragment = (PinnedHeaderMediaListFragment)getSupportFragmentManager().findFragmentById(R.id.mainFragment);
		mDescriptionFragment = (DescriptionFragment)getSupportFragmentManager().findFragmentById(R.id.secondFragment);

		mPinnedHeaderMediaListFragment.setOnItemSelectionListener(this);
		//mPinnedHeaderMediaListFragment.setEnableLoading(false);
		if (mSmartViewWithMenu.isShowDetailMedia()) {
			mPinnedHeaderMediaListFragment.setEnableLoading(!mSmartViewWithMenu.isShowDetailMedia());			
		}


		mKCDKMediaPlayer = mSmartViewWithMenu.getKCDKMediaPlayer();

		startService(new Intent(SmartKCDKActivity.this, KCDKMediaPlayerService.class));			
		mIsBound = false; // by default set this to unbound
		//automaticBind();
		doBindService();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "C:onServiceConnected()");
		Toast.makeText(this, "C:onServiceConnected", Toast.LENGTH_SHORT).show();
		if (mKCDKMediaPlayer!=null) {
			mKCDKMediaPlayer.initServiceMessenger(service);
		}

	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
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
	public void doSmartViewOnTop(int yAxis, boolean reachBottom,boolean onTop){
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar!=null) {
			boolean hide = yAxis<=actionBar.getHeight();
			if (hide) {
				actionBar.hide();				
			}
			else{
				if (!onTop) {
					actionBar.show();					
				}
				if (reachBottom&&mPinnedHeaderMediaListFragment!=null) {
					mPinnedHeaderMediaListFragment.reloadMediaList(null);
				}

			}
		}
	}
	@Override
	public void doItemSelection(MediaInfo item, boolean reset) {
		if(mDescriptionFragment!=null&&mSmartViewWithMenu!=null){
			if (item.validated()) {
				mDescriptionFragment.updateData(item,mSmartViewWithMenu.getMediaImage(),this);
				mSmartViewWithMenu.showMediaContent(item,reset);				
			}
			else{
				if (mPinnedHeaderMediaListFragment!=null) {
					Log.d(TAG, "doItemSelection reloadMediaListitem.id "+item.getMediaId()+" reset "+reset);
					mPinnedHeaderMediaListFragment.setEnableLoading(false);
					mPinnedHeaderMediaListFragment.reloadMediaList(null);
				}
			}
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



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mConnection = null;
		super.onDestroy();
	}
}
