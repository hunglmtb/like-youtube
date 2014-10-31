package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MEDIA_AUTHOR_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_CONTENTINFO_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGEURL_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_PUBLISHDATE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_SPEAKER_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_TITLE_KEY;
import static vn.tbs.kcdk.global.Common.MEDIA_VIEWCOUNT_KEY;
import vn.tbs.kcdk.KCDKApplication;
import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageWorker;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class DescriptionFragment extends Fragment implements OnClickListener {
	private static final String TAG = DescriptionFragment.class.getName();

	private String mMediaImageUrl;
	private ImageView mMediaImageView;
	private ImageManager imageManager;
	private ImageTagFactory imageTagFactory;

	public static final String IMAGE_CACHE_DIR = "images";

	//private ImageFetcher mImageFetcher;

	private Bundle data;
	private ScrollView mScrollView;

	private TextView mContent;
	private TextView mTitleTextView;
	private TextView mAuthorTextView;
	private TextView mViewCountTextView;
	private TextView mSpeakerTextView;
	private TextView mPublishedDateTextView;
	private View mContentLayout;
	private View mDivider;
	private View mComma;




	private ViewPager mViewPager;

	private MediaInfo mMediaItem = null;

	private RelateMediaFragment mRelateMediaFragment;
	private FacebookPluginFragment mFacebookPluginFragment;




	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initUrlData();
		iniLrucache();

		imageManager = KCDKApplication.getImageLoader();
		imageTagFactory = KCDKApplication.getImageTagFactory();

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

		mScrollView = (ScrollView) inflater.inflate(R.layout.media_detail_layout, null);

		//mMediaImageView = (ImageView)view.findViewById(R.id.media_imageview);

		mComma = mScrollView.findViewById(R.id.three_comma_tv);
		mDivider = mScrollView.findViewById(R.id.divider);
		mContentLayout = mScrollView.findViewById(R.id.media_content_layout);
		if (mContentLayout!=null) {
			mContentLayout.setOnClickListener(this);
		}

		mContent = (TextView)mScrollView.findViewById(R.id.media_content_tv);
		mTitleTextView = (TextView)mScrollView.findViewById(R.id.media_title_tv);
		mAuthorTextView = (TextView)mScrollView.findViewById(R.id.media_author_tv);
		mViewCountTextView = (TextView)mScrollView.findViewById(R.id.media_viewcount_tv);
		mSpeakerTextView = (TextView)mScrollView.findViewById(R.id.media_speaker_tv);
		mPublishedDateTextView = (TextView)mScrollView.findViewById(R.id.media_publisheddate_tv);



		//
		AppSectionsPagerAdapter mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getChildFragmentManager());

		mViewPager = (ViewPager) mScrollView.findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between different app sections, select the corresponding tab.
				// We can also use ActionBar.Tab#select() to do this if we have a reference to the
				// Tab.
				//actionBar.setSelectedNavigationItem(position);
				if (position==1&&mRelateMediaFragment!=null) {
					String mediaId = mMediaItem!=null?mMediaItem.getMediaId():"";
					mRelateMediaFragment.setMediaId(mediaId);
					mRelateMediaFragment.showRelativeMediaView();
				}
			}
		});
		//
		return mScrollView;

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		matchData();
		//TODO use it for fetch image for smart media player
		//mImageFetcher.setEnableOtherLoad(true);
		//mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);
	}


	public void matchData() {
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
		/*mImageFetcher = new ImageFetcher(getActivity(), longest);
		mImageFetcher.setStandardWidth(width);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.setImageFadeIn(true);
		mImageFetcher.setEnableResizeImageView(false);

        mImageFetcher.setLoadingDoneListener(mRelateMediaFragment);*/
	}


	@Override
	public void onResume() {
		super.onResume();
		//mImageFetcher.setExitTasksEarly(false);
		//		loadRelativeMedia();
	}

	@Override
	public void onPause() {
		super.onPause();
		//mImageFetcher.setExitTasksEarly(true);
		//mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//TODO use it for fetch image for smart media player

		if (mMediaImageView != null) {
			// Cancel any pending image work
			ImageWorker.cancelWork(mMediaImageView);
			mMediaImageView.setImageDrawable(null);
		}
		//mImageFetcher.closeCache();
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
		default:
			break;
		}
	}




	public void updateData(MediaInfo item, ImageView imageView, Context context) {
		if (item!=null&&item!=mMediaItem) {
			mMediaItem = item;
			if (imageView!=null) {
				mMediaImageUrl = item.getMediaImageUrl();
				mMediaImageView = imageView;
				/*mImageFetcher.setEnableOtherLoad(true);
				mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);*/

				imageView.setTag(imageTagFactory.build(mMediaImageUrl, context));
				imageManager.getLoader().load(imageView);
			}

			Typeface tf=Typeface.createFromAsset(context.getAssets(),"Roboto-Light.ttf");

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
			//mScrollView.fullScroll(ScrollView.FOCUS_UP);
			if (mFacebookPluginFragment!=null) {
				mFacebookPluginFragment.setMediaId(item.getMediaId());
				mFacebookPluginFragment.setEnableLoading(true);
				mFacebookPluginFragment.loadDataFromUrl(context);
			}
			
			if (mRelateMediaFragment!=null) {
				if (mViewPager.getCurrentItem()!=0) {
					mRelateMediaFragment.setMediaId(item.getMediaId());
					mRelateMediaFragment.showRelativeMediaView();					
				}
				else{
					mRelateMediaFragment.resetRelativeMedia();
				}
			}
		}
	}


	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
	 * sections of the app.
	 */
	public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

		public AppSectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			String mediaId = mMediaItem!=null?mMediaItem.getMediaId():"";
			switch (i) {
			case 0:
				if (mFacebookPluginFragment==null) {
					mFacebookPluginFragment = new FacebookPluginFragment(mediaId);
				}
				else{
					mFacebookPluginFragment.setMediaId(mediaId);
				}
				// The first section of the app is the most interesting -- it offers
				// a launchpad into the other demonstrations in this example application.
				return  mFacebookPluginFragment;

			default:
				if (mRelateMediaFragment==null) {
					mRelateMediaFragment = new RelateMediaFragment(mediaId);
				}
				else{
					mRelateMediaFragment.setMediaId(mediaId);
				}

				return mRelateMediaFragment;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "Section " + (position + 1);
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
			Bundle args = getArguments();
			((TextView) rootView.findViewById(android.R.id.text1)).setText(
					getString(R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
}
