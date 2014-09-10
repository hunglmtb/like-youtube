package vn.tbs.kcdk.fragments.contents;

import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.global.Common;
import android.os.Bundle;


public class HistoryFragment extends PinnedHeaderMediaListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mIndexer = new MediaIndexer(null){
			@Override
			public String getSectionStringByMedia(MediaInfo mediaInfo) {
				return Common.getEnjoyStatus(mediaInfo);
			}

			@Override
			public boolean isDifferentSections(MediaInfo m1, MediaInfo m2) {
				
				String t1 = Common.getEnjoyStatus(m1);
				String t2 = Common.getEnjoyStatus(m2);

				return !t1.equals(t2);
			}
			
		};
		mEnablePercentInfo = false;
		mEnablePinnedHeader = true;
		super.onCreate(savedInstanceState);
		
	}
	
}
