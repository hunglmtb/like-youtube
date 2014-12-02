package vn.tbs.kcdk;

import vn.tbs.kcdk.global.Common;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

public class AdditionalFragment extends Fragment {

	protected RelativeLayout mParentLayout;
	private WebView mOriginalWebView;
	private WebView mChildWebview =null;
	private FrameLayout mLoadingLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.e("bobo", "bobob onCreateView");
		mParentLayout =  (RelativeLayout) inflater.inflate(R.layout.addition_fragment_layout, null);
		mOriginalWebView = (WebView) mParentLayout.findViewById(R.id.addition_webview);
		mLoadingLayout = (FrameLayout)mParentLayout.findViewById(R.id.fb_loading_layout);
		showOriginWebview(false);
		mParentLayout.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});

		return mParentLayout;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//define webview
		mOriginalWebView.setHorizontalScrollBarEnabled(false);
		WebSettings webSettings = mOriginalWebView.getSettings();
		mOriginalWebView.setWebChromeClient(new MyChromeClient());

		webSettings.setJavaScriptEnabled(true);
		//originalWebView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");

		webSettings.setAppCacheEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setSupportMultipleWindows(true);
		mOriginalWebView.requestFocus(View.FOCUS_DOWN);
		mOriginalWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				showOriginWebview(true);
			}
		} );
		Context context = getActivity();
		String url =context.getString(R.string.url_domain)+context.getString(R.string.action_url_authenticate);
		mOriginalWebView.loadUrl(url);
	}



	private void initChildWebView() {

		if (mChildWebview==null) {
			mChildWebview = new WebView(getActivity());			
		}
		mChildWebview.getSettings().setJavaScriptEnabled(true);

		WebSettings webSettings = mChildWebview.getSettings();
		//		webSettings.setSupportZoom(true);
		//		webSettings.setBuiltInZoomControls(true);
		//webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

		mChildWebview.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				showChildWebview();
			}
		} );
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
			refreshWebView(false);
		}

	}


	public void refreshWebView(boolean reload) {
		if (mChildWebview!=null&&mParentLayout!=null) {
			mParentLayout.removeView(mChildWebview);				
			mChildWebview =null;
		}
		if (reload&&mOriginalWebView!=null) {
			mOriginalWebView.reload();			
		}
	}

	protected void showOriginWebview(boolean show) {
		Common.setVisible(mLoadingLayout, !show);
		Common.setVisible(mOriginalWebView, show);
		Common.setVisible(mChildWebview, false);
	}

	protected void showChildWebview() {
		Common.setVisible(mLoadingLayout, false);
		Common.setVisible(mOriginalWebView, true);
		Common.setVisible(mChildWebview, true);
	}
}
