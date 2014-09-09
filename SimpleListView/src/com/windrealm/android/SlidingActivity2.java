package com.windrealm.android;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

public class SlidingActivity2 extends Activity{


	private SmartViewWithMenu mSmartViewWithMenu;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayAdapter<String> listAdapter = null;
		
		// Create and populate a List of planet names.
		String[] planets = new String[] { "kaka", "keke", "kiki", "kuku",
				"Jupiter", "Saturn", "Uranus", "Neptune", "Venus", "Mars", "Venus", "Neptune", "Neptune","kaka", "keke", "kiki", "kuku"};  
		ArrayList<String> planetList = new ArrayList<String>();
		planetList.addAll( Arrays.asList(planets) );

		// Create ArrayAdapter using the planet list.
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, planetList	);
		
		//----------------------------------------------------------------------------
		mSmartViewWithMenu  = new SmartViewWithMenu(this, listAdapter);
		View view = mSmartViewWithMenu.getView();
		setContentView(view);
	}
	public void onBackPressed() {
		if (mSmartViewWithMenu==null||(mSmartViewWithMenu!=null&&mSmartViewWithMenu.onBackPressed())) {
			super.onBackPressed();
		}
	}
}