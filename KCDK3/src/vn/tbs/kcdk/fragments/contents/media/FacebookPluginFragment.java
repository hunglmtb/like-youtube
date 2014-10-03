package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


public class FacebookPluginFragment extends Fragment implements Runnable  {

	private static final String TAG = FacebookPluginFragment.class.getName();
	private static final int PROMT_FACEBOOK_WIDTH = 450;
	protected WebView originalWebView;
	private WebView childView =null;
	private WebViewClient mWebclient = null;
	protected RelativeLayout mParentLayout;
	protected String mMediaId = "";
	protected String mPluginHtml = "likeplugin.html";
	private Activity mActivity;
	protected ProgressBar mLoadingView;
	private final Handler mHandler = new Handler();
	protected boolean isLikePlugin = true;
	private FrameLayout mLoadingLayout;
	private boolean mEnableRefresh = false;
	private boolean mEnableZoom = false;
	private GestureDetector mGestureDetector;



	public FacebookPluginFragment(String mediaId) {
		this.mMediaId = mediaId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setEnableZoom();

	}

	private void setEnableZoom() {
		Log.i(TAG, "setEnableZoom start");

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int width = displayMetrics.widthPixels;
		mEnableZoom = width<PROMT_FACEBOOK_WIDTH;
		Log.i(TAG, "setEnableZoom end");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mParentLayout = (RelativeLayout) inflater.inflate(R.layout.media_content_description_layout, null);
		originalWebView = (WebView)mParentLayout.findViewById(R.id.webview);
		mLoadingLayout = (FrameLayout)mParentLayout.findViewById(R.id.fb_loading_layout);
		mLoadingView = (ProgressBar)mParentLayout.findViewById(R.id.fb_loading_view);
		return mParentLayout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (originalWebView!=null) {
			originalWebView.saveState(outState);			
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mActivity = getActivity();
		initOriginalWebView(savedInstanceState);
	}

	private void initOriginalWebView(Bundle savedInstanceState) {
		Log.i(TAG, "initOriginalWebView start");

		//define webview
		originalWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = originalWebView.getSettings();
		originalWebView.setWebChromeClient(new MyChromeClient());

		webSettings.setJavaScriptEnabled(true);
		originalWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");

		//		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setSupportMultipleWindows(true);
		if (mWebclient==null) {
			mWebclient = new LikeWebviewClient();			
		}
		originalWebView.setWebViewClient(mWebclient );
		//		webSettings.setSupportZoom(true);
		//		webSettings.setBuiltInZoomControls(true);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
		//		originalWebView.loadUrl("file:///android_asset/01fatihah.html");
		//		originalWebView.loadUrl("http://www.haivl.com/photo/928661");

		if (savedInstanceState!=null) {
			originalWebView.restoreState(savedInstanceState);
		}
		else{
			loadDataFromUrl();
		}


		originalWebView.requestFocus(View.FOCUS_DOWN);
		originalWebView.setOnTouchListener(new View.OnTouchListener()
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
		});

		Log.i(TAG, "initOriginalWebView end");
	}


	public void loadDataFromUrl() {
		Log.i(TAG, "loadDataFromUrl start");

		String url = null;
  		if (isLikePlugin ) {
  			url =getActivity().getString(R.string.url_domain)+getActivity().getString(R.string.action_url_social)+"mediaId="+mMediaId;
		}
		else{
			url =getActivity().getString(R.string.url_domain)+getActivity().getString(R.string.action_url_social)+"mediaId="+mMediaId;
//			String url = "http://hunglmbk.com:8888/commentplugin.html";
			
			/*
			try {
				BufferedReader reader =new BufferedReader(new InputStreamReader(mActivity.getAssets().open(mPluginHtml)));
				String line;
				String html = "";
				while((line = reader.readLine()) != null) {
					html += line;
				}
				reader.close();
				CharSequence target ="mPluginUrl";
				CharSequence replacement = mUrl;
				html = html.replace(target, replacement);
				Log.i(TAG, "initOriginalWebView html = "+html);

				//				originalWebView.loadUrl(mUrl);
				originalWebView.loadDataWithBaseURL(mUrl , html, "text/html", null, null);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}*/
			}		

		originalWebView.loadUrl(url);
		Log.i(TAG, "loadDataFromUrl "+url);
		Log.i(TAG, "loadDataFromUrl end");
	}

	private void initChildWebView() {
		Log.i(TAG, "initChildWebView start");

		mLoadingLayout.setVisibility(View.VISIBLE);				

		if (childView==null) {
			childView = new WebView(mActivity);			
		}
		childView.getSettings().setJavaScriptEnabled(true);

		WebSettings webSettings = childView.getSettings();
		//		webSettings.setSupportZoom(true);
		//		webSettings.setBuiltInZoomControls(true);
		webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		if (mWebclient==null) {
			mWebclient = new LikeWebviewClient();			
		}
		childView.setWebViewClient(mWebclient);

		Log.i(TAG, "initChildWebView end");
	}
	private class LikeWebviewClient extends WebViewClient {        

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

			if (mLoadingLayout!=null) {
				mLoadingLayout.setVisibility(View.GONE);				
			}
			setZoomLevel(childView,url);
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
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i(TAG, "LikeWebviewClient shouldOverrideUrlLoading url: " +url);

			return false;
		}

	}



	final class MyChromeClient extends WebChromeClient{

		@Override
		public boolean onCreateWindow(WebView view, boolean dialog,
				boolean userGesture, Message resultMsg) {
			Log.i("MyChromeClient", "onCreateWindow start");

			if (childView!=null) {
				return false;
			}

			initChildWebView();

			childView.setWebChromeClient(this);
			childView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

			mParentLayout.addView(childView);


			childView.requestFocus();
			originalWebView.setVisibility(View.GONE);

			/*I think this is the main part which handles all the log in session*/
			WebView.WebViewTransport transport =(WebView.WebViewTransport)resultMsg.obj;
			transport.setWebView(childView);
			resultMsg.sendToTarget();

			Log.i("MyChromeClient", "onCreateWindow end");
			return true;
		}


		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (mActivity!=null) {
				mActivity.setProgress(newProgress*100);				
			}
		}

		@Override
		public void onCloseWindow(WebView window) {
			Log.i("MyChromeClient", "onCloseWindow start");

			mParentLayout.removeViewAt(mParentLayout.getChildCount()-1);
			childView =null;
			originalWebView.setVisibility(View.VISIBLE);
			originalWebView.requestFocus();

			refreshWebView();
			mEnableRefresh = false;

			Log.i("MyChromeClient", "onCloseWindow end");
		}

	}



	@Override
	public void run() {
		Log.i(TAG, "run start");

		if (mLoadingLayout!=null) {
			mLoadingLayout.setVisibility(View.GONE);				
		}
		originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);				

		Log.i(TAG, "run end");
	}

	public void setZoomLevel(WebView webview, String url) {
		Log.i(TAG, "setZoomLevel start");

		if (url!=null&&webview!=null) {
			if (mEnableZoom) {
				String confirmLikeUrl = "plugins/error/confirm/like";
				String logindone = "plugins/close_popup.php";
				String fromLike = "plugins%2Flike.php";
				if (url.contains(confirmLikeUrl)) {
					webview.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);				
					originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);				
				}
				else if (url.contains(logindone)&&(url.contains(fromLike))) {
					originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);									
				}
				else {
					webview.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);				
					originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
				}
			}

			String fromComment = "plugins%2Fcomments.php";
			String loginsuccess = "plugins/login_success.php";
			mEnableRefresh = url.contains(loginsuccess)&&url.contains(fromComment);
		}


		Log.i(TAG, "setZoomLevel end");

	}
	public class WebAppInterface {
		Context mContext;

		/** Instantiate the interface and set the context */
		WebAppInterface(Context c) {
			mContext = c;
		}

		/** Show a toast from the web page */
		@JavascriptInterface
		public void finishLoading() {
			setZoom(false);
		}
	}



	public void refreshWebView() {
		Log.i(TAG, "refreshWebView start");

		if (mEnableRefresh&&originalWebView!=null) {
			mLoadingLayout.setVisibility(View.VISIBLE);
			originalWebView.reload();
		}

		Log.i(TAG, "refreshWebView end");
	}

	public void setZoom(boolean isDoubleTap) {
		Log.i(TAG, "setZoom start");
		if (mEnableZoom) {
			if (originalWebView.getSettings().getDefaultZoom()==WebSettings.ZoomDensity.MEDIUM) {
				originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);				
			}
			else if (isDoubleTap) {
				originalWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);				

			}		
		}

		Log.i(TAG, "setZoom end");
	}

	public void shareMediaLink() {
		Log.i(TAG, "shareMediaLink start");

		if (originalWebView!=null) {
			originalWebView.loadUrl("https://www.facebook.com/sharer.php?u=http://www.haivl.com/photo/973665");
		}

		Log.i(TAG, "shareMediaLink end");
	}

	public void setMediaId(String mediaId) {
		this.mMediaId = mediaId;
	}

}
