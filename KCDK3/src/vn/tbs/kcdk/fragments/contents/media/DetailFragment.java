package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.R;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailFragment extends Fragment implements OnClickListener {

	private static final String TAG = DetailFragment.class.getName();
	protected View mParentLayout;
	protected String mMediaId = "";
	private boolean mEnableRefresh = false;
	private boolean mEnableLoading = false;
	private String mOldMediaId = null;
	private MediaInfo mMedia = null;

	private TextView mContent;
	private TextView mTitleTextView;
	private TextView mAuthorTextView;
	private TextView mViewCountTextView;
	private TextView mSpeakerTextView;
	private TextView mPublishedDateTextView;
	private View mContentLayout;
	private View mDivider;
	private View mComma;

	private Context mContext;
	private Typeface mTypeFace;

	public void setEnableLoading(boolean mEnableLoading) {
		this.mEnableLoading = mEnableLoading;
	}

	public DetailFragment(Typeface tf, MediaInfo media) {
		this.mTypeFace = tf;
		this.mMedia = media;
		this.mMediaId = media!=null?media.getMediaId():"";
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.e("bobo", "bobob onCreateView");
		mParentLayout =  inflater.inflate(R.layout.detail_media_layout, null);
		mComma = mParentLayout.findViewById(R.id.three_comma_tv);
		mDivider = mParentLayout.findViewById(R.id.divider);
		mContentLayout = mParentLayout.findViewById(R.id.vg_cover);
		if (mContentLayout!=null) {
			mContentLayout.setOnClickListener(this);
		}

		mContent = (TextView)mParentLayout.findViewById(R.id.media_content_tv);
		mTitleTextView = (TextView)mParentLayout.findViewById(R.id.media_title_tv);
		mAuthorTextView = (TextView)mParentLayout.findViewById(R.id.media_author_tv);
		mViewCountTextView = (TextView)mParentLayout.findViewById(R.id.media_viewcount_tv);
		mSpeakerTextView = (TextView)mParentLayout.findViewById(R.id.media_speaker_tv);
		mPublishedDateTextView = (TextView)mParentLayout.findViewById(R.id.media_publisheddate_tv);
		updateData(this.mTypeFace, this.mMedia);
		return mParentLayout;
	}


	public void setMediaId(Typeface tf, String mediaId) {
		this.mOldMediaId = this.mMediaId;
		this.mMediaId = mediaId;
		this.mTypeFace = tf;
		updateData(tf, this.mMedia);
	}

	public void updateData(Typeface tf, MediaInfo item) {
		if (tf!=null) {
			this.mTypeFace = tf;
			this.mMedia = item;
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
	

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick start");

		switch (v.getId()) {
		case R.id.vg_cover:
			if (mContent.getVisibility()==View.VISIBLE) {
				mContent.setVisibility(View.GONE);
				mDivider.setVisibility(View.GONE);
				mComma.setVisibility(View.VISIBLE);
			}
			else if (mContent.getVisibility()==View.GONE) {
				mContent.setVisibility(View.VISIBLE);
				mDivider.setVisibility(View.GONE);
				mComma.setVisibility(View.GONE);
			}
			return;
		default:
			break;
		}
	}

	public void hideDescription() {
		mContent.setVisibility(View.GONE);
		mDivider.setVisibility(View.GONE);
		mComma.setVisibility(View.VISIBLE);
	}

/*	public void matchData(Typeface tf2) {
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
	}*/

}
