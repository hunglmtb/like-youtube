package vn.tbs.kcdk;

import java.util.ArrayList;
import java.util.Arrays;

import vn.tbs.kcdk.SmartViewWithMenu.OnTopListener;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import android.app.SearchManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;

public class SmartKCDKActivity  extends ActionBarActivity implements OnTopListener, ItemSelectionListener {

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

	//media player 
	
	
	private SmartViewWithMenu mSmartViewWithMenu;
	private PinnedHeaderMediaListFragment mPinnedHeaderMediaListFragment;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> listAdapter = null;
		sFont = Typeface.createFromAsset(this.getAssets(),"Roboto-Light.ttf");
		
		// Create and populate a List of planet names.
		String[] planets = new String[] { "kaka", "keke", "kiki", "kuku",
				"Jupiter", "Saturn", "Uranus", "Neptune", "Venus", "Mars", "Venus", "Neptune", "Neptune","kaka", "keke", "kiki", "kuku"};  
		ArrayList<String> planetList = new ArrayList<String>();
		planetList.addAll( Arrays.asList(planets) );

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList	);
		
		//----------------------------------------------------------------------------
		mSmartViewWithMenu  = new SmartViewWithMenu(this, listAdapter);
		mSmartViewWithMenu.setOnTopListener(this);
		View view = mSmartViewWithMenu.getView();
		setContentView(view);
		
		mPinnedHeaderMediaListFragment = (PinnedHeaderMediaListFragment)getSupportFragmentManager().findFragmentById(R.id.mainFragment);
		mPinnedHeaderMediaListFragment.setOnItemSelectionListener(this);
	}
	public void onBackPressed() {
		if (mSmartViewWithMenu==null||(mSmartViewWithMenu!=null&&mSmartViewWithMenu.onBackPressed())) {
			super.onBackPressed();
		}
	}
	@Override
	public void doSmartViewOnTop(boolean onTop) {
		android.support.v7.app.ActionBar actionBar = getSupportActionBar();
		if (actionBar!=null) {
			if (onTop) {
				actionBar.hide();				
			}
			else{
				actionBar.show();
			}
		}
	}
	@Override
	public void doItemSelection(MediaInfo item) {
		if (mSmartViewWithMenu!=null) {
			mSmartViewWithMenu.showMediaContent(item);			
		}
	}
}
