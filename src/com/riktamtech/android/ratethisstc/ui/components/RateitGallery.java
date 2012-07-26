package com.riktamtech.android.ratethisstc.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import com.riktamtech.android.ratethisstc.db.AppSession;

public class RateitGallery extends Gallery {

	public RateitGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initt();
	}

	private void initt() {
		setCallbackDuringFling(false);
		if (AppSession.DEVICE_DENSITY>=240) {
			setAnimationDuration(120);
			log("setting aninm dur 120");
		}
		if (AppSession.DEVICE_SCREEN_HEIGHT>=850) {
			setAnimationDuration(240);
			log("setting aninm dur 350");
		}
	}

	public RateitGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	initt();
	}

	public RateitGallery(Context context) {
		super(context);
	initt();
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float x1 = e1.getX(), x2 = e2.getX();
		// if the user swipe is big enough to select next element of gallery, dont interfere
		if (Math.abs(x1 - x2) > (AppSession.DEVICE_SCREEN_WIDTH * 2 / 3.0f)) {
			return false;
		} else {
			changePage(x1 > x2);
			return true;
		}
	}

	private void changePage(boolean b) {
		if (b)
			onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
		else
			onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
	}

	public void slideToNext1() {
		changePage(true);
	}

	public void slideToNext() {
		//				float x=AppSession.DEVICE_SCREEN_WIDTH,y=AppSession.DEVICE_SCREEN_HEIGHT/2;
		//				MotionEvent event1 = MotionEvent.obtain(100, 100, MotionEvent.ACTION_DOWN, x/4, y, 0);
		//				MotionEvent event2= MotionEvent.obtain(100, 100, MotionEvent.ACTION_UP, x*3/4, y, 0);
		//				onFling(event1, event2, 3000, 0);
		//				onScroll(event1, event2, x/2, 0);
		changePage(true);
	}

	private void log(String message) {
		Log.d("RateitGallery", message);
	}

}
