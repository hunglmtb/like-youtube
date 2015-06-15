package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.*;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class MediaContentPagerAdapter extends FragmentStatePagerAdapter {
	private static final String TAG = MediaContentPagerAdapter.class.getSimpleName();

	List<Fragment> mViewFragments;

	private String[] mTitle;

	private Fragment mfragment = null;

	private Bundle mMediaParams;

	private Context mContext;

	public MediaContentPagerAdapter(Fragment fg, Bundle params) {
		super(fg.getChildFragmentManager());
		this.mfragment  = fg;
		this.mMediaParams = params;
		this.mContext = fg.getActivity();
		loadChildViewOfViewPage();
	}

	private void loadChildViewOfViewPage() {
		Log.i(TAG, "loadChildViewOfViewPage start");
		
		mViewFragments = new ArrayList<Fragment>();
		MediaDescriptionFragment desfg = new MediaDescriptionFragment();
//		DescriptionFragment desfg = new DescriptionFragment();
		mViewFragments.add(desfg);
//		Bundle dbundle  = new Bundle(mMediaParams);
		//TODO
		Bundle dbundle  = new Bundle();
		dbundle.putString(TITLE_BUNDLE, "DESCRIPTION");
		desfg.setArguments(mfragment.getArguments());
		
		
		CommentFragment commentfg = new CommentFragment("test");
		mViewFragments.add(commentfg);
		//TODO
//		Bundle bundle  = new Bundle(mMediaParams);
		Bundle bundle  = new Bundle();
		bundle.putString(TITLE_BUNDLE, "COMMENT");
		//bundle.putString(MEDIA_ID_KEY, "COMMENT");
		commentfg.setArguments(mfragment.getArguments());
		
		mTitle = new String[] {mContext.getString(R.string.description_fragment_title),mContext.getString(R.string.comment_fragment_title)};
		
		
		Log.i(TAG, "loadChildViewOfViewPage end");
	}


	 @Override
     public Fragment getItem(int i) {
         return mViewFragments.get(i);
     }

     @Override
     public int getCount() {
         return 2;
     }

     @Override
     public CharSequence getPageTitle(int position) {
    	 return mTitle[position];
     }
}
