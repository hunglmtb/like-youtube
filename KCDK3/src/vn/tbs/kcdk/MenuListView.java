package vn.tbs.kcdk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class MenuListView extends ListView {

	private boolean mScrollingDisable = false;
	private float xDistance, yDistance, lastX, lastY;
	public MenuListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MenuListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setScrollingDisable(boolean enable) {
		this.mScrollingDisable = enable;
	}

	@Override 
	public boolean onTouchEvent(MotionEvent ev) 
	{ 
		boolean b = super.onTouchEvent(ev);
		switch (ev.getAction()) 
		{
		case MotionEvent.ACTION_MOVE: 
			if (mScrollingDisable) {
				this.smoothScrollBy(0, 0);
			}
			break; 
		}
		return b;
	}

	/*	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			lastX = ev.getX();
			lastY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();
			xDistance += Math.abs(curX - lastX);
			yDistance += Math.abs(curY - lastY);
			lastX = curX;
			lastY = curY;
			return mScrollingDisable;
		}

		return super.onInterceptTouchEvent(ev);

	}*/
}
