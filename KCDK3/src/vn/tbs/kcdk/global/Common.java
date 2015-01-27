package vn.tbs.kcdk.global;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.FoundMediaFragment;
import vn.tbs.kcdk.fragments.contents.HistoryFragment;
import vn.tbs.kcdk.fragments.contents.LikeAndCommentCountLoadingAsyntask;
import vn.tbs.kcdk.fragments.contents.LikeAndCommentCountLoadingAsyntask.OnDoneListener;
import vn.tbs.kcdk.fragments.contents.NewsFeedFragment;
import vn.tbs.kcdk.fragments.contents.PinnedHeaderMediaListFragment;
import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import vn.tbs.kcdk.fragments.menu.MenuAdapter;
import vn.tbs.kcdk.fragments.timer.TimerFragment;
import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.media.audiofx.Equalizer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class Common {

	public static final int MENU_HEADER = 0;
	public static final int MENU_CATEGORY = 1;
	public static final int MENU_SEARCH = 2;
	public static final int MENU_HISTORY = 3;
	public static final int MENU_TIMER = 4;
	public static final int MENU_NEWS_FEED = 5;
	public static final int MENU_SETTING = 6;
	public static final int MENU_EXIT = 7;
	
	public static final int HOUR_LENGTH = 60;
	public static final int DAY_LENGTH = 24*HOUR_LENGTH;
	public static final int WEEK_LENGTH = 7*DAY_LENGTH;
	public static final int MONTH_LENGTH = 30*DAY_LENGTH;
	public static final int YEAR_LENGTH = 365*DAY_LENGTH;
	
	public static final int MAX_NUM_RELATIVE_MEDIA = 5;

	public static final int MEDIA_TYPE_AUDIO = 0;
	public static final int MEDIA_TYPE_VIDEO = 1;
	public static final int MEDIA_TYPE_IMAGE = 2;

	public static enum FragmentMode {
		MEDIAlIST,
		CONTENT,
		PAGERCONTENT,
		HISTORY,
		NEWSFEED,
		SETTING,
		SEARCH,
		TIMER,
		NONE
	}

	public static final String TITLE_BUNDLE = "TITLE";
	public static final String MENU_POSITION_KEY = "MENUPOSITION";
	public static final String LIKE_PLUGIN = "LIKE_PLUGIN";
	public static final String MEDIA_URL = "MEDIA_URL";
	public static final String COMMENT_PLUGIN_URL = "COMMENT_PLUGIN_URL";
	public static final String MEDIA_LIST_KEY = "MEDIA_LIST_KEY";
	public static final String FRAGMENT_MODE_KEY = "FRAGMENT_MODE_KEY";
	public static final String FRAGMENT_TAG_KEY = "FRAGMENT_TAG_KEY";
	public static final String NONE_VALUE = "NONE";
	private static final String CATEGORYID_KEY = "CATEGORYID_KEY";
	
	
	public static final String MEDIA_AUTHOR_KEY = "MEDIA_AUTHOR_KEY";
	public static final String MEDIA_ID_KEY = "MEDIA_ID_KEY";
	public static final String MEDIA_FILEURL_KEY = "MEDIA_FILEURL_KEY";
	public static final String MEDIA_CATEGORYD_KEY = "MEDIA_CATEGORYD_KEY";
	public static final String MEDIA_LIKECOUNT_KEY = "MEDIA_LIKECOUNT_KEY";
	public static final String MEDIA_COMMENTCOUNT_KEY = "MEDIA_COMMENTCOUNT_KEY";
	public static final String MEDIA_SPEAKER_KEY = "MEDIA_SPEAKER_KEY";
	public static final String MEDIA_CONTENTINFO_KEY = "MEDIA_CONTENTINFO_KEY";
	public static final String MEDIA_DURATION_KEY = "MEDIA_DURATION_KEY";
	public static final String MEDIA_LINKURL_KEY = "MEDIA_LINKURL_KEY";
	public static final String MEDIA_PUBLISHDATE_KEY = "MEDIA_PUBLISHDATE_KEY";
	public static final String MEDIA_VIEWCOUNT_KEY = "MEDIA_VIEWCOUNT_KEY";
	public static final String MEDIA_TYPE_KEY = "MEDIA_TYPE_KEY";
	public static final String MEDIA_IMAGETHUMBURL_KEY = "MEDIA_IMAGETHUMBURL_KEY";
	public static final String MEDIA_IMAGEURL_KEY = "MEDIA_IMAGEURL_KEY";
	public static final String MEDIA_TITLE_KEY = "MEDIA_TITLE_KEY";


	public static final int URL_CATEGORY_MODE = 0;
	public static final int URL_MEDIA_LIST_MODE = 1;
	public static final int URL_IMAGE_LOAD = 2;

	public static final String CATEGORY_ID_HEADER = "HEADER";
	public static final String CATEGORY_ID_SEARCH = "SEARCH";
	public static final String CATEGORY_ID_HISTORY = "HISTORY";
	public static final String CATEGORY_ID_TIMER = "TIMER";
	public static final String CATEGORY_ID_NEWSFEED = "NEWSFEED";
	public static final String CATEGORY_ID_SETTING = "SETTING";
	public static final String CATEGORY_ID_EXIT = "EXIT";
			
	public static final String CATEGORY_ID_NAME_01 = "CATEGORY01";
	public static final String CATEGORY_ID_NAME_02 = "CATEGORY02";
	public static final String CATEGORY_ID_NAME_03 = "CATEGORY03";
	public static final String CATEGORY_ID_NAME_04 = "CATEGORY04";
	public static final String CATEGORY_ID_NAME_05 = "CATEGORY05";
	public static final String CATEGORY_ID_NAME_06 = "CATEGORY06";
	public static final String CATEGORY_ID_NAME_07 = "CATEGORY07";
	public static final String CATEGORY_ID_NAME_08 = "CATEGORY08";
	public static final String SOCIAL_HOST_URL = "http://kcdkv2.appspot.com/media/social?mediaId=";
	public static final String NOTIFICATION_DETAIL_MEDIA = "NOTIFICATION_DETAIL_MEDIA";
	
	//notification
	public static final String ACTION_LAUNCH="vn.tbs.kcdk.ACTION_LAUNCH";
	public static final String ACTION_STOP="vn.tbs.kcdk.ACTION_STOP";
	public static final String ACTION_PLAY="vn.tbs.kcdk.ACTION_PLAY";
	public static final String ACTION_PAUSE="vn.tbs.kcdk.ACTION_PAUSE";
	public static final int REQUEST_CODE = 1000;
	public static final int REQUEST_CODE_STOP = 1001;
	public static final int REQUEST_CODE_PLAY = 1002;

	public static String START_PLAY = "START_PLAY";
	public static final int SEEKBAR_MAX = 100;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int START_PLAY_COMMAND = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	public static final int PAUSE_PLAY_COMMAND = 5;
	public static final int UPDATE_PROGRESS_COMMAND = 6;
	public static final int STOP_COMMAND = 7;
	public static final int UPDATE_GUI_COMMAND = 8;


	public static final int BUFFERING_UPDATE_COMMAND = 100;
	public static final int SEEKBAR_UPDATE_COMAND = 101;
	public static final int PLAY_PAUSE_UPDATE_COMAND = 102;
	public static final int UI_UPDATE_COMAND = 1000;
	public static final int PLAYING = 10;
	public static final int PAUSING = 11;
	public static final int NOTIFICATION_ID = 2000;
	
	//media fields
	public static final String MEDIA_ID="MEDIA_ID";
	public static final String TITLE="TITLE";
	public static final String SPEAKER="SPEAKER";
	public static final String MEDIA_FILE_URL="MEDIA_FILE_URL";
	public static final String CONTENT_INFO="CONTENT_INFO";
	public static final String DURATION="DURATION";
	public static final String AUTHOR="AUTHOR";
	public static final String MEDIA_IMAGE_URL="MEDIA_IMAGE_URL";
	public static final String IS_PLAYING = "IS_PLAYING";

	
	public static Bundle initFragmentBund(FragmentMode mode, String[] values) {

		Bundle bundle = new Bundle();
		bundle.putString(FRAGMENT_TAG_KEY, values[0]);
		switch (mode) {
		case HISTORY:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.HISTORY);
			bundle.putString(CATEGORYID_KEY, values[0]);
			break;
		case SEARCH:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.SEARCH);
			bundle.putString(CATEGORYID_KEY, values[0]);
			break;
		case NEWSFEED:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.NEWSFEED);
			bundle.putString(CATEGORYID_KEY, values[0]);
			break;
		case TIMER:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.TIMER);
			bundle.putString(CATEGORYID_KEY, values[0]);
			break;
		case MEDIAlIST:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.MEDIAlIST);
			bundle.putString(CATEGORYID_KEY, values[0]);
			break;
		case CONTENT:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.CONTENT);
			bundle.putString(CATEGORYID_KEY, values[1]);
			bundle.putString(MEDIA_URL, values[2]);
			break;
		case PAGERCONTENT:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.PAGERCONTENT);
			break;
		default:
			bundle.putSerializable(FRAGMENT_MODE_KEY, FragmentMode.NONE);
			break;
		}
		return bundle ;
	}

	public static int convertFromFragmentToPosition(Bundle bundle, MenuAdapter menuAdapter) {
		int selectedPosition = -1;
		FragmentMode fmode = (FragmentMode) bundle.getSerializable(FRAGMENT_MODE_KEY);
		String tag = "";
		if (fmode!=null) {
			switch (fmode) {
			case HISTORY:
			case SEARCH:
			case SETTING:
			case NEWSFEED:
			case MEDIAlIST:
			case CONTENT:
				tag = bundle.getString(Common.CATEGORYID_KEY);
				selectedPosition = menuAdapter.getCategoryPositionById(tag);
				break;
			default:
				break;
			}
		}
		return selectedPosition;
	}
/*
	public static boolean showMediaContent(MediaInfo item, KCDKActivity kcdkActivity) {

		if (item!=null) {
			switch (item.getMediaType()) {
			case MEDIA_TYPE_AUDIO:
				loadMediaContentFragment(item,kcdkActivity);
//				KCDKActivity.sKCDKMediaPlayer.playMedia(item);
				return true;
			case MEDIA_TYPE_VIDEO:
				return true;
			case MEDIA_TYPE_IMAGE:
				return true;
			default:
				break;
			}
		}

		return false;
	}

	public static void loadMediaContentFragment(MediaInfo item, KCDKActivity kcdkActivity) {

		ContentFragment mediaContentFragment = new ContentFragment();

		String[] values = new String[]{
				item.getMediaId(),
				item.getCategoryId(),
				item.getMediaLinkUrl()};

		Bundle bundle = Common.initFragmentBund(Common.FragmentMode.CONTENT,values);
		putMediaFields(bundle,item);
		mediaContentFragment.setArguments(bundle);

		kcdkActivity.switchContent(mediaContentFragment,true);

		if(KCDKActivity.sKCDKMediaPlayer!=null){
			KCDKActivity.sKCDKMediaPlayer.playMedia(item);			
		}
	}*/

	private static void putMediaFields(Bundle bundle, MediaInfo item) {
		if (bundle!=null&&item!=null) {
			bundle.putString(MEDIA_AUTHOR_KEY, item.getAuthor());
			bundle.putString(MEDIA_CATEGORYD_KEY, item.getCategoryId());
			bundle.putString(MEDIA_COMMENTCOUNT_KEY, item.getCommentCount());
			bundle.putString(MEDIA_CONTENTINFO_KEY, item.getContentInfo());
			bundle.putString(MEDIA_DURATION_KEY, item.getDuration());
			bundle.putString(MEDIA_LIKECOUNT_KEY, item.getLikeCount());
			bundle.putString(MEDIA_FILEURL_KEY, item.getMediaFileUrl());
			bundle.putString(MEDIA_ID_KEY, item.getMediaId());
			bundle.putString(MEDIA_IMAGETHUMBURL_KEY, item.getMediaImageThumbUrl());
			bundle.putString(MEDIA_IMAGEURL_KEY, item.getMediaImageUrl());
			bundle.putString(MEDIA_LINKURL_KEY, item.getMediaLinkUrl());
			bundle.putString(MEDIA_PUBLISHDATE_KEY, item.getPublishedDate());
			bundle.putString(MEDIA_SPEAKER_KEY, item.getSpeaker());
			bundle.putString(MEDIA_TITLE_KEY, item.getTitle());
			bundle.putString(MEDIA_VIEWCOUNT_KEY, item.getViewCount());
			bundle.putInt(MEDIA_TYPE_KEY, item.getMediaType());
		}
	}

	public static Fragment initContentFragment(CategoryRow item) {
		if (item==null) {
			return null;
		}

		int mode = item.getItemMode();
		Fragment fragment = null;
		String[] values = new String[]{ item.getCategoryId()};
		Bundle bundle = null;
		//		bundle.putString(Common.MENU_POSITION_KEY, ""+position);		

		if (mode==MENU_CATEGORY) {

			/*mMediaListFragment = new MediaListFragment();
			mMediaListFragment.setArguments(bundle);
			fragment = mMediaListFragment;*/
			bundle = Common.initFragmentBund(Common.FragmentMode.MEDIAlIST,values);
			PinnedHeaderMediaListFragment mSpinnedHeaderMediaListFragment = new PinnedHeaderMediaListFragment();
			mSpinnedHeaderMediaListFragment.setArguments(bundle);
			fragment = mSpinnedHeaderMediaListFragment;

		}
		else if (mode==MENU_HEADER) {
			return null;
		}
		else if (mode==MENU_HISTORY) {
			bundle = Common.initFragmentBund(Common.FragmentMode.HISTORY,values);
			fragment = new HistoryFragment();
			fragment.setArguments(bundle);
		}
		else if (mode==MENU_SEARCH) {
			bundle = Common.initFragmentBund(Common.FragmentMode.SEARCH,values);
			fragment = new FoundMediaFragment();
			fragment.setArguments(bundle);
		}
		else if (mode==MENU_NEWS_FEED) {
			bundle = Common.initFragmentBund(Common.FragmentMode.NEWSFEED,values);
			fragment = new NewsFeedFragment();
			fragment.setArguments(bundle);
		}
		else if (mode==MENU_TIMER) {
			bundle = Common.initFragmentBund(Common.FragmentMode.TIMER,values);
			fragment = new TimerFragment();
			fragment.setArguments(bundle);
		}

		return fragment;

	}

/*	public static void showMediaPage(MediaInfo mediaInfoItem,
			KCDKActivity kcdkActivity) {
		
		if (mediaInfoItem!=null&&kcdkActivity!=null) {
			kcdkActivity.showMediaPageById(mediaInfoItem);
		}
	}*/

	public static List<MediaInfo> createTestMediaListData(Context mContext,
			int size) {
		
		String[] mMediaLinkUrls = mContext.getResources().getStringArray(R.array.media_link_urls);
		String[] mMediaFileUrls = mContext.getResources().getStringArray(R.array.media_file_urls);
		String[] mMediaImageThumbUrls = mContext.getResources().getStringArray(R.array.media_image_thumb_urls);
		String[] mMediaImageUrls = mContext.getResources().getStringArray(R.array.media_image_urls);
		String[] names =  mContext.getResources().getStringArray(R.array.media_title_names);

		ArrayList<MediaInfo> mediaList = new ArrayList<MediaInfo>();
		Random ran = new Random();
		int k = 0;
		MediaInfo media = null;
		int length = Math.min(names.length, mMediaLinkUrls.length);
		length = Math.min(length, mMediaFileUrls.length);
		length = Math.min(length, mMediaImageThumbUrls.length);
		length = Math.min(length, mMediaImageUrls.length);
		length = Math.min(length, size);

//		String categoryId = getArguments().getString(Common.FRAGMENT_TAG_KEY);
		String categoryId = "t5";

		for (int i = 0; i < length ; i++) {
			k = ran.nextInt(100);
			media = new MediaInfo("mediaId"+i,
					names[i], mMediaFileUrls[i],
					categoryId, ""+2*k, ""+k,
					"giọng đọc Việt Hùng "+k,
					"url", 5*k%60+":"+k%60,
					mMediaLinkUrls[i],
					"author"+i,
					"2013/8/"+i,
					""+3*k,
					MEDIA_TYPE_AUDIO,
					mMediaImageThumbUrls[i],
					mMediaImageUrls[i]);
			mediaList.add(media );
		}
		return mediaList;
	}

	public static String getConnectUrl(Context context,
			int urlMode, String[] params) {
		
		String domainUrl = context.getString(R.string.url_domain);
		String secondaryUrl = "";
		switch (urlMode) {
		case URL_CATEGORY_MODE:
			secondaryUrl = context.getString(R.string.url_categories);
			return domainUrl+secondaryUrl;
		case URL_MEDIA_LIST_MODE:
			secondaryUrl = context.getString(R.string.url_mediaList);
			return domainUrl+secondaryUrl+"?category="+params[0]+"&limit=" +params[2]+"&offset=" +params[3];
		case URL_IMAGE_LOAD:
			secondaryUrl = context.getString(R.string.url_image);
			return domainUrl+secondaryUrl+params[0];
			//TODO group mode update later
		default:
			break;
		}
		return domainUrl;
		
	}

	public static String getEnjoyStatus(MediaInfo media) {
		
		//	TODO OPTIMIZE LATER
		int minute =media.getTimeDurationAgo();
		String timeUnit = null;
		int timeNumber = 0;
		int hour = minute/HOUR_LENGTH;
		int day = minute/DAY_LENGTH;
		int week = minute/WEEK_LENGTH;
		int month = minute/MONTH_LENGTH;
		int year = minute/YEAR_LENGTH;
		if (year>0) {
			timeUnit=year==1?" year":" years";
			timeNumber = year;
		}
		else if (month>0) {
			timeUnit=month==1?" month":" months";
			timeNumber = month;
		}
		else if (week>0) {
			timeUnit=week==1?" week":" weeks";
			timeNumber = week;
		}
		else if (day>0) {
			timeUnit=year==1?" day":" days";
			timeNumber = day;
		}
		else if (hour>0) {
			timeUnit=hour==1?" hour":" hours";
			timeNumber = hour;
		}
		else if (minute>0) {
			timeUnit=minute==1?" minute":" minutes";
			timeNumber = minute;
		}
		else {
			return media.getEnjoyDonePercent()+" done";
		}
		
		String result = timeNumber+timeUnit+" ago. ";//+ media.getEnjoyDonePercent()+" done";
		return result;
		
	}

	public static void setTextValue(View convertView, int resourceId,
			String title, int textStyle, Typeface font) {
		TextView text = (TextView) convertView.findViewById(resourceId);
		if(text != null){
			text.setVisibility(View.VISIBLE);

			text.setText(title);
			if (font!=null) {
				text.setTypeface(font,textStyle);				
			}
		}
	}
	public static void bindTextValue(View convertView, MediaInfo media, boolean isHistoryAdapterType, Typeface font) {

		setTextValue(convertView,R.id.media_title_text,media.getTitle(),Typeface.NORMAL, font);
		setTextValue(convertView,R.id.media_duration_text,media.getDuration(),Typeface.NORMAL, font);
		setTextValue(convertView,R.id.media_bonus_info_text,media.getSpeaker(),Typeface.NORMAL, font);
		setTextValue(convertView,R.id.fb_comment_count_text,media.getCommentCount(),Typeface.NORMAL, font);
		setTextValue(convertView,R.id.fb_like_count_text,media.getLikeCount(),Typeface.NORMAL, font);
		
		if (isHistoryAdapterType) {
			Common.setTextValue(convertView,R.id.media_user_view_status,Common.getEnjoyStatus(media),Typeface.NORMAL, font);
		}
	}
	
	public static void updateLikeAndCommentCount(List<MediaInfo> moreData, OnDoneListener listener) {
		LikeAndCommentCountLoadingAsyntask likeAndCommentCountLoadingAsyntask;
		likeAndCommentCountLoadingAsyntask = new LikeAndCommentCountLoadingAsyntask(moreData);
		likeAndCommentCountLoadingAsyntask.setOnDoneListener(listener);
		likeAndCommentCountLoadingAsyntask.execute();			
	}

	public static boolean validateString(String mMediaId) {
		return mMediaId!=null&&mMediaId.length()>0&&!"null".equals(mMediaId)&&!"NULL".equals(mMediaId);
	}

	public static void setVisible(View view, boolean visible) {
		int visibleValue = visible?View.VISIBLE:View.GONE;
		if (view!=null) {
			view.setVisibility(visibleValue);
		}		
	}

	public static String getDurationTextFromNumber(int duration) {
		String durationText = "-:-";
		if (duration>0&&duration<1000000000) {
			long hours = TimeUnit.MILLISECONDS.toHours(duration);
			
			if (hours>0) {
				durationText = String.format("%02d:%02d:%02d", 
						TimeUnit.MILLISECONDS.toHours(duration),
						TimeUnit.MILLISECONDS.toMinutes(duration) -  
						TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), // The change is in this line
						TimeUnit.MILLISECONDS.toSeconds(duration) - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))); 
			}
			else{
				long minute = TimeUnit.MILLISECONDS.toMinutes(duration);
				
				String format = minute>9?"%02d:%02d":"%1d:%02d";
				durationText = String.format(format, 
						TimeUnit.MILLISECONDS.toMinutes(duration), // The change is in this line
						TimeUnit.MILLISECONDS.toSeconds(duration) - 
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))); 
			}
			
		}
		return durationText;
	}
	

	public static void setItemWidth(View view,Context context,boolean initLayout) {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		
		int newWidht = width;
		LayoutParams layout = view.getLayoutParams();
		if (layout!=null) {
			layout.width = newWidht;
		}
		else if (initLayout) {
			layout = new  it.sephiroth.android.library.widget.AbsHListView.LayoutParams(width,android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		}
		
		if (layout!=null) {
			view.setLayoutParams(layout);
			view.requestLayout();
		}
	}
	
	public static String getMacAddress(Activity activity) {
		WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		String macAddress = wInfo.getMacAddress();
		return macAddress;
	}

}
