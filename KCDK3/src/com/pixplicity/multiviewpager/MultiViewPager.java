package com.pixplicity.multiviewpager;

import vn.tbs.kcdk.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class MultiViewPager extends ViewPager {

    /**
     * Maximum size.
     */
    private int mMaxWidth = -1;
    /**
     * Maximum size.
     */
    private int mMaxHeight = -1;
    /**
     * Child view inside a page to match the page size against.
     */
    private int mMatchWidthChildResId;

    /**
     * Internal state to schedule a new measurement pass.
     */
    private boolean mNeedsMeasurePage;

    private static void constrainTo(Point size, Point maxSize) {
        if (maxSize.x >= 0) {
            if (size.x > maxSize.x) {
                size.x = maxSize.x;
            }
        }
        if (maxSize.y >= 0) {
            if (size.y > maxSize.y) {
                size.y = maxSize.y;
            }
        }
    }

    public MultiViewPager(Context context) {
        super(context);
    }

    public MultiViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setClipChildren(false);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MultiViewPager);
        setMaxWidth(ta.getDimensionPixelSize(R.styleable.MultiViewPager_android_maxWidth, -1));
        setMaxHeight(ta.getDimensionPixelSize(R.styleable.MultiViewPager_android_maxHeight, -1));
        setMatchChildWidth(ta.getResourceId(R.styleable.MultiViewPager_matchChildWidth, 0));
        ta.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Point size = new Point(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
        if (mMaxWidth >= 0 || mMaxHeight >= 0) {
            Point maxSize = new Point(mMaxWidth, mMaxHeight);
            constrainTo(size, maxSize);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    size.x,
                    MeasureSpec.EXACTLY);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    size.y,
                    MeasureSpec.EXACTLY);
        }
        int height = 0;
        int minHeight  = 0;
		for(int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
			int h = child.getMeasuredHeight();
			if(h > height) height = h;
			if (i==0) {
				minHeight = h;
			}
			minHeight = minHeight>height?height:minHeight;
		}

		heightMeasureSpec = MeasureSpec.makeMeasureSpec(getCurrentItem()==0?height:minHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        onMeasurePage(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onMeasurePage(int widthMeasureSpec, int heightMeasureSpec) {
        // Only measure if a measurement pass was scheduled
        if (!mNeedsMeasurePage) {
            return;
        }
        if (mMatchWidthChildResId == 0) {
            mNeedsMeasurePage = false;
        } else if (getChildCount() > 0) {
            View child = getChildAt(0);
            child.measure(widthMeasureSpec, heightMeasureSpec);
            int pageWidth = child.getMeasuredWidth();
            View match = child.findViewById(mMatchWidthChildResId);
            if (match == null) {
                throw new NullPointerException(
                        "MatchWithChildResId did not find that ID in the first fragment of the ViewPager; "
                                + "is that view defined in the child view's layout? Note that MultiViewPager "
                                + "only measures the child for index 0.");
            }
            int childWidth = match.getMeasuredWidth();
            // Check that the measurement was successful
            if (childWidth > 0) {
                mNeedsMeasurePage = false;
                int difference = pageWidth - childWidth;
                setPageMargin(-difference);
                int offscreen = (int) Math.ceil((float) pageWidth / (float) childWidth) + 1;
                setOffscreenPageLimit(offscreen);
                requestLayout();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Schedule a new measurement pass as the dimensions have changed
        mNeedsMeasurePage = true;
    }

    /**
     * Sets the child view inside a page to match the page size against.
     * 
     * @param matchChildWidthResId
     */
    public void setMatchChildWidth(int matchChildWidthResId) {
        if (mMatchWidthChildResId != matchChildWidthResId) {
            mMatchWidthChildResId = matchChildWidthResId;
            mNeedsMeasurePage = true;
        }
    }

    /**
     * Sets the maximum size.
     * 
     * @param width
     */
    public void setMaxWidth(int width) {
        mMaxWidth = width;
    }

    /**
     * Sets the maximum size.
     * 
     * @param height
     */
    public void setMaxHeight(int height) {
        mMaxHeight = height;
    }

}
