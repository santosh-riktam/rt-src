package com.riktamtech.android.ratethisstc.animations;

import com.riktamtech.android.ratethisstc.db.AppSession;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;


public class AppAnimations
{
	private static int duration=400;
	public static Animation pullingDoorOpen()
	{
	Rotate3dAnimation animation=new Rotate3dAnimation(0, -90, 0,0,  0.5f, false);
		animation.setDuration(duration);
		return animation;
	}
	
	public static Animation pullingDoorClose()
	{
		Rotate3dAnimation animation=new Rotate3dAnimation(90.5f, 0.5f, 0,AppSession.DEVICE_SCREEN_HEIGHT/2, 0f, false);
		animation.setDuration(duration);
		return animation;
	}
	public static Animation pushingDoorClose()
	{ 
		Rotate3dAnimation animation=new Rotate3dAnimation(-90.5f, 0.5f, 0.5f, .5f,  0.5f, false);
		animation.setDuration(duration);	
		return animation;
	}
	public static Animation pushingDoorOpen()

	{
		Rotate3dAnimation animation=new Rotate3dAnimation(0.5f, 90.5f,0, AppSession.DEVICE_SCREEN_HEIGHT/2 ,  0.5f, false);
		animation.setDuration(duration);
		return animation;
	}
	
	public static Animation pullingDoorOpen(int centerX,int centerY)
	{
		Rotate3dAnimation animation=new Rotate3dAnimation(0.5f, -90.5f, centerX, centerY,  -0.5f, false);
		animation.setDuration(duration);		
		
		return animation;
	}
	public static Animation pushingDoorClose(int centerX,int centerY)
	{ 
		Rotate3dAnimation animation=new Rotate3dAnimation(-90.5f, 0.5f,centerX, centerY, 0.5f, false);
		animation.setDuration(duration);	
		return animation;
	}
	
	public static Animation inFromRightAnimation()
	{
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(350);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	public static Animation outToLeftAnimation()
	{
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(350);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	// for the next movement
	public static Animation inFromLeftAnimation()
	{
		Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(350);
		inFromLeft.setInterpolator(new AccelerateInterpolator());

		return inFromLeft;
	}

	public static Animation outToRightAnimation()
	{
		Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoRight.setDuration(350);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		return outtoRight;
	}
	
	public static Animation inFromBottomAnimation()
	{
		Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromLeft.setDuration(250);
		inFromLeft.setInterpolator(new AccelerateInterpolator());
		inFromLeft.setFillAfter(true);
		return inFromLeft;
	}

	public static Animation outToBottomAnimation()
	{
		Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f);
		outtoRight.setDuration(250);
		outtoRight.setInterpolator(new AccelerateInterpolator());
		outtoRight.setFillAfter(true);
		return outtoRight;
	}


}
