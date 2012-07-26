package com.riktamtech.android.ratethisstc.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class MyAnimListener implements AnimationListener
{

	boolean shouldDisappear;
	View v;

	
	public MyAnimListener(boolean shouldDisappear, View v)
	{
		super();
		this.shouldDisappear = shouldDisappear;
		this.v = v;
	}

	@Override
	public void onAnimationEnd(Animation animation)
	{
		
		if (shouldDisappear)
			{
				v.setVisibility(View.INVISIBLE);
			}
	}

	@Override
	public void onAnimationRepeat(Animation animation)
	{
		

	}

	@Override
	public void onAnimationStart(Animation animation)
	{
		
		if (!shouldDisappear)
			{
				v.setVisibility(View.VISIBLE);
			}
	}

}
