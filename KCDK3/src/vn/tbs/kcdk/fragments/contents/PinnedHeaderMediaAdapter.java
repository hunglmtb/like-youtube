package vn.tbs.kcdk.fragments.contents;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.LikeAndCommentCountLoadingAsyntask.OnDoneListener;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.PinnedHeaderListView;
import vn.tbs.kcdk.global.PinnedHeaderListView.PinnedHeaderAdapter;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageFetcher;


public final class PinnedHeaderMediaAdapter extends MediaAdapter implements SectionIndexer, OnScrollListener, PinnedHeaderAdapter, OnDoneListener {
	private static final String TAG = PinnedHeaderMediaAdapter.class.getSimpleName();

	private MediaIndexer mIndexer;

	private boolean mEnablePinnedHeader = false;

	/*public PinnedHeaderMediaAdapter(Context context, int resourceId, int textViewResourceId, String[] objects) {
		super(context, resourceId, textViewResourceId, objects);
		this.mIndexer = new StringArrayAlphabetIndexer(objects, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}*/

	public PinnedHeaderMediaAdapter(List<MediaInfo> mCategories, Context context, String[] objects, boolean mEnablePinnedHeader, MediaIndexer indexer) {
		super(mCategories,context);
		//		this.mIndexer = new StringArrayAlphabetIndexer(objects, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
//		this.mIndexer = new MediaIndexer(mCategories);
		this.mIndexer = indexer;
		this.mEnablePinnedHeader  = mEnablePinnedHeader;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		v.setTag(position);

		if (mEnablePinnedHeader) {
			bindSectionHeader(v, position);			
		}

		return v;
	}

	private void bindSectionHeader(View itemView, int position) {
		final View headerView =  itemView.findViewById(R.id.pin_header_view);

		final int section = getSectionForPosition(position);
		int posForSec = getPositionForSection(section);
		if (position==0) {
			headerView.setVisibility(View.INVISIBLE);
		}
		else if (posForSec == position) {
			headerView.setVisibility(View.VISIBLE);
			MediaInfo media = mMediaList.get(position);
			if (media!=null) {
				//TODO update font != null
				Common.setTextValue(itemView, R.id.header_text, mIndexer.getSectionStringByMedia(media),Typeface.NORMAL,null);
			}

			Log.i(TAG, "bindSectionHeader VISIBLE at position "+position);
		} else {
			headerView.setVisibility(View.GONE);

			Log.i(TAG, "bindSectionHeader GONE at position "+position);
		}

	}

	public int getPositionForSection(int sectionIndex) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getPositionForSection(sectionIndex);
	}

	public int getSectionForPosition(int position) {
		if (mIndexer == null) {
			return -1;
		}

		return mIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		if (mIndexer == null) {
			return new String[] { " " };
		} else {
			return mIndexer.getSections();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}		
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	@Override
	public int getPinnedHeaderState(int position) {
		if (mIndexer == null || getCount() == 0) {
			return PINNED_HEADER_GONE;
		}

		if (position < 0) {
			return PINNED_HEADER_GONE;
		}

		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		int section = getSectionForPosition(position);
		int nextSectionPosition = getPositionForSection(section + 1);

		if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}

		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View v, int position, int alpha) {
		TextView header = (TextView) v.findViewById(R.id.header_text);

		final int section = getSectionForPosition(position);
		Object[] sections = getSections();
		if (section<0||section>=sections.length) {
			return;
		}
		final String title = (String) sections[section];

		header.setText(title);

		/*if (alpha == PinnedHeaderListView.MAX_ALPHA) {
			header.setBackgroundColor(mPinnedHeaderBackgroundColor);
			header.setTextColor(mPinnedHeaderTextColor);
			header.setBackgroundColor(Color.CYAN);
		} else {
			header.setBackgroundColor(Color.argb(alpha, 
					Color.red(mPinnedHeaderBackgroundColor),
					Color.green(mPinnedHeaderBackgroundColor),
					Color.blue(mPinnedHeaderBackgroundColor)));
			header.setTextColor(Color.argb(alpha, 
					Color.red(mPinnedHeaderTextColor),
					Color.green(mPinnedHeaderTextColor),
					Color.blue(mPinnedHeaderTextColor)));

			header.setBackgroundColor(Color.RED);
		}*/
	}

	public void addMoreData(List<MediaInfo> moreData) {
		Log.i(TAG, "addMoreData start");

		if (moreData!=null&&moreData.size()>0) {
			Common.updateLikeAndCommentCount(moreData,this);
			if (mMediaList!=null) {
				mMediaList.addAll(moreData);
				if (mEnablePinnedHeader) {
					updateSectionIndex(mMediaList);					
				}
			}
			else{
				mMediaList = new ArrayList<MediaInfo>(moreData);
			}
			notifyDataSetChanged();
		}

		Log.i(TAG, "addMoreData end");
	}

	private void updateSectionIndex(List<MediaInfo> moreData) {
		Log.i(TAG, "updateSectionIndex start");

		if (mIndexer!=null) {
			mIndexer.updateSections(moreData);
		}

		Log.i(TAG, "updateSectionIndex end");
	}


	@Override
	public void updateMediaData() {
		Log.i(TAG, "updateMediaData start");

		notifyDataSetChanged();

		Log.i(TAG, "updateMediaData end");
	}

}