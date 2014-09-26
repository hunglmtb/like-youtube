package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.EndlessLoadAdapter;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaAdapter;
import vn.tbs.kcdk.global.PinnedHeaderListView;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RelateMediaFragment extends ListFragment {

	private ListView mMediaContentListView;
	private ListAdapter mRelativeAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//mIsHistoryFragmentType = false;
		//mEnablePinnedHeader = false;
		super.onCreate(savedInstanceState);
		mRelativeAdapter = new ListAdapter() {
			
			@Override
			public void unregisterDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void registerDataSetObserver(DataSetObserver observer) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getViewTypeCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getItemViewType(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		setListAdapter(mRelativeAdapter);

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMediaContentListView = (ListView) inflater.inflate(R.layout.pinned_header_list, null);


		return mMediaContentListView;
	}

}
