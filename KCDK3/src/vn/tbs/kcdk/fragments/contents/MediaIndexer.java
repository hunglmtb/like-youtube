package vn.tbs.kcdk.fragments.contents;

import java.util.ArrayList;
import java.util.List;

import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import android.util.Log;
import android.widget.SectionIndexer;

public abstract class MediaIndexer implements SectionIndexer {
	private static final String TAG = MediaIndexer.class.getSimpleName();
	private String[] mStrings;
	private List<MediaInfo> mMediaList;
	private int[] mSections;

	public MediaIndexer(List<MediaInfo> mediaList) {
//		this.mMediaList = mediaList;

		initSection(mediaList);
	}

	private void initSection(List<MediaInfo> mediaList) {
		Log.i(TAG, "initSection start");

		this.mMediaList = mediaList;

		if (mediaList!=null&&mediaList.size()>0) {
			List<Integer> sections = new ArrayList<Integer>();
			sections.add(0);

			for (int i = 1; i < mediaList.size(); i++) {
				if (isDifferentSections(mediaList.get(i-1),mediaList.get(i))) {
					sections.add(i);
				}
			}
			
			//init secction
			mSections = new int[sections.size()];
			mStrings = new String[sections.size()];
			
			for(int i = 0; i < sections.size(); i++) {
				mSections[i] = sections.get(i);
				mStrings[i] = getSectionStringByMedia(mediaList.get(mSections[i]));

				Log.i(TAG, "section "+i+" : "+mSections[i]+" text : "+mStrings[i]);
			}
		}


		Log.i(TAG, "initSection end");
	}


	abstract public String getSectionStringByMedia(MediaInfo mediaInfo);

	abstract public boolean isDifferentSections(MediaInfo m1,
			MediaInfo m2);
	
	
	@Override
	public int getPositionForSection(int section) {

		if (section<0||mSections == null||mMediaList==null||section>=mMediaList.size()||section>=mSections.length) {
			return -1;
		}

		return mSections[section];

	}

	@Override
	public int getSectionForPosition(int position) {
		Log.i(TAG, "getSectionForPosition start");


		if (position<0||mMediaList==null||mSections == null||position>=mMediaList.size()) {
			return -1;
		}

		return find(position, mSections, 0, mSections.length-1);

	}

	@Override
	public Object[] getSections() {
		Log.i(TAG, "getSections start");

		if (mStrings==null) {
			return new String[]{""};
		}

		Log.i(TAG, "getSections end");
		return mStrings;

	}

	public int find(int val, int a[], int startIndex, int endIndex) {
		Log.i(TAG, "find with start: "+startIndex+" end : "+endIndex);

		if(endIndex-startIndex <= 1) {
			return -1;
		}

		int mid = startIndex + ((endIndex-startIndex) / 2);

		if (a[mid] <= val&&a[mid+1]>val) {
			return mid;
		}
		else if (a[mid+1]==val) {
			return mid+1;
		}
		else if (a[mid-1] <= val&&a[mid]>val) {
			return mid-1;
		}
		else {
			if(a[mid] > val) {
				return find(val, a, startIndex, mid);
			} else {
				return find(val, a, mid, endIndex);
			}
		}
	}

	public void updateSections(List<MediaInfo> moreData) {
		Log.i(TAG, "updateSections start");
		
		initSection(moreData);
		
		Log.i(TAG, "updateSections end");
	}
}
