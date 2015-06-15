package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.MAX_NUM_RELATIVE_MEDIA;
import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_AUDIO;
import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_VIDEO;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import vn.tbs.kcdk.global.Common;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.android.bitmapfun.util.ImageFetcher;

public class DetailMediaAdapter extends BaseAdapter {
	private static final String TAG = DetailMediaAdapter.class.getSimpleName();
	private Context mContext;
	private List<MediaInfo> mRelativeMediaList;

	private ImageView mMediaImageView;
	private View mMoreSuggestion;
	private View mContentLayout;
	private View mProgressBar;
	private ImageFetcher mImageFetcher;
	private boolean mIsLoading;
	private boolean hasMoreSuggestion = false;
	private boolean isHiden = true;
	

	public DetailMediaAdapter(Context context, ImageFetcher imageFetcher) {
		this.mContext = context;
		this.mImageFetcher = imageFetcher;
		this.mIsLoading = true;
	}

	public void setRelativeMediaList(List<MediaInfo> relativeMediaList) {
		if (relativeMediaList!=null&&relativeMediaList.size()>MAX_NUM_RELATIVE_MEDIA) {
			List<MediaInfo> list = new ArrayList<MediaInfo>();
			for (int i = 0; i < MAX_NUM_RELATIVE_MEDIA; i++) {
				list.add(relativeMediaList.get(i));
			}
			this.mRelativeMediaList = list;
		}
		else{
			this.mRelativeMediaList = relativeMediaList;			
		}
	}
	
	public boolean isLoading() {
		return mIsLoading;
	}

	public void setIsLoading(boolean mIsLoading) {
		this.mIsLoading = mIsLoading;
	}
	

	public boolean isHiden() {
		return isHiden;
	}

	public void setHiden(boolean isHiden) {
		this.isHiden = isHiden;
	}

	@Override
	public int getCount() {
		Log.i(TAG, "getCount start");

		int count = 0;
		if (mMediaImageView!=null) {
			count++;
		}
		if (mContentLayout!=null) {
			count++;
		}
		if (mIsLoading&&mProgressBar!=null) {
			count++;
		}
		if (mRelativeMediaList!=null) {
			int size = mRelativeMediaList.size();
			if (size>2&&mMoreSuggestion!=null) {
				hasMoreSuggestion = true;
				count++;				
			}
			if (isHiden&&size>2) {
				size = 2;
			}
			count+=size;
		}
		return count;

	}

	@Override
	public Object getItem(int pos) {
		return null;

	}

	@Override
	public long getItemId(int position) {
		return 0;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.i(TAG, "getView start");

		//for image and content
		switch (position) {
		case 0:
			mMediaImageView.setTag(0);
			return mMediaImageView;
		case 1:
			mContentLayout.setTag(1);
			return mContentLayout;
		case 2:
			if (mIsLoading) {
				mProgressBar.setTag(2);
				return mProgressBar;				
			}
			else break;
		default:
			break;
		}
		
		//for relative media
		int lastIndex = getCount()-1;

		if (position==lastIndex&&hasMoreSuggestion) {
			return mMoreSuggestion;
		}
		else{
			int pos = position-2;
			final MediaInfo media = mRelativeMediaList.get(pos);

			if (convertView==null||convertView.getTag()==null) {
				convertView = initConvertView(media);
			}
			else{
				int posTag = (Integer) convertView.getTag();
				if (posTag<=2||posTag>=lastIndex) {
					convertView = initConvertView(media);
				}
			}
			
			if (!hasMoreSuggestion&&pos==mRelativeMediaList.size()-1) {
				View endView = convertView.findViewById(R.id.end_view);
				if (endView!=null) {
					endView.setVisibility(View.VISIBLE);
				}
			}
			if (media!=null) {
				// Now handle the main ImageView thumbnails
				ImageView imageView = (ImageView) convertView.findViewById(R.id.media_item_image);
				Common.bindTextValue(convertView,media,false,SmartKCDKActivity.sFont);
				mImageFetcher.setEnableOtherLoad(false);
				mImageFetcher.loadImage(media.getMediaImageThumbUrl(), imageView);
			}

			convertView.setTag(position);
			return convertView;
		}
	}

	private View initConvertView(MediaInfo media) {
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		switch (media.getMediaType()) {
		case MEDIA_TYPE_AUDIO:
			//category then select category layout
			return inflater.inflate(R.layout.media_content_item_row, null);					
		case MEDIA_TYPE_VIDEO:
			//header then select header layout
			return inflater.inflate(R.layout.category_header_row, null);

		default:
			return inflater.inflate(R.layout.media_content_item_with_header_row, null);					
		}
		
	}

	public void setStaticView(ImageView imageview, View contentview,
			View moreSuggestion, View progressBar) {
		this.mMediaImageView = imageview;
		this.mContentLayout = contentview;
		this.mMoreSuggestion = moreSuggestion;
		this.mProgressBar = progressBar;

	}
}
