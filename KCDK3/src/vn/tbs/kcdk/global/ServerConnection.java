package vn.tbs.kcdk.global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.tbs.kcdk.fragments.contents.media.MediaInfo;
import vn.tbs.kcdk.fragments.menu.CategoryRow;
import android.util.Log;

public class ServerConnection {

	private static final String TAG = ServerConnection.class.getSimpleName();

	public static List<CategoryRow> getCategories(String url) {
		Log.i(TAG, "getCategories start");

		String categoriesString = loadDataFromUrl(url);
		List<CategoryRow> categories = parseCategoriesJson(categoriesString);

		Log.i(TAG, "getCategories end");
		return categories;

	}

	private static List<CategoryRow> parseCategoriesJson(String categoriesString) {
		Log.i(TAG, "parseJson start");

		if (categoriesString==null) {
			return null;
		}

		List<CategoryRow> categories = null;
		try {
			//categories
			JSONArray jCategories = new JSONArray(categoriesString);
			int categoriesNum = jCategories.length();

			if (jCategories!=null&&categoriesNum>0) {
				CategoryRow category = null;
				String categoryId = Common.CATEGORY_ID_NEWSFEED;
				String categoryName = null;
				String categoryKeyString = "";
				boolean showChildCategory = false;
				List<CategoryRow> childCategories = null;
				JSONObject jCategory = null;
				categories = new ArrayList<CategoryRow>();

				for (int i = 0; i < categoriesNum; i++) {
					jCategory = jCategories.getJSONObject(i);
//					categoryId = jCategory.getString("categoryId");
					categoryName = jCategory.getString("categoryName");
					categoryKeyString = jCategory.getString("keyString");
					try {
						showChildCategory = jCategory.getBoolean("showChildCategory");
					} catch (JSONException e1) {
						e1.printStackTrace();
						Log.e(TAG, "parseJson JSONException "+e1);
						showChildCategory = false;
					}
					//TODO parse child categories
					//					childCategories = jCategory.getJSONArray("createdDate");
					childCategories = null;

					category = new CategoryRow(categoryKeyString,categoryId, categoryName, showChildCategory, childCategories);
					categories.add(category);
				}

			}

		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.e(TAG, "parseJson JSONException "+e1);
			return null;
		}
		Log.i(TAG, "parseJson end");
		return categories;

	}

	/**
	 * author	lmhung
	 * time		2:30:56 PM
	 * @param string
	 * @return
	 */
	public static String loadDataFromUrl(String urlStr) {
		Log.i(TAG, "url request = "+urlStr);
		if (urlStr==null) {
			return null;
		}
		InputStream is = null;
		try {
			URL url = new URL(urlStr);
			if (url!=null) {
				is = (InputStream) url.getContent();				
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			Log.e(TAG, "loadDataFromUrl MalformedURLException "+e);
			return null;
		}
		catch (NullPointerException e) {
			Log.e(TAG, "loadDataFromUrl NullPointerException "+e);
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				//TODO
			}
			Log.e(TAG, "loadDataFromUrl IOException "+e);
			e.printStackTrace();
			return null;
		}
		String result = convertStreamToString(is);

		return result;
	}
	/**
	 * author	lmhung
	 * time		2:13:07 PM
	 * @param input
	 * @return
	 */
	public static String convertStreamToString(final InputStream input) {
		if (input == null)
		{
			return "";
		}
		final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		final StringBuilder sBuf = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sBuf.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "convertStreamToString IOException "+e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				Log.e(TAG, "convertStreamToString IOException "+e);
			}
		}
		return sBuf.toString();
	}

	public static List<MediaInfo> loadMediaList(String url) {
		Log.i(TAG, "loadMediaList url: "+url);

		String mediaString = loadDataFromUrl(url);
		List<MediaInfo> mediaList = parseMediaListJson(mediaString);
		return mediaList;

	}

	private static List<MediaInfo> parseMediaListJson(String mediaString) {
		Log.i(TAG, "parseMediaListJson start");


		if (mediaString==null) {
			return null;
		}

		List<MediaInfo> mediaList = null;
		try {
			//categories
			JSONArray jCategories = new JSONArray(mediaString);
			int categoriesNum = jCategories.length();

			if (jCategories!=null&&categoriesNum>0) {
				String mediaId;
				String title;
				String mediaFileUrl;
				String categoryId = Common.CATEGORY_ID_NEWSFEED;
				String likeCount;
				String commentCount;
				String speaker;
				String contentInfo;
				String duration;
				String mediaLinkUrl;
				String author;
				String publishedDate;
				String viewCount;
				int mediaType;
				String mediaImageThumbUrl;
				String mediaImageUrl;
				String enjoyDonePercent = null;
				int timeDurationAgo = 0;

				MediaInfo media = null;
				JSONObject jmedia = null;
				mediaList = new ArrayList<MediaInfo>();

				for (int i = 0; i < categoriesNum; i++) {
					jmedia = jCategories.getJSONObject(i);

					//match field
					mediaId = jmedia.getString("keyString");
					title = jmedia.getString("title");
					mediaFileUrl = jmedia.getString("mediaFileUrl");
//					categoryId = jmedia.getString("categoryId");
					likeCount = jmedia.getString("likeCount");
					commentCount = jmedia.getString("commentCount");
					speaker = jmedia.getString("speaker");
					contentInfo = jmedia.getString("contentInfo");
					duration = jmedia.getString("duration");
					mediaLinkUrl = jmedia.getString("mediaLinkUrl");
					author = jmedia.getString("author");
					publishedDate = jmedia.getString("publishedDate");
					viewCount = jmedia.getString("viewCount");
					mediaType = jmedia.getInt("mediaType");
					mediaImageThumbUrl = jmedia.getString("mediaImageThumbUrl");
					mediaImageUrl = jmedia.getString("mediaImageUrl");
//					enjoyDonePercent = jmedia.getString("enjoyDonePercent");
//					timeDurationAgo = jmedia.getInt("timeDurationAgo");


					//init media
					media = new MediaInfo(mediaId,
							title,
							mediaFileUrl,
							categoryId,
							likeCount,
							commentCount,
							speaker,
							contentInfo,
							duration,
							mediaLinkUrl,
							author,
							publishedDate,
							viewCount,
							mediaType,
							mediaImageThumbUrl,
							mediaImageUrl,
							enjoyDonePercent,
							timeDurationAgo);
					mediaList.add(media);
				}

			}

		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.e(TAG, "parseMediaListJson JSONException "+e1);
		}
		Log.i(TAG, "parseMediaListJson end");
		return mediaList;

	}

	public static List<MediaInfo> getRelativeMedia(String string) {

		//TODO SET MIN NUM = 5
//		String url = "http://hunglmbk.com:8888/api/medialist?groupmode=&limit=10&offset=0";
		String url = string;
		return ServerConnection.loadMediaList(url);

	}

	public static void getLikeAndCommentCount(List<MediaInfo> mMediaList) {
		Log.i(TAG, "getLikeAndCommentCount start");

		String url;
		try {
			url = initSocialUrl(mMediaList);
			Log.i(TAG, "getLikeAndCommentCount url = "+url);
			String socialData = loadDataFromUrl(url);
			putJsonDataToMedia(mMediaList,socialData);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.i(TAG, "getLikeAndCommentCount end");
	}

	private static String initSocialUrl(List<MediaInfo> mMediaList) throws UnsupportedEncodingException {
		Log.i(TAG, "initSocialUrl start");
		
		if (mMediaList!=null) {
			
			String urlSet = "(\"";
			
			MediaInfo media = null;
			for (int i = 0; i < mMediaList.size(); i++) {
				media = mMediaList.get(i);
				if (i==0) {
					//urlSet+=media.getMediaLinkUrl()+"\"";
					urlSet+=Common.SOCIAL_HOST_URL+media.getMediaId()+"\"";
				}
				else{
					//urlSet+=",\""+media.getMediaLinkUrl()+"\"";					
					urlSet+=",\""+Common.SOCIAL_HOST_URL+media.getMediaId()+"\"";					
				}
			}
			urlSet+=")";
			
			String query = "SELECT like_count,comment_count,url FROM link_stat WHERE url in"+urlSet;
			
			String url = "http://graph.facebook.com/fql?q="+URLEncoder.encode(query, "utf-8");
			return url;
		}
		else{
			return null;
		}


	}

	private static void putJsonDataToMedia(List<MediaInfo> mMediaList,
			String socialData) {
		Log.i(TAG, "putJsonDataToMedia start");

		if (socialData==null||mMediaList==null||mMediaList.size()<=0) {
			return ;
		}

		try {
			//categories
			JSONObject jD = new JSONObject(socialData);
			
			JSONArray jData = jD.getJSONArray("data");
			int jDataNum = jData.length();
			int minSize = mMediaList.size()>=jDataNum?jDataNum:mMediaList.size();

			if (jData!=null&&jDataNum>0) {
				String likeCount = null;
				String commentCount = null;
				JSONObject jSocial = null;
				MediaInfo media = null;

				for (int i = 0; i < minSize; i++) {
					jSocial = jData.getJSONObject(i);
					likeCount = jSocial.getString("like_count");
					commentCount = jSocial.getString("comment_count");
					media = mMediaList.get(i);
					media.setLikeCount(likeCount);
					media.setCommentCount(commentCount);
				}

			}

		} catch (JSONException e1) {
			e1.printStackTrace();
			Log.e(TAG, "parseJson JSONException "+e1);
		}

		Log.i(TAG, "putJsonDataToMedia end");
	}

}
