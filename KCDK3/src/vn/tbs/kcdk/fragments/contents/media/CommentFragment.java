package vn.tbs.kcdk.fragments.contents.media;

import vn.tbs.kcdk.global.Common;
import android.os.Bundle;

public class CommentFragment extends FacebookPluginFragment {

	public CommentFragment(String mediaId) {
		super(mediaId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		//mUrl = "http://www.haivl.com/photo/973665";
		mPluginHtml = "commentplugin.html";
		isLikePlugin = false;
		Bundle arg = getArguments();
		if (arg!=null) {
			mMediaId = arg.getString(Common.MEDIA_ID_KEY);
		}
		
		super.onActivityCreated(savedInstanceState);
	}
}
