package vn.tbs.kcdk.fragments.contents;

import java.util.List;

import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.global.ServerConnection;
import android.os.AsyncTask;
import android.util.Log;

public class LikeAndCommentCountLoadingAsyntask extends
		AsyncTask<Void, Void, Void> {
	private static final String TAG = LikeAndCommentCountLoadingAsyntask.class.getSimpleName();
	private List<MediaInfo> mMediaList;
	private OnDoneListener mOnDoneListener;

	public interface OnDoneListener {

		void updateMediaData();
		
	}
	public LikeAndCommentCountLoadingAsyntask(List<MediaInfo> data) {
		
		this.mMediaList = data;
		
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.i(TAG, "doInBackground start");
		
		ServerConnection.getLikeAndCommentCount(mMediaList);
		return null;
		
	}

	@Override
	protected void onPostExecute(Void result) {
		Log.i(TAG, "onPostExecute start");
		
		if (mOnDoneListener!=null) {
			mOnDoneListener.updateMediaData();
			
		}
		super.onPostExecute(result);
		
		Log.i(TAG, "onPostExecute end");
	}

	public void setOnDoneListener(
			OnDoneListener listener) {
		this.mOnDoneListener = listener;
		
	}
}
