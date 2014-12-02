package vn.tbs.kcdk.fragments.contents;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ServerConnection;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;

import com.example.android.bitmapfun.util.EndlessAdapter;

public class EndlessLoadAdapter extends EndlessAdapter {
	private RotateAnimation rotate=null;
	private View pendingView=null;
	private Context mContext;
	private PinnedHeaderMediaAdapter mPinnedHeaderMediaAdapter;
	private List<MediaInfo> mMoreData = null;
	private boolean mEnableLoading = true;



	public void setEnableLoading(boolean enableLoading) {
		this.mEnableLoading = enableLoading;
	}

	EndlessLoadAdapter(Context ctxt, ArrayList<Integer> list) {
		super(new ArrayAdapter<Integer>(ctxt,
				R.layout.row,
				android.R.id.text1,
				list));
		this.mContext = ctxt;
		initRotate();

	}

	private void initRotate() {
		rotate=new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(600);
		rotate.setRepeatMode(Animation.RESTART);
		rotate.setRepeatCount(Animation.INFINITE);
	}

	public EndlessLoadAdapter(PinnedHeaderMediaAdapter wrapped, Context ctx) {
		super(wrapped);
		this.mPinnedHeaderMediaAdapter = wrapped;
		this.mContext = ctx;
		initRotate();
	}

	public PinnedHeaderMediaAdapter getPinnedHeaderMediaAdapter() {
		return mPinnedHeaderMediaAdapter;
	}

	@Override
	protected View getPendingView(ViewGroup parent) {
		View row=LayoutInflater.from(parent.getContext()).inflate(R.layout.row, null);

		pendingView=row.findViewById(R.id.pending_view);
		if (pendingView!=null&&mPinnedHeaderMediaAdapter.getCount()<=0) {
			pendingView.setPadding(pendingView.getPaddingLeft(),
					pendingView.getPaddingBottom(),
					pendingView.getPaddingRight(),
					pendingView.getPaddingBottom());
		}


		pendingView=row.findViewById(R.id.throbber);
		pendingView.setVisibility(View.VISIBLE);
		startProgressAnimation();

		return(row);
	}

	@Override
	protected boolean cacheInBackground() {
		//		SystemClock.sleep(3000);

		if (mPinnedHeaderMediaAdapter!=null) {
			//load more data
			Log.i("kuku", "cacheInBackground start");
			if (mEnableLoading&&mPinnedHeaderMediaAdapter!=null&&mPinnedHeaderMediaAdapter.getCount()<35) {
				String url = Common.getConnectUrl(mContext,Common.URL_MEDIA_LIST_MODE,mPinnedHeaderMediaAdapter.getUrlParams());
				Log.i("kuku", "url "+url);

				mMoreData = ServerConnection.loadMediaList(url);
				if (mMoreData==null||mMoreData.size()<=0) {
					Log.i("kuku", "cacheInBackground end mMoreData null false");
					return false;
				}
				else{
					Log.i("kuku", "cacheInBackground end hasMoreData");
					mPinnedHeaderMediaAdapter.updateOffset(mMoreData.size());
					return true;
				}
			}

		}
		Log.i("kuku", "cacheInBackground end false");

		return false;
	}

	@Override
	protected void appendCachedData() {
		//load more data
		if (mPinnedHeaderMediaAdapter!=null&&mPinnedHeaderMediaAdapter.getCount()<35) {
			mPinnedHeaderMediaAdapter.addMoreData(mMoreData);
		}
	}

	void startProgressAnimation() {
		if (pendingView!=null) {
			pendingView.startAnimation(rotate);
		}
	}

	public void reload(CategoryRow item) {
		mEnableLoading = mPinnedHeaderMediaAdapter.resetItemList(0,item);
		if (mEnableLoading) {
			super.restartAppending();			
		}
	}
}