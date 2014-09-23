package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MAX_NUM_RELATIVE_MEDIA;
import static vn.tbs.kcdk.global.Common.MEDIA_AUTHOR_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_CONTENTINFO_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGEURL_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_PUBLISHDATE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_SPEAKER_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_TITLE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_VIEWCOUNT_KEY;

import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ServerConnection;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.LoadingDoneListener;

public class DescriptionFragment extends Fragment implements OnClickListener, LoadingDoneListener {
	private static final String TAG = DescriptionFragment.class.getName();

	private String mMediaImageUrl;
	//private ImageView mMediaImageView;

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
	private Button mMoreSuggestion;
	private LinearLayout mRelativeMediaLayout;
	private ProgressBar mProgressBar;

	private RelativeAsyntask mLoadRelativeAsyntask;

	private List<MediaInfo> mRelativeMediaList;

	private boolean mIsMore = true;

	private View mEndView;
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initUrlData();
		iniLrucache();
	}

	private void loadFromServer() {
		Log.i(TAG, "loadFromServer start");

		if (mLoadRelativeAsyntask==null) {
			mLoadRelativeAsyntask = new RelativeAsyntask();			
			mLoadRelativeAsyntask.execute();			
		}
		else{
			showRelativeMedia(mRelativeMediaList);
		}

		Log.i(TAG, "loadFromServer end");
	}

	private void hide2SampleMedia() {
		Log.i(TAG, "hide2SampleMedia start");

		setRelativeLayoutVisibility(mRelativeMediaLayout,View.VISIBLE);
		mMoreSuggestion.setVisibility(View.GONE);


		Log.i(TAG, "hide2SampleMedia end");
	}

	private void setRelativeLayoutVisibility(View view,int visibility) {

		if (view!=null) {
			view.setVisibility(visibility);
		}		
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.media_detail_layout, null);

		//mMediaImageView = (ImageView)view.findViewById(R.id.media_imageview);

		mComma = view.findViewById(R.id.three_comma_tv);
		mDivider = view.findViewById(R.id.divider);
		mContentLayout = view.findViewById(R.id.media_content_layout);
		if (mContentLayout!=null) {
			mContentLayout.setOnClickListener(this);
		}

		mContent = (TextView)view.findViewById(R.id.media_content_tv);
		mTitleTextView = (TextView)view.findViewById(R.id.media_title_tv);
		mAuthorTextView = (TextView)view.findViewById(R.id.media_author_tv);
		mViewCountTextView = (TextView)view.findViewById(R.id.media_viewcount_tv);
		mSpeakerTextView = (TextView)view.findViewById(R.id.media_speaker_tv);
		mPublishedDateTextView = (TextView)view.findViewById(R.id.media_publisheddate_tv);

		mMoreSuggestion = (Button)view.findViewById(R.id.more_relative_media);
		mRelativeMediaLayout = (LinearLayout)view.findViewById(R.id.relative_media_layout);
		mMoreSuggestion.setOnClickListener(this);
		mProgressBar = (ProgressBar)view.findViewById(R.id.relative_media_progressBar);
		mProgressBar.setVisibility(View.VISIBLE);
		mEndView = view.findViewById(R.id.end_view);

		hide2SampleMedia();
		return view;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		matchData();
		//TODO use it for fetch image for smart media player
		//mImageFetcher.setEnableOtherLoad(true);
		//mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);
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
		//TODO use it for fetch image for smart media player

		/*if (mMediaImageView != null) {
			// Cancel any pending image work
			ImageWorker.cancelWork(mMediaImageView);
			mMediaImageView.setImageDrawable(null);
		}*/
		mImageFetcher.closeCache();
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick start");

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
			mMoreSuggestion.setEnabled(false);
			if (isAddMore()) {
				showRelativeMedia(mRelativeMediaList);				
			}
			else{
				hideRelativeMedia();
			}
			String text = mIsMore?getString(R.string.less_suggestion):getString(R.string.more_suggestion);
			mMoreSuggestion.setText(text);
			mIsMore = ! mIsMore;
			mMoreSuggestion.setEnabled(true);
			return;
		default:
			break;
		}
	}


	private void hideRelativeMedia() {
		Log.i(TAG, "hideRelativeMedia start");

		if (mRelativeMediaLayout!=null) {
			int visibility = mIsMore?View.VISIBLE:View.GONE;
			
			for (int i = 3; i < mRelativeMediaLayout.getChildCount(); i++) {
				mRelativeMediaLayout.getChildAt(i).setVisibility(visibility);
			}
		}
		Log.i(TAG, "hideRelativeMedia end");
	}

	private boolean isAddMore() {

		return (mRelativeMediaLayout!=null&&mRelativeMediaLayout.getChildCount()<MAX_NUM_RELATIVE_MEDIA);

	}


	private class RelativeAsyntask extends AsyncTask<Void, Void, List<MediaInfo>>{

		@Override
		protected List<MediaInfo> doInBackground(Void... params) {
//			return ServerConnection.getRelativeMedia(getString(R.string.url_domain)+data.getString(MEDIA_ID_KEY));
			return ServerConnection.getRelativeMedia(getString(R.string.url_domain)+"/api/medialist?groupmode=&limit=10&offset=0");

		}

		@Override
		protected void onPostExecute(List<MediaInfo> mediaList) {
			Log.i(TAG, "onPostExecute start");
			mRelativeMediaList = mediaList;
			showRelativeMedia(mediaList);
			super.onPostExecute(mediaList);
			mLoadRelativeAsyntask.cancel(false);
			Log.i(TAG, "onPostExecute end");
		}

	}

	@Override
	public void loadOtherComponent() {
		Log.i(TAG, "loadOtherComponent start");

		loadFromServer();

		Log.i(TAG, "loadOtherComponent end");
	}

	public void showRelativeMedia(List<MediaInfo> result) {
		Log.i(TAG, "initRelativeMedia start");
		Context context = getActivity();

		if (context!=null&&result!=null&&result.size()>0&&mRelativeMediaLayout!=null) {
			setRelativeLayoutVisibility(mRelativeMediaLayout,View.VISIBLE);
			mMoreSuggestion.setVisibility(result.size()<=2?View.GONE:View.VISIBLE);

			LayoutInflater inflater = LayoutInflater.from(context);
			View child = null;
			mImageFetcher.setEnableResizeImageView(false);

			int start = mRelativeMediaLayout.getChildCount()>1?2:0;

			int size = result.size()<=2?result.size():2;

			size = start!=0?result.size():size;
			size = size>MAX_NUM_RELATIVE_MEDIA?MAX_NUM_RELATIVE_MEDIA:size;

			if (mRelativeMediaLayout.getChildCount()<MAX_NUM_RELATIVE_MEDIA) {
				for (int i = start; i < size ; i++) {
					final MediaInfo media = result.get(i);
					child =  inflater.inflate(R.layout.media_content_item_row, null);
					child.findViewById(R.id.relative_media_button).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							//TODO comment for later 
							//Common.showMediaPage(media,(KCDKActivity) getActivity());
						}
					});
					mRelativeMediaLayout.addView(child);
					Common.bindTextValue(child,media,false,SmartKCDKActivity.sFont);
					mImageFetcher.setEnableOtherLoad(false);
					mImageFetcher.loadImage(media.getMediaImageThumbUrl(), (ImageView) child.findViewById(R.id.media_item_image));
				}
			}
			if (mMoreSuggestion.getVisibility()==View.GONE) {
				mEndView.setVisibility(View.VISIBLE);
			}

		}
		mProgressBar.setVisibility(View.GONE);

		Log.i(TAG, "initRelativeMedia end");
	}

	public void updateData(MediaInfo item) {
		if (item!=null) {
			Typeface tf=Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf");

			mContent.setText(item.getContentInfo());
			mTitleTextView.setText(item.getTitle());
			mAuthorTextView.setText(item.getAuthor());
			mSpeakerTextView.setText(item.getSpeaker());
			mPublishedDateTextView.setText(item.getPublishedDate());
			mViewCountTextView.setText(item.getCommentCount());

			mContent.setTypeface(tf); 
			mTitleTextView.setTypeface(tf);
			mAuthorTextView.setTypeface(tf);
			mSpeakerTextView.setTypeface(tf);
			mPublishedDateTextView.setTypeface(tf);
			mViewCountTextView.setTypeface(tf);
		}
	}
}
