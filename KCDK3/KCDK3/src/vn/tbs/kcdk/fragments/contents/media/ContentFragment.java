package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ContentFragment extends Fragment {
	private ViewPager mMediaContentViewPage;
	private MediaContentPagerAdapter mMediaContentAdapter;
	private OnPageChangeListener mPageChangeListener;
	private static final String TAG = ContentFragment.class.getSimpleName();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate start");
		
		super.onCreate(savedInstanceState);
		mMediaContentAdapter = new MediaContentPagerAdapter(this,this.getArguments());

		Log.i(TAG, "onCreate end");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.media_content_layout, null);
		mMediaContentViewPage = (ViewPager) view.findViewById(R.id.media_content_viewpager);
		mMediaContentViewPage.setAdapter(mMediaContentAdapter);
		mMediaContentViewPage.setCurrentItem(0);
		mMediaContentViewPage.setOffscreenPageLimit(2);
		//TODO
//		handEvent();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}


/*	private void handEvent() {
		Log.i(TAG, "handEvent start");

		if (getActivity() instanceof KCDKActivity) {
			mPageChangeListener = new OnPageChangeListener() {

				@Override
				public void onPageScrollStateChanged(int arg0) { }

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) { }

				@Override
				public void onPageSelected(int position) {
					switch (position) {
					case 0:
						mKCDKActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
						break;
					default:
						mKCDKActivity.getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
						break;
					}

					if (position!=mMediaContentAdapter.getCount()-1) {
						setHasOptionsMenu(true);
					}
					else{
						setHasOptionsMenu(false);
					}
				}

			};
			mMediaContentViewPage.setOnPageChangeListener(mPageChangeListener);
		}		
		Log.i(TAG, "handEvent end");
	}*/


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.kcdk, menu);

	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.refresh_menu:
			Toast.makeText(getActivity(), "refresh", Toast.LENGTH_SHORT).show();
			Fragment fragment = mMediaContentAdapter.getItem(mMediaContentViewPage.getCurrentItem());
			if (fragment instanceof FacebookPluginFragment) {
				FacebookPluginFragment fbf = (FacebookPluginFragment) fragment;
				fbf.refreshWebView();
			}

			return true;
		case R.id.share_menu:
			mMediaContentViewPage.setCurrentItem(0);
			Fragment fragment2 = mMediaContentAdapter.getItem(0);
			if (fragment2 instanceof FacebookPluginFragment) {
				FacebookPluginFragment fbf = (FacebookPluginFragment) fragment2;
				fbf.shareMediaLink();
			}			
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

