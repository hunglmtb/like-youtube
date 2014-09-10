package vn.tbs.kcdk.fragments.contents;

import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_AUDIO;
import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_VIDEO;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.global.Common;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.android.bitmapfun.util.ImageFetcher;

public class MediaAdapter extends BaseAdapter {
	private static final String TAG = MediaAdapter.class.getSimpleName();

	protected List<MediaInfo> mMediaList;
	private int mSelectedPosition = 0;
	private Context mContext;
	private String mMediaGroupMode="";
	private int mLimit = 10;
	private int mOffset = 0;

	//lrucache 
	private ImageFetcher mImageFetcher;

	private boolean mIsHistoryAdapterType = false;

	public MediaAdapter(List<MediaInfo> mCategories, Context context, ImageFetcher imageFetcher) {
		this.mMediaList = mCategories;
		this.mContext = context;
		this.mImageFetcher = imageFetcher;
	}

	
	public void setIsHistoryAdapterType(boolean mIsHistoryAdapterType) {
		this.mIsHistoryAdapterType = mIsHistoryAdapterType;
	}


	public void setMediaList(List<MediaInfo> mMediaList) {
		this.mMediaList = mMediaList;
	}

	public int getSelectedPosition() {
		return mSelectedPosition;
	}

	@Override
	public int getCount() {
		if (mMediaList!=null) {
			return mMediaList.size();
		}
		return 0;
	}

	@Override
	public MediaInfo getItem(int pos) {
		if (mMediaList!=null) {
			return mMediaList.get(pos);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		MediaInfo media = mMediaList.get(position);

		if (convertView == null) { 
			LayoutInflater inflater = LayoutInflater.from(mContext);
			switch (media.getMediaType()) {
			case MEDIA_TYPE_AUDIO:
				//category then select category layout
				convertView = inflater.inflate(R.layout.media_content_item_with_header_row, null);					
				break;
			case MEDIA_TYPE_VIDEO:
				//header then select header layout
				convertView = inflater.inflate(R.layout.category_header_row, null);

			default:
				convertView = inflater.inflate(R.layout.media_content_item_with_header_row, null);					
				break;
			}
		}
		else{
			//TODO LATER
		}
		if (media!=null) {
			if (position==0) {
				convertView.setPadding(convertView.getPaddingLeft(),
						convertView.getPaddingBottom(),
						convertView.getPaddingRight(),
						convertView.getPaddingBottom());
			}
			else{
				convertView.setPadding(convertView.getPaddingLeft(),
						0,
						convertView.getPaddingRight(),
						convertView.getPaddingBottom());
			}
			// Now handle the main ImageView thumbnails
			ImageView imageView = (ImageView) convertView.findViewById(R.id.media_item_image);
			//TODO update font != null
			Common.bindTextValue(convertView,media,mIsHistoryAdapterType,null);
			mImageFetcher.loadImage(media.getMediaImageThumbUrl(), imageView);
		}

		return convertView;
	}


	public void setSelectedPosition(int mSelectedPosition, boolean notify) {
		Log.i(TAG, "setSelectedPosition start" + "position: "+ mSelectedPosition +" notify: "+notify);

		MediaInfo item = getItem(mSelectedPosition);
		if (item!=null&&item.getMediaType()==MEDIA_TYPE_AUDIO) {
			this.mSelectedPosition = mSelectedPosition;
			if (notify) {
				this.notifyDataSetChanged();							
			}
		}

		Log.i(TAG, "setSelectedPosition end");
	}


	public boolean hasMoreData() {

		if (mMediaList!=null) {
			mOffset += mLimit;
			return mOffset<=mMediaList.size();
		}
		else{
			mMediaList = new ArrayList<MediaInfo>();
			return true;
		}

	}


	public String[] getUrlParams() {
		return new String[] {mMediaGroupMode,String.valueOf(mLimit),String.valueOf(mOffset)};

	}
}