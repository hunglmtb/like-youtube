package vn.tbs.kcdk.fragments.contents;

import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_AUDIO;
import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_VIDEO;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.KCDKApplication;
import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.global.Common;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.android.bitmapfun.util.ImageFetcher;
import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class MediaAdapter extends BaseAdapter {
	private static final String TAG = MediaAdapter.class.getSimpleName();

	protected List<MediaInfo> mMediaList;
	private int mSelectedPosition = 0;
	private Context mContext;
	private String mMediaGroupMode="";
	private int mLimit = 10;
	private int mOffset = 0;
	private ImageManager imageManager;
	private ImageTagFactory imageTagFactory;
	
	//lrucache 
	private ImageFetcher mImageFetcher;

	private boolean mIsHistoryAdapterType = false;

	private String mCategoryKeyString = "";
	private String mCategoryName = "";

	public MediaAdapter(List<MediaInfo> aMediaList, Context context, ImageFetcher imageFetcher) {
		this.mMediaList = aMediaList;
		this.mContext = context;
		this.mImageFetcher = imageFetcher;
        imageManager = KCDKApplication.getImageLoader();
        imageTagFactory = KCDKApplication.getImageTagFactory();
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
				//convertView = inflater.inflate(R.layout.media_content_item_with_header_row, null);					
				convertView = inflater.inflate(R.layout.media_content_item_row_with_header, null);					
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
						convertView.getPaddingRight(),
						convertView.getPaddingRight(),
						0);
			}
			else if (position==getCount()-1) {
				convertView.setPadding(convertView.getPaddingLeft(),
						0,
						convertView.getPaddingRight(),
						convertView.getPaddingRight());
			}
			else {
				convertView.setPadding(convertView.getPaddingLeft(),
						0,
						convertView.getPaddingRight(),
						0);
			}
			// Now handle the main ImageView thumbnails
			ImageView imageView = (ImageView) convertView.findViewById(R.id.media_item_image);
			//TODO update font != null
			Common.bindTextValue(convertView,media,mIsHistoryAdapterType,null);
			//mImageFetcher.loadImage(media.getMediaImageThumbUrl(), imageView);
			imageView.setTag(imageTagFactory.build(media.getMediaImageUrl(), mContext));
			imageManager.getLoader().load(imageView);
			Log.e(TAG, "kaka "+media.getMediaImageUrl());
			

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


	public void updateOffset(int size) {
		mOffset += size;
	}


	public boolean resetItemList(int mOffset, CategoryRow item) {
		boolean result = false;
		if (item!=null) {
			this.mOffset = mOffset;
			mCategoryKeyString = item.getCategoryKeyString();
			mCategoryName = item.getCategoryName();
			if (mMediaList!=null) {
				mMediaList.clear();
			}
			result = true;
		}else{
			result = mMediaList==null||mMediaList.size()<=0;
		}
		
		return result;
		
	}


	public String[] getUrlParams() {
		return new String[] {mCategoryKeyString,mMediaGroupMode,String.valueOf(mLimit),String.valueOf(mOffset)};
	}
}
