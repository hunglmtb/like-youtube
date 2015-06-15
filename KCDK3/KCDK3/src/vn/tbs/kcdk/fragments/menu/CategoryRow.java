package vn.tbs.kcdk.fragments.menu;

import static vn.tbs.kcdk.global.Common.*;

import java.util.List;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.global.Common;

public class CategoryRow {
	
	private String mCategoryKeyString = "";
	private String mCategoryId;
	private String mCategoryName;
	private int mIconResource;
	private boolean mShowChildCategory;
	private List<CategoryRow> mChildCategories;
	private int mItemMode = MENU_CATEGORY;

	

	public CategoryRow(String query, int mItemMode) {
		super();
		this.mCategoryId = query;
		this.mItemMode = mItemMode;
	}


	public CategoryRow(String categoryKeyString,String mCategoryId, String mCategoryName,
			 boolean mShowChildCategory,List<CategoryRow> mChildCategories) {
		super();
		this.mCategoryKeyString = categoryKeyString;
		this.mCategoryId = mCategoryId;
		this.mCategoryName = mCategoryName;
		this.mShowChildCategory = mShowChildCategory;
		this.mChildCategories = mChildCategories;
		matchParams(mCategoryId);
	}


	private void matchParams(String id) {
		if (id.equals(Common.CATEGORY_ID_HEADER)) {
			setType(R.drawable.ic_drawer_yt_autos,MENU_HEADER);
		}
		else if (id.equals(Common.CATEGORY_ID_TIMER)) {
			setType(R.drawable.ic_drawer_yt_comedy,MENU_TIMER);
		}
		else if (id.equals(Common.CATEGORY_ID_NEWSFEED)) {
			setType(R.drawable.ic_drawer_yt_comedy,MENU_NEWS_FEED);
		}
		else if (id.equals(Common.CATEGORY_ID_SETTING)) {
			setType(R.drawable.ic_drawer_yt_comedy,MENU_SETTING);
		}
		else if (id.equals(Common.CATEGORY_ID_EXIT)) {
			setType(R.drawable.ic_drawer_yt_comedy,MENU_EXIT);
		}
		else if (id.equals(Common.CATEGORY_ID_HISTORY)) {
			setType(R.drawable.ic_drawer_yt_comedy,MENU_HISTORY);
		}
		else if (id.equals(Common.CATEGORY_ID_SEARCH)) {
			setType(R.drawable.ic_drawer_yt_education,MENU_SEARCH);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_01)) {
			setType(R.drawable.ic_drawer_yt_entertainment,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_02)) {
			setType(R.drawable.ic_drawer_yt_film,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_03)) {
			setType(R.drawable.ic_drawer_yt_games,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_04)) {
			setType(R.drawable.ic_drawer_yt_live,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_05)) {
			setType(R.drawable.ic_drawer_yt_music,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_06)) {
			setType(R.drawable.ic_drawer_yt_news,MENU_CATEGORY);
		}
		else if (id.equals(Common.CATEGORY_ID_NAME_07)) {
			setType(R.drawable.ic_drawer_yt_nonprofits,MENU_CATEGORY);
		}
		else{
			setType(R.drawable.ic_drawer_yt_autos,MENU_CATEGORY);

		}
		
	}


	private void setType(int reource, int type) {
		this.mIconResource = reource;
		this.mItemMode = type;
	}

	public CategoryRow(String mCategoryName, int mIconResource,
			boolean mShowChildCategory, List<CategoryRow> mChildCategories) {
		super();
		this.mCategoryName = mCategoryName;
		this.mIconResource = mIconResource;
		this.mShowChildCategory = mShowChildCategory;
		this.mChildCategories = mChildCategories;
	}



	public CategoryRow(String mCategoryName, int mIconResource,
			boolean mShowChildCategory, List<CategoryRow> mChildCategories,
			int mItemMode) {
		super();
		this.mCategoryName = mCategoryName;
		this.mIconResource = mIconResource;
		this.mShowChildCategory = mShowChildCategory;
		this.mChildCategories = mChildCategories;
		this.mItemMode = mItemMode;
	}


	public String getCategoryId() {
		return mCategoryId;
	}
	public String getCategoryKeyString() {
		return mCategoryKeyString;
	}

	/**
	 * @return the mName
	 */
	public String getCategoryName() {
		return mCategoryName;
	}

	/**
	 * @return the mValue
	 */
	public int getIconResource() {
		return mIconResource;
	}


	public List<CategoryRow> getChildCategories() {
		return mChildCategories;
	}


	public void setChildCategories(List<CategoryRow> mChildCategories) {
		this.mChildCategories = mChildCategories;
	}


	public boolean isShowChildCategory() {
		return mShowChildCategory;
	}


	public int getItemMode() {
		return mItemMode;
	}


}
