package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MEDIA_IMAGEURL_KEY;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.KCDKApplication;
import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ImageViewTopCrop;
import vn.tbs.kcdk.global.ServerConnection;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
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
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.android.bitmapfun.util.ImageCache;
import com.example.android.bitmapfun.util.ImageWorker;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class DescriptionFragment extends Fragment implements OnClickListener {
	private static final String TAG = DescriptionFragment.class.getName();
	private WebView mChildWebview =null;
	protected RelativeLayout mParentLayout;

	private String mMediaImageUrl;
	private ImageView mMediaImageView;
	private ImageManager imageManager;
	private ImageTagFactory imageTagFactory;
	private boolean mEnableRefresh = false;
	private List<MediaInfo> mRelativeMediaList;

	public static final String IMAGE_CACHE_DIR = "images";

	//private ImageFetcher mImageFetcher;
	protected WebView mOriginalWebView;

	private Bundle data;
	private ScrollView mScrollView;
	private ViewPager mViewPager;
	private FrameLayout mLoadingLayout;

	private MediaInfo mMediaItem = null;

	private RelateMediaFragment2 mRelateMediaFragment;
	private DetailFragment mDetailFragment;
	private Context mContext;

	private RelativeAsyntask mLoadRelativeAsyntask;
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private boolean mEnableLoading = false;
	private String mMediaId = null;
	private String mOldMediaId = null;
	private boolean mEnableLoadRelative = false;
	public List<RelateMediaFragment2> mFragmentList;
	private Typeface tf;
	private Button mRefreshWebViewButton;
	private ItemSelectionListener mItemSelectionListener;

	public void setOnItemSelectionListener(ItemSelectionListener alistener) {
		this.mItemSelectionListener = alistener;
	}

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

		mParentLayout = (RelativeLayout) mScrollView.findViewById(R.id.parent_of_webview);
		mOriginalWebView = (WebView)mScrollView.findViewById(R.id.webview);
		mLoadingLayout = (FrameLayout)mScrollView.findViewById(R.id.fb_loading_layout);
		mRefreshWebViewButton = (Button)mScrollView.findViewById(R.id.refresh_webview);
		mRefreshWebViewButton.setOnClickListener(this);
		showOriginWebview(false);
		//
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getChildFragmentManager());
		mViewPager = (ViewPager) mScrollView.findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between different app sections, select the corresponding tab.
				// We can also use ActionBar.Tab#select() to do this if we have a reference to the
				// Tab.
				//actionBar.setSelectedNavigationItem(position);
				/*if (position>0&&mDetailFragment!=null) {
				}*/
				//mViewPager.getChildAt(position).requestFocus();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				super.onPageScrollStateChanged(state);
				if (state==ViewPager.SCROLL_STATE_IDLE&&mDetailFragment!=null) {
					mDetailFragment.hideDescription();						
				}
			}
		});
		//
		return mScrollView;

	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//matchData();
		//TODO use it for fetch image for smart media player
		//mImageFetcher.setEnableOtherLoad(true);
		//mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);

		//define webview
		mOriginalWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = mOriginalWebView.getSettings();
		mOriginalWebView.setWebChromeClient(new MyChromeClient());

		webSettings.setJavaScriptEnabled(true);
		//originalWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");

		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setSupportMultipleWindows(true);
		/*if (mWebclient==null) {
			mWebclient = new LikeWebviewClient();			
		}*/
		mOriginalWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, "LikeWebviewClient shouldOverrideUrlLoading url: " +url);

				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.i(TAG, "LikeWebviewClient onPageFinished url: " +url);
				// Facebook redirects to this url once a user has logged in, this is a blank page so we override this
				// http://www.facebook.com/connect/connect_to_external_page_widget_loggedin.php?............
				/*if(url.startsWith("https://www.facebook.com/dialog/oauth")){
					String redirectUrl = "http://www.haivl.com/photo/928661";
					view.loadUrl(redirectUrl);
					return;
				}*/
				super.onPageFinished(view, url);

				String fromComment = "plugins%2Fcomments.php";
				String loginsuccess = "plugins/login_success.php";
				mEnableRefresh = url.contains(loginsuccess)&&url.contains(fromComment);


				showOriginWebview(true);
				/*
				if (mHandler!=null) {
					mHandler.postDelayed(FacebookPluginFragment.this, 1000);
					Toast.makeText(mActivity, "done "+url, Toast.LENGTH_SHORT).show();				
				}
				 */


				/*if (url.equals(mUrl)&&mLoadingView!=null&&mHandler!=null) {
					mHandler.postDelayed(FacebookPluginFragment.this, 5000);
				}*/
				Log.e(TAG,"url done" + url);
			}
		} );
		//		webSettings.setSupportZoom(true);
		//		webSettings.setBuiltInZoomControls(true);
		//webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
		//		originalWebView.loadUrl("file:///android_asset/01fatihah.html");
		//		originalWebView.loadUrl("http://www.haivl.com/photo/928661");





		mOriginalWebView.requestFocus(View.FOCUS_DOWN);
		/*originalWebView.setOnTouchListener(new View.OnTouchListener()
		{
			@SuppressWarnings("deprecation")
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				switch (event.getAction())
				{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus())
					{
						v.requestFocus();
					}
					break;
				}
				if (mGestureDetector==null) {
					mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

						@Override
						public boolean onDoubleTap(MotionEvent e) {
							setZoom(true);           
							return true;
						}
					});
				}
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});*/
	}

	protected void showOriginWebview(boolean show) {
		Common.setVisible(mLoadingLayout, !show);
		Common.setVisible(mOriginalWebView, show);
		Common.setVisible(mRefreshWebViewButton, show);
		Common.setVisible(mChildWebview, false);
	}

	protected void showChildWebview() {
		Common.setVisible(mLoadingLayout, false);
		Common.setVisible(mOriginalWebView, true);
		Common.setVisible(mRefreshWebViewButton, true);
		Common.setVisible(mChildWebview, true);
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
			/*if (mContent.getVisibility()==View.VISIBLE) {
				mContent.setVisibility(View.GONE);
				mDivider.setVisibility(View.GONE);
				mComma.setVisibility(View.VISIBLE);
			}
			else if (mContent.getVisibility()==View.GONE) {
				mContent.setVisibility(View.VISIBLE);
				mDivider.setVisibility(View.VISIBLE);
				mComma.setVisibility(View.GONE);
			}*/
		case R.id.refresh_webview:
			showOriginWebview(true);
			refreshWebView(true);
			return;
		default:
			break;
		}
	}




	public void updateData(MediaInfo item, ImageView imageView, Context context) {
		this.mContext = context;
		if (item!=null&&item!=mMediaItem) {
			mMediaItem = item;
			if (imageView!=null) {
				mMediaImageUrl = item.getMediaImageUrl();
				mMediaImageView = imageView;
				/*mImageFetcher.setEnableOtherLoad(true);
				mImageFetcher.loadImage(mMediaImageUrl, mMediaImageView);*/
				if (mMediaImageView instanceof ImageViewTopCrop) {
					ImageViewTopCrop image = (ImageViewTopCrop) mMediaImageView;
					image.setOriginScaleFactor(-1);
				}
				imageView.setTag(imageTagFactory.build(mMediaImageUrl, context));
				imageManager.getLoader().load(imageView);
			}

			if (mDetailFragment!=null) {
				if (tf==null) {
					tf=Typeface.createFromAsset(context.getAssets(),"Roboto-Light.ttf");
				}
				mDetailFragment.updateData(tf,item);
			}

			if (mViewPager!=null) {
				mViewPager.setCurrentItem(0);
			}

			String url =context.getString(R.string.url_domain)+context.getString(R.string.action_url_social)+"mediaId="+mMediaItem.getMediaId();
			// url = "https://m.facebook.com/";
			Log.i("mimi", url);
			
			showOriginWebview(false);
			mOriginalWebView.loadUrl(url);
			refreshWebView(false);

			mEnableLoadRelative = true;
			loadRelativeMediaList();
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
		public void destroyItem(View pView, int pIndex, Object pObject) {
			((ViewPager) pView).removeView((View)pObject);
		}

		@Override
		public Fragment getItem(int i) {
			String mediaId = mMediaItem!=null?mMediaItem.getMediaId():"";
			switch (i) {
			case 0:
				if (mDetailFragment==null) {
					mDetailFragment = new DetailFragment(tf,mMediaItem);
				}
				else{
					mDetailFragment.setMediaId(tf,mediaId);
				}
				return  mDetailFragment;

			default:
				int index = i-1;;
				if (mRelativeMediaList!=null&&index<mRelativeMediaList.size()) {
					MediaInfo media = mRelativeMediaList.get(index);
					if (media!=null) {
						Fragment fragment = getFragmentByMediaId(media.getMediaId());
						if (fragment==null) {
							mRelateMediaFragment = new RelateMediaFragment2(media,mContext);
							mRelateMediaFragment.setOnItemSelectionListener(mItemSelectionListener);
							if (mFragmentList==null) {
								mFragmentList = new ArrayList<RelateMediaFragment2>();
							}
							mFragmentList.add(mRelateMediaFragment);
							//getFragmentManager().beginTransaction().add(mRelateMediaFragment, media.getMediaId());
							return mRelateMediaFragment;
						}
						else{
							return fragment;
						}
					}
				}
				return null;
			}
		}

		@Override
		public int getCount() {
			if (mRelativeMediaList!=null) {
				int count = mRelativeMediaList.size()+1;
				return count;
			}
			return 1;
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




	final class MyChromeClient extends WebChromeClient{

		@Override
		public boolean onCreateWindow(WebView view, boolean dialog,
				boolean userGesture, Message resultMsg) {

			if (mChildWebview!=null) {
				return false;
			}
			showOriginWebview(false);
			initChildWebView();

			mChildWebview.setWebChromeClient(this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			int mg = (int) getResources().getDimension(R.dimen.media_content_item_pading_vertical);
			params.setMargins(mg, mg, mg, mg);
			mChildWebview.setLayoutParams(params);
			mParentLayout.addView(mChildWebview);
			mChildWebview.requestFocus();

			/*I think this is the main part which handles all the log in session*/
			WebView.WebViewTransport transport =(WebView.WebViewTransport)resultMsg.obj;
			transport.setWebView(mChildWebview);
			resultMsg.sendToTarget();
			return true;
		}


		/*@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (mActivity!=null) {
				mActivity.setProgress(newProgress*100);				
			}
		}*/

		@Override
		public void onCloseWindow(WebView window) {
			showOriginWebview(true);
			refreshWebView(true);
		}

	}


	public void refreshWebView(boolean reload) {
		Log.i(TAG, "refreshWebView start");
		if (mChildWebview!=null&&mParentLayout!=null) {
			mParentLayout.removeView(mChildWebview);				
			mChildWebview =null;
		}
		mEnableRefresh = false;
		if (reload) {
			//mOriginalWebView.reload();			
		}
		Log.i(TAG, "refreshWebView end");
	}

	public Fragment getFragmentByMediaId(String mediaId) {
		if (mFragmentList!=null&&mediaId!=null) {
			RelateMediaFragment2 fragment = null;
			for (int i = 0; i < mFragmentList.size(); i++) {
				fragment = mFragmentList.get(i);
				if (mediaId.equals(fragment.getMedia().getMediaId())) {
					return fragment;
				}
			}
		}
		return null;
	}


	private void initChildWebView() {
		Log.i(TAG, "initChildWebView start");

		if (mChildWebview==null) {
			mChildWebview = new WebView(mContext);			
		}
		mChildWebview.getSettings().setJavaScriptEnabled(true);

		WebSettings webSettings = mChildWebview.getSettings();
		//		webSettings.setSupportZoom(true);
		//		webSettings.setBuiltInZoomControls(true);
		//webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		mChildWebview.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, "LikeWebviewClient shouldOverrideUrlLoading url: " +url);

				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				Log.i(TAG, "LikeWebviewClient onPageFinished url: " +url);
				// Facebook redirects to this url once a user has logged in, this is a blank page so we override this
				// http://www.facebook.com/connect/connect_to_external_page_widget_loggedin.php?............
				/*if(url.startsWith("https://www.facebook.com/dialog/oauth")){
					String redirectUrl = "http://www.haivl.com/photo/928661";
					view.loadUrl(redirectUrl);
					return;
				}*/
				super.onPageFinished(view, url);

				String fromComment = "plugins%2Fcomments.php";
				String loginsuccess = "plugins/login_success.php";
				mEnableRefresh = url.contains(loginsuccess)&&url.contains(fromComment);
				showChildWebview();
				/*
				if (mHandler!=null) {
					mHandler.postDelayed(FacebookPluginFragment.this, 1000);
					Toast.makeText(mActivity, "done "+url, Toast.LENGTH_SHORT).show();				
				}
				 */


				/*if (url.equals(mUrl)&&mLoadingView!=null&&mHandler!=null) {
					mHandler.postDelayed(FacebookPluginFragment.this, 5000);
				}*/
				Log.e(TAG,"url done" + url);
			}
		} );

		Log.i(TAG, "initChildWebView end");
	}




	public void loadRelativeMediaList() {
		if (mEnableLoadRelative&&mMediaItem!=null) {
			mMediaId = mMediaItem.getMediaId();
			mEnableLoading = mMediaId!=null&&mMediaId.length()>0&&!mMediaId.equals(mOldMediaId);
			if (mEnableLoading){
				if (mRelativeMediaList!=null&&mRelativeMediaList.size()>0) {
					mRelativeMediaList.clear();
					mRelativeMediaList = null;
					mViewPager.setAdapter(mAppSectionsPagerAdapter);
					if (mAppSectionsPagerAdapter!=null) {
						mAppSectionsPagerAdapter.notifyDataSetChanged();
					}
				}
				mLoadRelativeAsyntask = new RelativeAsyntask();
				mLoadRelativeAsyntask.execute();
				mEnableLoadRelative = false;
			}
		}
	}

	private class RelativeAsyntask extends AsyncTask<Void, Void, List<MediaInfo>>{


		public RelativeAsyntask() {
			super();

		}

		@Override
		protected List<MediaInfo> doInBackground(Void... params) {
			//TODO add mediaID here
			String url = getString(R.string.url_domain)+"/media/all?limit=10&offset=0";
			List<MediaInfo> mediaList = ServerConnection.getRelativeMedia(url);
			ServerConnection.getLikeAndCommentCount(mediaList);
			//			return ServerConnection.getRelativeMedia(getString(R.string.url_domain)+data.getString(MEDIA_ID_KEY));
			return mediaList;

		}

		@Override
		protected void onPostExecute(List<MediaInfo> mediaList) {
			mRelativeMediaList = mediaList;
			if (mAppSectionsPagerAdapter!=null) {
				mAppSectionsPagerAdapter.notifyDataSetChanged();
			}
			super.onPostExecute(mediaList);
			mLoadRelativeAsyntask.cancel(false);
			mOldMediaId = mMediaId;
			/*mMediaList = mediaList;
			showRelativeMedia(mediaList);
			super.onPostExecute(mediaList);
			mLoadRelativeAsyntask.cancel(false);
			mOldMediaId = mMediaId;*/
		}

	}
}
