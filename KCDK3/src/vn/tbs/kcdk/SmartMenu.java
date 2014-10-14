package vn.tbs.kcdk;


import static vn.tbs.kcdk.global.Common.CATEGORY_ID_HEADER;
import static vn.tbs.kcdk.global.Common.CATEGORY_ID_HISTORY;
import static vn.tbs.kcdk.global.Common.CATEGORY_ID_SEARCH;
import static vn.tbs.kcdk.global.Common.CATEGORY_ID_TIMER;
import static vn.tbs.kcdk.global.Common.MENU_SEARCH;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.fragments.menu.MenuAdapter;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ServerConnection;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;


public class SmartMenu implements OnItemClickListener {
	private static final String TAG = SmartMenu.class.getSimpleName();
	private MenuAdapter mMenuAdapter;
	private ListView mMenuListView;
	private List<CategoryRow> mCategories;
	private Context mContext = null;
	private LoadCategoriesFromServerTask mLoadCategoriesFromServerTask;

	
	public SmartMenu(ListView aMenuListView, Context mContext) {
		super();
		this.mMenuListView = aMenuListView;
		this.mContext = mContext;
		loadCategoriesMenu();
	}



	private void loadCategoriesMenu() {
		Log.i(TAG, "loadCategoriesMenu start");

		initListview();

		final String url = Common.getConnectUrl(mContext,Common.URL_CATEGORY_MODE,null);
		mLoadCategoriesFromServerTask = new LoadCategoriesFromServerTask();
		mLoadCategoriesFromServerTask.execute(url);
		Log.i(TAG, "loadCategoriesMenu end");
		//		return categories;
	}

	private void initListview() {
		Log.i(TAG, "initListview start");

		if (mCategories==null) {
			mCategories = loadDefaultCategories();
		}
		mMenuAdapter = new MenuAdapter(mCategories,mContext);
		mMenuAdapter.setSelectedPosition(2,true);
		mMenuListView.setAdapter(mMenuAdapter);
		mMenuListView.setOnItemClickListener(this);

		Log.i(TAG, "initListview end");
	}

	protected List<CategoryRow> loadDefaultCategories() {
		Log.i(TAG, "loadDefaultCategories start");

		List<CategoryRow> categories = new ArrayList<CategoryRow>();


		categories.add(new CategoryRow(CATEGORY_ID_HEADER, "YOU",false, null));
		//TODO later match to history at kcdk activity
		categories.add(new CategoryRow(CATEGORY_ID_TIMER, "Close App when playing done", false, null));
		categories.add(new CategoryRow(CATEGORY_ID_HISTORY, "History", false, null));
		categories.add(new CategoryRow(CATEGORY_ID_HEADER, "CHUYÊN MỤC", false, null));
		/*categories.add(new CategoryRow(CATEGORY_ID_NAME_01, "Kể chuyện đêm khuya", false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_02, "Cửa sổ tình yêu", false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_03, "Blog Radio",  false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_04, "Quick & Snow Show", false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_05, "Truyện ngắn kinh dị",  false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_06, "Thơ quán",  false, null));
		categories.add(new CategoryRow(CATEGORY_ID_NAME_07, "Cafe chiều thứ 7", false, null));*/

		Log.i(TAG, "loadDefaultCategories end");
		return categories;

	}



	private void showContentFragment(CategoryRow item, int position) {
		boolean ok = switchContent(item);
		if (ok) {
			mMenuAdapter.setSelectedPosition(position,true);			
		}
//		mContext.showPlayer();
	}


	private boolean switchContent(CategoryRow item) {
		Log.i(TAG, "switchContent start");

		if (item!=null) {
			/*Fragment fragment = mContext.findFragmentByTag(item.getCategoryId());
			if (fragment==null) {
				fragment = Common.initContentFragment(item);
			}
			if (fragment!=null) {
				mContext.switchContent(fragment,true);
				return true;				
			}*/
		}

		Log.i(TAG, "switchContent end");

		return true;
	}


	public void refreshSelectedPosition(Fragment contentFragment) {
		Log.i(TAG, "refreshSelectedPosition start");

		if (contentFragment!=null&&contentFragment.isVisible()) {

			Bundle bundle = contentFragment.getArguments();
			if (bundle!=null) {
				int selectedPosition = Common.convertFromFragmentToPosition(bundle,mMenuAdapter);

				if (selectedPosition!=mMenuAdapter.getSelectedPosition()) {
					mMenuAdapter.setSelectedPosition(selectedPosition,true);
					if (selectedPosition>=0) {
						//getListView().smoothScrollToPosition(selectedPosition);						
					}

				}
			}
		}

		Log.i(TAG, "refreshSelectedPosition end");
	}

	private class LoadCategoriesFromServerTask extends AsyncTask<String, Void, List<CategoryRow>> {

		@Override
		protected List<CategoryRow> doInBackground(String... urls) {
			Log.i(TAG, "doInBackground start");
			//			String url = Common.getConnectUrl(mKCDKActivity,Common.URL_CATEGORY_MODE);
			List<CategoryRow> categories = ServerConnection.getCategories(urls[0]);
			//			SystemClock.sleep(2000);
			Log.i(TAG, "doInBackground end");
			return categories;
		}

		@Override
		protected void onPostExecute(List<CategoryRow> categories) {
			Log.i(TAG, "onPostExecute start");
			if (categories!=null&&categories.size()>0&&mMenuAdapter!=null) {
				mCategories.addAll(categories);
				mMenuAdapter.setCategories(mCategories);
				mMenuAdapter.notifyDataSetChanged();
			}
			super.onPostExecute(categories);
			Log.i(TAG, "onPostExecute end");

		}

	}

	public void showFoundMediaFragment(String query, CategoryRow item) {
		Log.i(TAG, "showFoundMediaFragment start");
		if (item !=null) {
			switchContent(item);
		}
		else{
			switchContent(new CategoryRow(CATEGORY_ID_SEARCH,MENU_SEARCH));
		}

		Log.i(TAG, "showFoundMediaFragment end");
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Log.i(TAG, "onItemClick start" +position);

		boolean isCurrentPosition = position==mMenuAdapter.getSelectedPosition();

		//for search menu
		CategoryRow item = mMenuAdapter.getItem(position);
		if (item!=null&&item.getItemMode()==MENU_SEARCH) {
			//mKCDKActivity.showSearchView(item);
			mMenuAdapter.setSelectedPosition(position,true);			
			return;
		}

		//other menu item
		if (isCurrentPosition) {
			//mKCDKActivity.toggle();
			return ;
		}

		showContentFragment(item,position);

		//		mKCDKActivity.setMenuVisible(position%2==0);
		Log.i(TAG, "onItemClick end" +position);
	}
}

