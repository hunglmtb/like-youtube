package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MAX_NUM_RELATIVE_MEDIA;

import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import vn.tbs.kcdk.global.Common;
import vn.tbs.kcdk.global.ServerConnection;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.example.android.bitmapfun.util.LoadingDoneListener;

public class RelateMediaFragment extends Fragment implements  LoadingDoneListener, OnClickListener {

	private LinearLayout mRelativeMediaLayout;
	private Button mMoreSuggestion;
	private RelativeAsyntask mLoadRelativeAsyntask;
	private List<MediaInfo> mRelativeMediaList;
	private ImageFetcher mImageFetcher;
	private View mEndView;
	private ProgressBar mProgressBar;
	private boolean mIsMore = true;

	public RelateMediaFragment(ImageFetcher aImageFetcher) {
		this.mImageFetcher = aImageFetcher;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		//mIsHistoryFragmentType = false;
		//mEnablePinnedHeader = false;
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.media_relative_layout, null);

		mMoreSuggestion = (Button)view.findViewById(R.id.more_relative_media);
		mRelativeMediaLayout = (LinearLayout)view.findViewById(R.id.relative_media_layout);
		mMoreSuggestion.setOnClickListener(this);
		mProgressBar = (ProgressBar)view.findViewById(R.id.relative_media_progressBar);
		mProgressBar.setVisibility(View.VISIBLE);
		mEndView = view.findViewById(R.id.end_view);

		hide2SampleMedia();

		return view;
	}

	private void hide2SampleMedia() {

		setRelativeLayoutVisibility(mRelativeMediaLayout,View.VISIBLE);
		mMoreSuggestion.setVisibility(View.GONE);


	}

	private void setRelativeLayoutVisibility(View view,int visibility) {

		if (view!=null) {
			view.setVisibility(visibility);
		}		
	}

	@Override
	public void loadOtherComponent() {

		loadFromServer();

	}
	private void loadFromServer() {

		if (mLoadRelativeAsyntask==null||mRelativeMediaList==null){
			mLoadRelativeAsyntask = new RelativeAsyntask();			
			mLoadRelativeAsyntask.execute();			
		}
		else{
			showRelativeMedia(mRelativeMediaList);				
		}

	}

	public void showRelativeMedia(List<MediaInfo> result) {
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

	}
	private class RelativeAsyntask extends AsyncTask<Void, Void, List<MediaInfo>>{

		@Override
		protected List<MediaInfo> doInBackground(Void... params) {
			//			return ServerConnection.getRelativeMedia(getString(R.string.url_domain)+data.getString(MEDIA_ID_KEY));
			return ServerConnection.getRelativeMedia(getString(R.string.url_domain)+"/media/all?limit=10&offset=0");

		}

		@Override
		protected void onPostExecute(List<MediaInfo> mediaList) {
			mRelativeMediaList = mediaList;
			showRelativeMedia(mediaList);
			super.onPostExecute(mediaList);
			mLoadRelativeAsyntask.cancel(false);
		}

	}
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		
		case R.id.more_relative_media:
			mMoreSuggestion.setEnabled(false);
			if (isAddMore()) {
				showRelativeMedia(mRelativeMediaList);		
				//mViewPager.getLayoutParams().height = 600;
			}
			else{
				hideRelativeMedia();
				//mViewPager.getLayoutParams().height = 400;
			}
			//mViewPager.requestLayout();
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

		if (mRelativeMediaLayout!=null) {
			int visibility = mIsMore?View.VISIBLE:View.GONE;
			
			for (int i = 3; i < mRelativeMediaLayout.getChildCount(); i++) {
				mRelativeMediaLayout.getChildAt(i).setVisibility(visibility);
			}
		}
	}

	private boolean isAddMore() {

		return (mRelativeMediaLayout!=null&&mRelativeMediaLayout.getChildCount()<MAX_NUM_RELATIVE_MEDIA);

	}


}
