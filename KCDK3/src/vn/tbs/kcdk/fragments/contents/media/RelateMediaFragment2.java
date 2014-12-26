package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.KCDKApplication;
import vn.tbs.kcdk.R;
import vn.tbs.kcdk.SmartKCDKActivity;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment.ItemSelectionListener;
import vn.tbs.kcdk.global.Common;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.novoda.imageloader.core.ImageManager;
import com.novoda.imageloader.core.model.ImageTagFactory;

public class RelateMediaFragment2 extends Fragment implements OnClickListener  {

	private ImageManager imageManager;
	private ImageTagFactory imageTagFactory;
	private boolean mEnableLoading = false;
	private MediaInfo mMedia = null;
	private MediaInfo mOldMediaId = null;
	private View mView;
	private Context mContext;
	private ItemSelectionListener mItemListerner;

	public RelateMediaFragment2(MediaInfo media, Context context) {
		//this.mImageFetcher = aImageFetcher;
		this.mMedia = media;
		this.mContext = context;
		imageManager = KCDKApplication.getImageLoader();
		imageTagFactory = KCDKApplication.getImageTagFactory();
	}


	public RelateMediaFragment2() {
		super();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView =  inflater.inflate(R.layout.media_content_item_row, null);
		bindView(mContext);
		View coverView = mView.findViewById(R.id.cover_view);
		if (coverView!=null) {
			coverView.setOnClickListener(this);
		}
		return mView;
	}

	private void bindView(Context context) {
		if (mView!=null) {
			Common.bindTextValue(mView,this.mMedia,false,SmartKCDKActivity.sFont);
			ImageView imageView  = (ImageView) mView.findViewById(R.id.media_item_image);
			imageView.setTag(imageTagFactory.build(mMedia.getMediaImageThumbUrl(), context));
			imageManager.getLoader().load(imageView);			
		}
	}


	private void setRelativeLayoutVisibility(View view,int visibility) {

		if (view!=null) {
			view.setVisibility(visibility);
		}		
	}

	public MediaInfo getMedia() {
		return mMedia;
	}


	public void setMediaId(MediaInfo media, Context context) {
		this.mOldMediaId = this.mMedia;
		this.mMedia = media;
		//bindView(context);
	}

	public void setOnItemSelectionListener(ItemSelectionListener alistener) {
		this.mItemListerner = alistener;
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cover_view:
			if (mItemListerner!=null) {
				mItemListerner.doItemSelection(mMedia,true,false);			
			}
			break;

		default:
			break;
		}
	}
}
