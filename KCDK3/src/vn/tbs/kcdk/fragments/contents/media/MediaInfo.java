package vn.tbs.kcdk.fragments.contents.media;

import static vn.tbs.kcdk.global.Common.AUTHOR;
import static vn.tbs.kcdk.global.Common.CONTENT_INFO;
import static vn.tbs.kcdk.global.Common.DURATION;
import static vn.tbs.kcdk.global.Common.IS_PLAYING;
import static vn.tbs.kcdk.global.Common.MEDIA_FILE_URL;
import static vn.tbs.kcdk.global.Common.MEDIA_ID;
import static vn.tbs.kcdk.global.Common.MEDIA_IMAGE_URL;
import static vn.tbs.kcdk.global.Common.MEDIA_TYPE_AUDIO;
import static vn.tbs.kcdk.global.Common.SPEAKER;
import static vn.tbs.kcdk.global.Common.TITLE;
import android.os.Bundle;

public class MediaInfo {

	private String mMediaId;
	private String mTitle;
	private String mMediaFileUrl;
	private String mCategoryId;
	private String mLikeCount;
	private String mCommentCount;
	private String mSpeaker;
	private String mContentInfo;
	private String mDuration;
	private String mMediaLinkUrl;
	private String mAuthor;
	private String mPublishedDate;
	private String mViewCount;
	private int mMediaType = MEDIA_TYPE_AUDIO;
	private String mMediaImageThumbUrl;
	private String mMediaImageUrl;
	private String mEnjoyDonePercent;
	private int mTimeDurationAgo;
	private boolean mIsPlaying = false;
	
	
	public MediaInfo(String mediaId, String title, String mediaFileUrl,
			String categoryID, String likeCount, String commentCount,
			String speaker, String contentInfo, String duration,
			String mediaLinkUrl, String author, String publishedDate,
			String viewCount, int mediaType,
			String mediaImageThumbUrl, String mediaImageUrl) {
		super();
		this.mMediaId = mediaId;
		this.mTitle = title;
		this.mMediaFileUrl = mediaFileUrl;
		this.mCategoryId = categoryID;
		this.mLikeCount = likeCount;
		this.mCommentCount = commentCount;
		this.mSpeaker = speaker;
		this.mContentInfo = contentInfo;
		this.mDuration = duration;
		this.mMediaLinkUrl = mediaLinkUrl;
		this.mAuthor = author;
		this.mPublishedDate = publishedDate;
		this.mViewCount = viewCount;
		this.mMediaType = mediaType;
		this.mMediaImageThumbUrl = mediaImageThumbUrl;
		this.mMediaImageUrl = mediaImageUrl;
	}

	public MediaInfo(String mediaId, String title, String mediaFileUrl,
			String categoryID, String likeCount, String commentCount,
			String speaker, String contentInfo, String duration,
			String mediaLinkUrl, String author, String publishedDate,
			String viewCount, int mediaType,
			String mediaImageThumbUrl, String mediaImageUrl, String enjoyDonePercent, int timeDurationAgo) {
		super();
		this.mMediaId = mediaId;
		this.mTitle = title;
		this.mMediaFileUrl = mediaFileUrl;
		this.mCategoryId = categoryID;
		this.mLikeCount = likeCount;
		this.mCommentCount = commentCount;
		this.mSpeaker = speaker;
		this.mContentInfo = contentInfo;
		this.mDuration = duration;
		this.mMediaLinkUrl = mediaLinkUrl;
		this.mAuthor = author;
		this.mPublishedDate = publishedDate;
		this.mViewCount = viewCount;
		this.mMediaType = mediaType;
		this.mMediaImageThumbUrl = mediaImageThumbUrl;
		this.mMediaImageUrl = mediaImageUrl;
		this.mEnjoyDonePercent = enjoyDonePercent;
		this.mTimeDurationAgo = timeDurationAgo;
	}

	public MediaInfo(String mediaID, String title, String mediaFileUrl, int mediaType) {
		this.mMediaId = mediaID;
		this.mTitle = title;
		this.mMediaFileUrl = mediaFileUrl;
		this.mMediaType = mediaType;
	}
	
	

	public MediaInfo(String mMediaID, String mTitle, String mMediaUrl,
			String mCategoryID, String mLikeCount, String mCommentCount,
			String mBonusInfo, String mContentInfo, String mDuration,
			String mediaLinkUrl) {
		super();
		this.mMediaId = mMediaID;
		this.mTitle = mTitle;
		this.mMediaFileUrl = mMediaUrl;
		this.mCategoryId = mCategoryID;
		this.mLikeCount = mLikeCount;
		this.mCommentCount = mCommentCount;
		this.mSpeaker = mBonusInfo;
		this.mContentInfo = mContentInfo;
		this.mDuration = mDuration;
		this.mMediaLinkUrl = mediaLinkUrl;
	}



	public MediaInfo(String mMediaId, String mTitle, String mediaFileUrl,
			String mCategoryID, String mLikeCount, String mCommentCount,
			String mSpeaker, String mContentInfo, String mDuration,
			String mediaLinkUrl,
			String mAuthor, String mPublishedDate, String mViewCount) {
		super();
		this.mMediaId = mMediaId;
		this.mTitle = mTitle;
		this.mMediaFileUrl = mediaFileUrl;
		this.mCategoryId = mCategoryID;
		this.mLikeCount = mLikeCount;
		this.mCommentCount = mCommentCount;
		this.mSpeaker = mSpeaker;
		this.mContentInfo = mContentInfo;
		this.mDuration = mDuration;
		this.mMediaLinkUrl = mediaLinkUrl;
		this.mAuthor = mAuthor;
		this.mPublishedDate = mPublishedDate;
		this.mViewCount = mViewCount;
	}

	public MediaInfo(Bundle extras) {
		if (extras!=null) {
			mMediaId = extras.getString(MEDIA_ID);
			mMediaFileUrl = extras.getString(MEDIA_FILE_URL);
			mTitle = extras.getString(TITLE);
			mSpeaker = extras.getString(SPEAKER);
			mContentInfo = extras.getString(CONTENT_INFO);
			mDuration = extras.getString(DURATION);
			mAuthor = extras.getString(AUTHOR);
			mMediaImageUrl = extras.getString(MEDIA_IMAGE_URL);
			mIsPlaying =  extras.getBoolean(IS_PLAYING,false);
		}
	}
	
	

	public boolean isPlaying() {
		return mIsPlaying;
	}

	public String getSpeaker() {
		return mSpeaker;
	}

	public String getMediaImageThumbUrl() {
		return mMediaImageThumbUrl;
	}



	public String getMediaImageUrl() {
		return mMediaImageUrl;
	}



	public String getAuthor() {
		return mAuthor;
	}



	public String getPublishedDate() {
		return mPublishedDate;
	}


	public String getCategoryId() {
		return mCategoryId;
	}

	public String getLikeCount() {
		return mLikeCount;
	}

	public String getCommentCount() {
		return mCommentCount;
	}


	public String getContentInfo() {
		return mContentInfo;
	}



	public String getDuration() {
		return mDuration;
	}



	public String getMediaLinkUrl() {
		return mMediaLinkUrl;
	}


	public String getMediaId() {
		return mMediaId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getMediaFileUrl() {
		return mMediaFileUrl;
	}

	public int getMediaType() {
		return mMediaType;
	}

	public String getViewCount() {
		return mViewCount;
	}



	public String getEnjoyDonePercent() {
		return mEnjoyDonePercent;
	}


	public int getTimeDurationAgo() {
		return mTimeDurationAgo;
	}

	public void setLikeCount(String mLikeCount) {
		this.mLikeCount = mLikeCount;
	}

	public void setCommentCount(String mCommentCount) {
		this.mCommentCount = mCommentCount;
	}

	
	
}
