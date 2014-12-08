package vn.tbs.kcdk.fragments.contents;

import java.util.Arrays;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.PinnedHeaderListView;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.bitmapfun.util.ImageCache.ImageCacheParams;
import com.example.android.bitmapfun.util.ImageFetcher;

public class PinnedHeaderMediaListFragment extends ListFragment {
	public interface ItemSelectionListener {

		void doItemSelection(MediaInfo item, boolean reset, boolean animate);

	}

	private static final String TAG = PinnedHeaderMediaListFragment.class.getSimpleName();

	private PinnedHeaderMediaAdapter mPinnedHeaderMediaAdapter;
	private PinnedHeaderListView mMediaContentListView;
	private EndlessLoadAdapter mEndlessAdapter;
	protected boolean mEnablePinnedHeader = false;
	protected boolean mEnablePercentInfo = false;
	protected MediaIndexer mIndexer;

	//lrucache 
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private int mImageThumbSize;
	private ImageFetcher mImageFetcher;

	private ItemSelectionListener mItemListerner;

;

	private static final String [] names = {
		"Geoffrey Hampton",
		"Ciaran Holcomb",
		"Marshall Kelly",
		"Mufutau Saunders",
		"Ishmael Durham",
		"Brock Golden",
		"Tad Wright",
		"Carl Olsen",
		"Sebastian Mcmahon",
		"Talon Stout",
		"Anthony Johnston",
		"Simon Hale",
		"Talon Leon",
		"Stephen Mayo",
		"Ezra Graham",
		"Ryan Juarez",
		"Nathan Bowman",
		"Kermit Mcclure",
		"Axel Rhodes",
		"David Maynard",
		"Wing Larsen",
		"Nigel Mccormick",
		"Herrod Rivera",
		"Armando Meyers",
		"Zeus Brooks",
		"Hilel Stafford",
		"Merrill Russo",
		"Cole Lang",
		"Dieter Velez",
		"Lance Stokes",
		"Jarrod Oneil",
		"Louis Robbins",
		"Dorian Wong",
		"Nicholas Adams",
		"Kaseem Holt",
		"Clarke Munoz",
		"Logan Holmes",
		"Kennedy Moody",
		"Jamal David"
	};

	static {
		Arrays.sort(names);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate start");

		super.onCreate(savedInstanceState);

		//initKCDK();
		iniLrucache();
		initAdapter();

		Log.i(TAG, "onCreate end");
	}


/*	private void initKCDK() {
		Log.i(TAG, "initKCDK start");

		FragmentActivity act = getActivity();
		if (act!=null&&act instanceof KCDKActivity) {
			mKCDKActivity = (KCDKActivity) act;

		}

		Log.i(TAG, "initKCDK end");
	}*/


	private void initAdapter() {
		Log.i(TAG, "initAdapter start");

		mPinnedHeaderMediaAdapter = new PinnedHeaderMediaAdapter(null,getActivity(),names,mEnablePinnedHeader,mImageFetcher,mIndexer);
		mPinnedHeaderMediaAdapter.setIsHistoryAdapterType(mEnablePercentInfo);
		mEndlessAdapter = new EndlessLoadAdapter(mPinnedHeaderMediaAdapter, getActivity());
		setListAdapter(mEndlessAdapter);

		Log.i(TAG, "initAdapter end");
	}


	private void iniLrucache() {
		Log.i(TAG, "iniLrucache start");

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);		
		Log.i(TAG, "iniLrucache end");
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMediaContentListView = (PinnedHeaderListView) inflater.inflate(R.layout.pinned_header_list, null);

		mMediaContentListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageFetcher.setPauseWork(true);
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		return mMediaContentListView;
	}


	@Override
	public void onResume() {
		Log.i(TAG, "onResume start");
		super.onResume();
		//lrucache
		mImageFetcher.setExitTasksEarly(false);
		if (mPinnedHeaderMediaAdapter!=null) {
			mPinnedHeaderMediaAdapter.notifyDataSetChanged();			
		}

		//resize image thumbnail size to suitable lrchace
		mImageThumbSize = mMediaContentListView.getWidth();
		mImageFetcher.setImageSize(mImageThumbSize);

		Log.i(TAG, "onResume end");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (mEnablePinnedHeader) {
			View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.media_header, mMediaContentListView, false);
			mMediaContentListView.setPinnedHeaderView(headerView);
			Common.setTextValue(headerView, R.id.header_text, "",Typeface.NORMAL, SmartKCDKActivity.sFont);
			mMediaContentListView.setOnScrollListener(mPinnedHeaderMediaAdapter);
		}
		else{
			mMediaContentListView.setPinnedHeaderView(null);
		}
		mMediaContentListView.setDividerHeight(0);

	}

	@Override
	public void onListItemClick(ListView lv, View view, int position, long id) {
		Log.i(TAG, "onListItemClick start" +position);

		if (mItemListerner!=null) {
			mItemListerner.doItemSelection(mPinnedHeaderMediaAdapter.getItem(position),true,true);			
		}
		//Common.showMediaPage(mPinnedHeaderMediaAdapter.getItem(position),mKCDKActivity);
		//		Common.showMediaContent(mMediaAdapter.getItem(position),mKCDKActivity);

		//TODO remove later due to lrucache clear
		if (position==0) {
			mImageFetcher.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
		}
		Log.i(TAG, "onListItemClick end" +position);
	}

	//lrucahce
	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}


	public void setOnItemSelectionListener(ItemSelectionListener alistener) {
		this.mItemListerner = alistener;
	}


	public void setEnableLoading(boolean enable) {
		if (mEndlessAdapter!=null) {
			mEndlessAdapter.setEnableLoading(enable);
		}
	}
	
	public void reloadMediaList(CategoryRow item) {
		Log.i(TAG, "reloadMediaList start");
		if (mEndlessAdapter!=null) {
			mEndlessAdapter.reload(item);
		}
		Log.i(TAG, "reloadMediaList end");
	}
}
