package com.windrealm.android;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SimpleListViewActivity extends Activity implements OnItemClickListener {

	private ListView mainListView ;
	private ArrayAdapter<String> listAdapter ;
	private Button mButton;
	private float mOriginDistance = 1.0f;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Find the ListView resource. 
		mButton = (Button) findViewById( R.id.button );

		mainListView = (ListView) findViewById( R.id.mainListView );

		// Create and populate a List of planet names.
		String[] planets = new String[] { "Mercury", "Venus", "Earth", "Mars",
				"Jupiter", "Saturn", "Uranus", "Neptune"};  
		ArrayList<String> planetList = new ArrayList<String>();
		planetList.addAll( Arrays.asList(planets) );

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList);

		// Add more planets. If you passed a String[] instead of a List<String> 
		// into the ArrayAdapter constructor, you must not add more items. 
		// Otherwise an exception will occur.
		listAdapter.add( "Ceres" );
		listAdapter.add( "Pluto" );
		listAdapter.add( "Haumea" );
		listAdapter.add( "Makemake" );
		listAdapter.add( "Eris" );

		// Set the ArrayAdapter as the ListView's adapter.
		mainListView.setAdapter( listAdapter );      
		mainListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {

		mButton.clearAnimation();
		float relativeDistance = mOriginDistance+0.5f;
		
		// Limit it to 0-1 if it goes beyond 0-1 for any reason.
//		relativeDistance=Math.max(relativeDistance, 0);
//		relativeDistance=Math.min(relativeDistance, 1);
		
		if (arg2%2==0) {
			/*// Setup animations
			AnimationSet animations = new AnimationSet(true);
			animations.setFillAfter(true);
			Animation animationAlpha = new AlphaAnimation(
					relativeDistance, 
					relativeDistance);
			animations.addAnimation(animationAlpha);
			
			Animation animationScale = new ScaleAnimation(
					relativeDistance, 
					relativeDistance, 
					relativeDistance, 
					relativeDistance);
			animations.addAnimation(animationScale);
			mButton.startAnimation(animations);*/
			expand(mButton);
		}
		else{
			//mButton.clearAnimation();
			collapse(mButton);
		}

	}
	
	public static void expand(final View v) {
	    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    final int targtetHeight = v.getMeasuredHeight();

	    v.getLayoutParams().height = 0;
	    v.setVisibility(View.VISIBLE);
	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) {
	            v.getLayoutParams().height = interpolatedTime == 1
	                    ? LayoutParams.WRAP_CONTENT
	                    : (int)(targtetHeight * interpolatedTime);
	            v.getLayoutParams().height += 2;
	            v.getLayoutParams().width += 2;
	            v.getLayoutParams().width += 2;
	            v.requestLayout();
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(targtetHeight / v.getContext().getResources().getDisplayMetrics().density));
	    v.startAnimation(a);
	}

	public static void collapse(final View v) {
	    final int initialHeight = v.getMeasuredHeight();

	    Animation a = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) {
	            if(interpolatedTime == 1){
	                v.setVisibility(View.GONE);
	            }else{
	                v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
	                v.requestLayout();
	            }
	        }

	        @Override
	        public boolean willChangeBounds() {
	            return true;
	        }
	    };

	    // 1dp/ms
	    a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
	    v.startAnimation(a);
	}
}