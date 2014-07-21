package com.windrealm.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

RelativeLayout rl_footer;
ImageView iv_header;
boolean isBottom = true;
Button btn1;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_activity);
    rl_footer = (RelativeLayout) findViewById(R.id.rl_footer);
    iv_header = (ImageView) findViewById(R.id.iv_up_arrow);
    iv_header.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            iv_header.setImageResource(R.drawable.ic_action_back);
            iv_header.setPadding(0, 10, 0, 0); 
            //int cl;
			//rl_footer.setBackgroundColor(cl);
            if (isBottom) {
                SlideToAbove();
                isBottom = false;
            } else {
                iv_header.setImageResource(R.drawable.ic_action_forward);
                iv_header.setPadding(0, 0, 0, 10);
                //rl_footer.setBackgroundResource(R.drawable.ic_action_attachment);
                SlideToDown();
                isBottom = true;
            }

        }
    });

}
private int sCount =0;

@SuppressLint("NewApi")
public void SlideToAbove() {
	AnimationSet animations = new AnimationSet(true);
	animations.setFillAfter(true);
	
    Animation slide = null;
    slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, -5.0f);

    slide.setDuration(1000);
    slide.setFillAfter(true);
    slide.setFillEnabled(true);
    
    
    animations.addAnimation(slide);

    int screenHeight = getResources().getDisplayMetrics().heightPixels;
    float relativeDistance = (rl_footer.getHeight()+sCount)/(float)screenHeight;
	Animation animationScale = new ScaleAnimation(
			relativeDistance, 
			relativeDistance, 
			relativeDistance, 
			relativeDistance);
	animations.addAnimation(animationScale);
	
    rl_footer.startAnimation(animations);

    slide.setAnimationListener(new AnimationListener() {


		@Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        	sCount +=10;
        }

        @Override
        public void onAnimationEnd(Animation animation) {

            rl_footer.clearAnimation();

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    rl_footer.getWidth(),  rl_footer.getHeight());//RelativeLayout.LayoutParams.MATCH_PARENT);
            // lp.setMargins(0, 0, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            rl_footer.setLayoutParams(lp);

        }

    });

}

public void SlideToDown() {
    Animation slide = null;
    slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

    slide.setDuration(1000);
    slide.setFillAfter(true);
    slide.setFillEnabled(true);
    rl_footer.startAnimation(slide);

    slide.setAnimationListener(new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {

            rl_footer.clearAnimation();

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    rl_footer.getWidth(), rl_footer.getHeight());
            lp.setMargins(0, rl_footer.getWidth(), 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            rl_footer.setLayoutParams(lp);

        }

    });

}



 }