/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vn.tbs.kcdk.global;

import vn.tbs.kcdk.R;
import vn.tbs.kcdk.fragments.contents.EndlessLoadAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
/**
 * A ListView that maintains a header pinned at the top of the list. The
 * pinned header can be pushed up and dissolved as needed.
 */
public class PinnedHeaderListView extends ListView {
	private static final String TAG = PinnedHeaderListView.class.getSimpleName();

	/**
	 * Adapter interface.  The list adapter must implement this interface.
	 */
	public interface PinnedHeaderAdapter {

		/**
		 * Pinned header state: don't show the header.
		 */
		public static final int PINNED_HEADER_GONE = 0;

		/**
		 * Pinned header state: show the header at the top of the list.
		 */
		public static final int PINNED_HEADER_VISIBLE = 1;

		/**
		 * Pinned header state: show the header. If the header extends beyond
		 * the bottom of the first shown element, push it up and clip.
		 */
		public static final int PINNED_HEADER_PUSHED_UP = 2;

		/**
		 * Computes the desired state of the pinned header for the given
		 * position of the first visible list item. Allowed return values are
		 * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
		 * {@link #PINNED_HEADER_PUSHED_UP}.
		 */
		int getPinnedHeaderState(int position);

		/**
		 * Configures the pinned header view to match the first visible list item.
		 *
		 * @param header pinned header view.
		 * @param position position of the first visible list item.
		 * @param alpha fading of the header view, between 0 and 255.
		 */
		void configurePinnedHeader(View header, int position, int alpha);
	}

	public static final int MAX_ALPHA = 255;

	private PinnedHeaderAdapter mAdapter;
	private View mHeaderView;
	private boolean mHeaderViewVisible;

	private int mHeaderViewWidth;

	private int mHeaderViewHeight;

	private boolean mPushingDone = false;

	private boolean mPushingBegin = false;

	private boolean mPushingStateChangeContinue = false;

	public PinnedHeaderListView(Context context) {
		super(context);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setPinnedHeaderView(View view) {
		mHeaderView = view;
		// Disable vertical fading when the pinned header is present
		// TODO change ListView to allow separate measures for top and bottom fading edge;
		// in this particular case we would like to disable the top, but not the bottom edge.
		if (mHeaderView != null) {
			setFadingEdgeLength(0);
		}
		
		requestLayout();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter instanceof EndlessLoadAdapter) {
			EndlessLoadAdapter adap = (EndlessLoadAdapter) adapter;
			mAdapter = adap.getPinnedHeaderMediaAdapter();			
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mHeaderView != null) {
			measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
			mHeaderViewWidth = mHeaderView.getMeasuredWidth();
			mHeaderViewHeight = mHeaderView.getMeasuredHeight();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mHeaderView != null) {
			mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			configureHeaderView(getFirstVisiblePosition());
		}
	}

	public void configureHeaderView(int position) {
		if (mHeaderView == null) {
			return;
		}

		int state = mAdapter.getPinnedHeaderState(position);
		switch (state) {
		case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
			mHeaderViewVisible = false;
			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
			mAdapter.configurePinnedHeader(mHeaderView, position, MAX_ALPHA);
			if (mHeaderView.getTop() != 0) {
				mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			}
			mHeaderViewVisible = true;
			mPushingBegin  = true;

			if (isPushingDone()) {
				hideBehindCopyView(position);
			}

			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
			pushingUp(position);
			mPushingDone = true;
			
			if (isBeginPushing()) {
				showBehindCopyView(position+1);							
			}

			break;
		}
		}
	}

	private boolean showBehindCopyView(int position) {
		return setVision(position,true);
	}

	private boolean setVision(int position, boolean visible) {

		View view = getFirstVisibleView(position);

		if (view!=null) {
			View v = view.findViewById(R.id.header_text);
			if (v!=null) {
				if (visible) {
					if (v.getVisibility()==View.INVISIBLE) {
						v.setVisibility(View.VISIBLE);					
						return true;
					}
				}
				else{
					if (v.getVisibility()==View.VISIBLE) {
						v.setVisibility(View.INVISIBLE);					
						return true;
					}
				}
			}
		}
		return false;	
	}

	private void pushingUp(int position) {
		Log.i(TAG, "pushingUp start");

		View firstView = getChildAt(0);
		//			View firstView = getFirstVisibleView(position);
		int bottom = 0;
		if (firstView!=null) {
			bottom = firstView.getBottom();
		}
		//                int itemHeight = firstView.getHeight();
		int headerHeight = mHeaderView.getHeight();
		int y;
		int alpha;

		if (bottom < headerHeight) {
			y = (bottom - headerHeight);
			alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
			
			if (isPushingStateChangeContinue()) {
				showBehindCopyView(position+1);											
			}
		} else {
			y = 0;
			alpha = MAX_ALPHA;
			
			hideBehindCopyView(position);
			mPushingStateChangeContinue = true;
		}
		mAdapter.configurePinnedHeader(mHeaderView, position, alpha);
		if (mHeaderView.getTop() != y) {
			mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
		}
		mHeaderViewVisible = true;

		Log.i(TAG, "pushingUp end");
	}

	private boolean isPushingStateChangeContinue() {
		if (mPushingStateChangeContinue) {
			mPushingStateChangeContinue = false;
			return true;
		}
		return false;
	}

	private boolean isBeginPushing() {

		if (mPushingBegin) {
			mPushingBegin = false;
			return true;
		}
		return false;

	}

	private boolean isPushingDone() {
		Log.i(TAG, "isFirstTime start");

		if (mPushingDone) {
			mPushingDone = false;
			return true;
		}
		Log.i(TAG, "isFirstTime end");
		return false;
	}

	private boolean hideBehindCopyView(int position) {
		return setVision(position,false);
	}

	private View getFirstVisibleView(int position) {
		Log.i(TAG, "getFirstVisibleView start");
		View view = null;
		for (int i = 0; i < getChildCount(); i++) {
			view = getChildAt(i);
			if (view!=null) {
				Integer tag = (Integer) view.getTag();
				if (tag==position) {
					return view;
				}
			}
		}

		Log.i(TAG, "getFirstVisibleView end");
		return null;

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHeaderViewVisible) {
			drawChild(canvas, mHeaderView, getDrawingTime());
		}
	}

}
