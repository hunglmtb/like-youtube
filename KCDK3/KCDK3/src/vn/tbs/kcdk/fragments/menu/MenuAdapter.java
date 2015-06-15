package vn.tbs.kcdk.fragments.menu;

import static vn.tbs.kcdk.global.Common.MENU_CATEGORY;
import static vn.tbs.kcdk.global.Common.MENU_EXIT;
import static vn.tbs.kcdk.global.Common.MENU_HEADER;
import static vn.tbs.kcdk.global.Common.MENU_HISTORY;
import static vn.tbs.kcdk.global.Common.MENU_NEWS_FEED;
import static vn.tbs.kcdk.global.Common.MENU_SEARCH;
import static vn.tbs.kcdk.global.Common.MENU_SETTING;
import static vn.tbs.kcdk.global.Common.MENU_TIMER;

import java.util.List;

import vn.tbs.kcdk.R;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//import com.actionbarsherlock.widget.SearchView;

public class MenuAdapter extends BaseAdapter { //implements SearchView.OnQueryTextListener,SearchView.OnSuggestionListener {

	private static final String TAG = MenuAdapter.class.getSimpleName();
	private List<CategoryRow> mCategories;
	private int mSelectedPosition = 0;
	private Context mContext;
	private Typeface mFont = null;
	private SuggestionsAdapter mSuggestionsAdapter;
	private View mAuthenticateView;

	public MenuAdapter(List<CategoryRow> mCategories, Context context) {
		this.mCategories = mCategories;
		this.mContext = context;
		this.mFont=Typeface.createFromAsset(mContext.getAssets(),"Roboto-Regular.ttf");

	}



	public void setCategories(List<CategoryRow> mCategories) {
		this.mCategories = mCategories;
	}



	public int getSelectedPosition() {
		return mSelectedPosition;
	}


	@Override
	public int getCount() {
		if (mCategories!=null) {
			return mCategories.size();
		}
		return 0;
	}

	@Override
	public CategoryRow getItem(int pos) {
		if (mCategories!=null&&pos<getCount()&&pos>=0) {
			return mCategories.get(pos);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		/*if (position==getCount()-1) {
			if (mAuthenticateView==null) {
				mAuthenticateView = inflater.inflate(R.layout.authenticate_view_layout, null);
				WebView webview = (WebView) mAuthenticateView.findViewById(R.id.webview);
				webview.setHorizontalScrollBarEnabled(false);
				WebSettings webSettings = webview.getSettings();
				webSettings.setJavaScriptEnabled(true);
				webSettings.setAppCacheEnabled(true);
				webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
				webSettings.setSupportMultipleWindows(true);
				webview.setWebViewClient(new WebViewClient(){
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						return false;
					}
				} );
				webview.requestFocus(View.FOCUS_DOWN);
				String url =mContext.getString(R.string.url_domain)+mContext.getString(R.string.action_url_authenticate);
				webview.loadUrl(url);
			}
			return mAuthenticateView;
		}*/
		CategoryRow category = mCategories.get(position);
		Log.i(TAG, "category key String "+category.getCategoryKeyString()+" id "+ category.getCategoryId() +" name "+category.getCategoryName()+" mode "+category.getItemMode());

		if (category!=null) {
			TextView rowText = null ;
			ImageView icon = null;
			switch (category.getItemMode()) {
			case MENU_HEADER:
				//header then select header layout
				convertView = inflater.inflate(R.layout.category_header_row, null);
				rowText = (TextView) convertView.findViewById(R.id.category_row_text);
				break;
			case MENU_SEARCH:
			case MENU_CATEGORY:
			case MENU_NEWS_FEED:
			case MENU_SETTING:
			case MENU_EXIT:
			case MENU_HISTORY:
			case MENU_TIMER:
				//category then select category layout
				convertView = inflater.inflate(R.layout.category_row, null);
				updateSelectedView(convertView,position);
				rowText = (TextView) convertView.findViewById(R.id.category_row_text);
				icon = (ImageView) convertView.findViewById(R.id.category_menu_icon);
				break;
			/*case MENU_TIMER:
				//category then select category layout
				convertView = inflater.inflate(R.layout.timer_row, null);
				updateSelectedView(convertView,position);
				rowText = (TextView) convertView.findViewById(R.id.category_row_text);
				icon = (ImageView) convertView.findViewById(R.id.category_menu_icon);
				break;*/

			default:
				break;
			}

			if(rowText != null){
				rowText.setText(category.getCategoryName());
				if (mFont!=null) {
					rowText.setTypeface(mFont);				
				}
			}
			if(icon != null){
				icon.setImageResource(category.getIconResource());
			}
		}

		return convertView;
	}


	private void updateSelectedView(View convertView, int position) {
		Log.i(TAG, "updateSelectedView start");

		View divider = convertView.findViewById(R.id.divider_at_menu_item);
		if (position==mSelectedPosition) {
			convertView.setBackgroundResource(R.drawable.bg_selected_category_item);
			if (divider!=null) {
				divider.setVisibility(View.INVISIBLE);
			}
		}
		else{
			if (divider!=null) {
				divider.setVisibility(View.VISIBLE);
			}
		}		
		Log.i(TAG, "updateSelectedView end");
	}



	public void setSelectedPosition(int selectedPosition, boolean notify) {
		Log.i(TAG, "setSelectedPosition start" + "position: "+ selectedPosition +" notify: "+notify);

		CategoryRow item = getItem(selectedPosition);
		if (item==null||(item!=null&&(item.getItemMode()==MENU_CATEGORY||
				item.getItemMode()==MENU_HISTORY||
				item.getItemMode()==MENU_NEWS_FEED||
				item.getItemMode()==MENU_SEARCH||
				item.getItemMode()==MENU_SETTING))){
			this.mSelectedPosition = selectedPosition;
			if (notify) {
				this.notifyDataSetChanged();							
			}
		}
		Log.i("keke", "koko selectedPosition "+selectedPosition+" mSelectedPosition "+mSelectedPosition);

		Log.i(TAG, "setSelectedPosition end");
	}



	public CategoryRow getSelectedItem() {
		Log.i(TAG, "getSelectedItem start");

		if (mCategories!=null&&mSelectedPosition>0&&mSelectedPosition<mCategories.size()) {
			return mCategories.get(mSelectedPosition);
		}

		Log.i(TAG, "getSelectedItem end");
		return null;

	}



	public int getCategoryPositionById(String id) {
		Log.i(TAG, "getCategoryPositionById start");

		if (id!=null&&id.length()>0) {
			CategoryRow ctg = null;
			for (int i = 0; i < mCategories.size(); i++) {
				ctg = mCategories.get(i);
				if (id.equals(ctg.getCategoryId())) {
					return i;
				}
			}
		}

		Log.i(TAG, "getCategoryPositionById end");
		return -1;

	}

	private class SuggestionsAdapter extends CursorAdapter {

		public SuggestionsAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tv = (TextView) view;
			final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
			tv.setText(cursor.getString(textIndex));
		}
	}

	/*@Override
	public boolean onQueryTextSubmit(String query) {
		Toast.makeText(mContext, "You searched for: " + query, Toast.LENGTH_LONG).show();
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		return false;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor c = (Cursor) mSuggestionsAdapter.getItem(position);
		String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
		Toast.makeText(mContext, "Suggestion clicked: " + query, Toast.LENGTH_LONG).show();
		return true;
	}*/
}
