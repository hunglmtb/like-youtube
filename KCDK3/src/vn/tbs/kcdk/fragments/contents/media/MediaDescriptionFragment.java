package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MEDIA_AUTHOR_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_CONTENTINFO_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGEURL_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_PUBLISHDATE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_SPEAKER_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_TITLE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_VIEWCOUNT_KEY;

import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.global.ServerConnection;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.ImageWorker;
import com.example.android.bitmapfun.util.LoadingDoneListener;

public class MediaDescriptionFragment extends ListFragment implements LoadingDoneListener {
	private static final String TAG = MediaDescriptionFragment.class.getName();

	private String mMediaImageUrl;
	private ImageView mMediaImageView;

	private static final String IMAGE_CACHE_DIR = "images";

	private ImageFetcher mImageFetcher;

	private Bundle data;

	private TextView mContent;
	private TextView mTitleTextView;
	private TextView mAuthorTextView;
	private TextView mViewCountTextView;
	private TextView mSpeakerTextView;
	private TextView mPublishedDateTextView;
	private View mContentLayout;
	private View mDivider;
	private View mComma;
	private View mMoreSuggestion;
	private View mProgressBar;

	private RelativeAsyntask mLoadRelativeAsyntask;
	private List<MediaInfo> mRelativeMediaList;
	private DetailMediaAdapter mAdapter;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initUrlData();
		iniLrucache();
		initAdapter();
	}

	private void initAdapter() {
		Log.i(TAG, "initAdapter start");

		mAdapter = new DetailMediaAdapter(getActivity(),mImageFetcher);
		setListAdapter(mAdapter);

		Log.i(TAG, "initAdapter end");
	}

	private void initStaticView(LayoutInflater inflater) {
		Log.i(TAG, "initStaticView start");

		mMediaImageView = (ImageView) inflater.inflate(R.layout.image_detail_media, null);
		mContentLayout = inflater.inflate(R.layout.description_detail_media, null);
		mMoreSuggestion = inflater.inflate(R.layout.more_detail_media, null);
		mProgressBar = inflater.inflate(R.layout.loading_detail_media, null);
		mProgressBar.setVisibility(View.VISIBLE);

		mAdapter.setStaticView(mMediaImageView,mContentLayout,mMoreSuggestion,mProgressBar);

		bindText();
		Log.i(TAG, "initStaticView end");
	}

	private void bindText() {
		Log.i(TAG, "bindText start");

		mComma = mContentLayout.findViewById(R.id.three_comma_tv);
		mDivider = mContentLayout.findViewById(R.id.divider);

		mContent = (TextView)mContentLayout.findViewById(R.id.media_content_tv);
		mTitleTextView = (TextView)mContentLayout.findViewById(R.id.media_title_tv);
		mAuthorTextView = (TextView)mContentLayout.findViewById(R.id.media_author_tv);
		mViewCountTextView = (TextView)mContentLayout.findViewById(R.id.media_viewcount_tv);
		mSpeakerTextView = (TextView)mContentLayout.findViewById(R.id.media_speaker_tv);
		mPublishedDateTextView = (TextView)mContentLayout.findViewById(R.id.media_publisheddate_tv);

		Log.i(TAG, "bindText end");
	}

	private void loadFromServer() {
		Log.i(TAG, "loadFromServer start");

		if (mRelativeMediaList==null) {
			mLoadRelativeAsyntask = new RelativeAsyntask();			
			mLoadRelativeAsyntask.execute();			
		}
		else{
			updateRelativeMediaList(mRelativeMediaList);
		}

		Log.i(TAG, "loadFromServer end");
	}

	private void initUrlData() {
		Log.i(TAG, "initUrlData start");

		data = getArguments();
		if (data!=null) {
			mMediaImageUrl = data.getString(MEDIA_IMAGEURL_KEY);
		}

		Log.i(TAG, "initUrlData end");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initStaticView(inflater);
		View view = inflater.inflate(R.layout.listview_detail, null);
		return view; 
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		matchData();
		mImageFetcher.setEnableOtherLoad(true);
		mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);
	}


	private void matchData() {
		Log.i(TAG, "matchData start");

		if (data!=null) {
			Typeface tf=Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf");

			mContent.setText(data.getString(MEDIA_CONTENTINFO_KEY));
			mTitleTextView.setText(data.getString(MEDIA_TITLE_KEY));
			mAuthorTextView.setText(data.getString(MEDIA_AUTHOR_KEY));
			mSpeakerTextView.setText(data.getString(MEDIA_SPEAKER_KEY));
			mPublishedDateTextView.setText(data.getString(MEDIA_PUBLISHDATE_KEY));
			mViewCountTextView.setText(data.getString(MEDIA_VIEWCOUNT_KEY));

			mContent.setTypeface(tf); 
			mTitleTextView.setTypeface(tf);
			mAuthorTextView.setTypeface(tf);
			mSpeakerTextView.setTypeface(tf);
			mPublishedDateTextView.setTypeface(tf);
			mViewCountTextView.setTypeface(tf);
		}

		Log.i(TAG, "matchData end");
	}

	private void iniLrucache() {

		// Fetch screen height and width, to use as our max size when loading images as this
		// activity runs full screen
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// For this sample we'll use half of the longest width to resize our images. As the
		// image scaling ensures the image is larger than this, we should be left with a
		// resolution that is appropriate for both portrait and landscape. For best image quality
		// we shouldn't divide by 2, but this will use more memory and require a larger memory
		// cache.
		final int longest = (height > width ? height : width) / 2;

		ImageCache.ImageCacheParams cacheParams =
				new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), longest);
		mImageFetcher.setStandardWidth(width);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.setImageFadeIn(true);
		mImageFetcher.setLoadingDoneListener(this);
		mImageFetcher.setEnableResizeImageView(true);
	}


	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		//		loadRelativeMedia();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMediaImageView != null) {
			// Cancel any pending image work
			ImageWorker.cancelWork(mMediaImageView);
			mMediaImageView.setImageDrawable(null);
		}
		mImageFetcher.closeCache();
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.i(TAG, "onListItemClick start");

		switch (v.getId()) {
		case R.id.media_content_layout:
			if (mContent.getVisibility()==View.VISIBLE) {
				mContent.setVisibility(View.GONE);
				mDivider.setVisibility(View.GONE);
				mComma.setVisibility(View.VISIBLE);
			}
			else if (mContent.getVisibility()==View.GONE) {
				mContent.setVisibility(View.VISIBLE);
				mDivider.setVisibility(View.VISIBLE);
				mComma.setVisibility(View.GONE);
			}
			return;
		case R.id.more_relative_media:
			boolean hiden = mAdapter.isHiden();
			String text = hiden?getString(R.string.less_suggestion):getString(R.string.more_suggestion);
			((TextView)mMoreSuggestion.findViewById(R.id.more_relative_media_text)).setText(text);
			mAdapter.setHiden(!hiden);
			mAdapter.notifyDataSetChanged();
			return;
		default:
			break;
		}


		if (v.getId()!=mMoreSuggestion.getId()&&position>=2) {
			int pos = position - 2;
			if (mRelativeMediaList!=null) {
//				TODO
				//Common.showMediaPage(mRelativeMediaList.get(pos),(KCDKActivity) getActivity());
			}
		}
		Log.i(TAG, "onListItemClick end");
	}


	private class RelativeAsyntask extends AsyncTask<Void, Void, List<MediaInfo>>{

		@Override
		protected List<MediaInfo> doInBackground(Void... params) {

//			List<MediaInfo> mMediaList = ServerConnection.getRelativeMedia(data.getString(MEDIA_ID_KEY));
			List<MediaInfo> mMediaList = ServerConnection.getRelativeMedia(getString(R.string.url_domain)+"/api/medialist?groupmode=&limit=10&offset=0");
			ServerConnection.getLikeAndCommentCount(mMediaList);
			return mMediaList;

		}

		@Override
		protected void onPostExecute(List<MediaInfo> mediaList) {
			Log.i(TAG, "onPostExecute start");
			mRelativeMediaList = mediaList;
			updateRelativeMediaList(mediaList);
			super.onPostExecute(mediaList);
			mLoadRelativeAsyntask.cancel(false);
			Log.i(TAG, "onPostExecute end");
		}

	}

	@Override
	public void loadOtherComponent() {
		Log.i(TAG, "loadOtherComponent start");

		mImageFetcher.setEnableResizeImageView(false);
		loadFromServer();

		Log.i(TAG, "loadOtherComponent end");
	}

	public void updateRelativeMediaList(List<MediaInfo> mediaList) {
		Log.i(TAG, "updateRelativeMediaList start");

		if (mAdapter!=null) {
			mAdapter.setRelativeMediaList(mediaList);
			mAdapter.setIsLoading(false);
			mAdapter.notifyDataSetChanged();
		}

		Log.i(TAG, "updateRelativeMediaList end");
	}
}
