package com.riktamtech.android.ratethisstc.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import com.riktamtech.android.ratethisstc.db.AppSession;

public class RatedDetailsGallery extends Gallery {

	public RatedDetailsGallery(Context context) {
		super(context);
		setCallbackDuringFling(false);
	}

	public RatedDetailsGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCallbackDuringFling(false);
	}

	public RatedDetailsGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCallbackDuringFling(false);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float x1=e1.getX(),x2=e2.getX();
		// if the user swipe is big enough to select next element of gallery, dont interfere
		if (Math.abs(x1-x2)>(AppSession.DEVICE_SCREEN_WIDTH*2/3.0f)) {
			return false;
		}
		else {
		changePage(x1>x2);
		return true;
		}
	}
	
	/**
	 * changes page
	 * @param b - if true- shows next else shows prev page
	 */
	public void changePage(boolean b) {
		if (b) onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
		else onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
	}
	
	
	@SuppressWarnings("unused")
	private void log(String message) {
		Log.d("RatedDetailsGallery", message);
	}
}
