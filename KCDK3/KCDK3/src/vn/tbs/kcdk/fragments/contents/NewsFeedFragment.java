package vn.tbs.kcdk.fragments.contents;

import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import android.os.Bundle;

public class NewsFeedFragment extends PinnedHeaderMediaListFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		mIndexer = new MediaIndexer(null){
			@Override
			public String getSectionStringByMedia(MediaInfo mediaInfo) {
				return mediaInfo.getAuthor();
			}

			@Override
			public boolean isDifferentSections(MediaInfo m1, MediaInfo m2) {

				String t1 = m1.getAuthor();
				String t2 = m2.getAuthor();

				return !t1.equals(t2);
			}

		};
		mEnablePercentInfo = false;
		mEnablePinnedHeader = true;
		super.onCreate(savedInstanceState);

	}

}