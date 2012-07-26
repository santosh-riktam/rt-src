package com.riktamtech.android.ratethisstc.ui.components;

import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

/**
 * 
 * @author santu this class encapsulates the logic for displaying the title
 *         component sets the text and icon
 */
public class TitleComponent {
	public TextView textView;
	public ImageView imageView;
	public Activity activity;
	public View toAnimateView;

	public View.OnClickListener imageClickListener;

	private View parentView;

	public void initVariables(Activity act, View v, String text, View toAnimateView) {
		activity = act;
		parentView = v;
		textView = (TextView) v.findViewById(R.id.TitleTextView);
		new CustomFontizer().fontize((ViewGroup) v, R.id.TitleTextView);
		imageView = (ImageView) v.findViewById(R.id.TitleImageView);
		textView.setText(text);
		if (AppSession.DEVICE_DENSITY == 120) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 65);
			log("120 dpi setting size to 65 dp");
		} else if (AppSession.DEVICE_DENSITY == 160) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 70);
			log("160 dpi setting size to 70 dp");
		} else if (AppSession.DEVICE_DENSITY >= 240) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80);
			LinearLayout.LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
			layoutParams.topMargin = -36;
			log("240 dpi setting size to 80 dp");
		}

		this.toAnimateView = toAnimateView;
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				imageView.setEnabled(false);
				if (imageClickListener != null) {
					imageClickListener.onClick(v);
				}
				Animation animation = AppAnimations.pushingDoorOpen();
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						TitleComponent.this.toAnimateView.setVisibility(View.INVISIBLE);
						activity.finish();
						activity.overridePendingTransition(0, 0);
					}
				});
				TitleComponent.this.toAnimateView.startAnimation(animation);

			}
		});

	}

	public TitleComponent(Activity act, View v, String text, View toAnimateView) {
		initVariables(act, v, text, toAnimateView);
	}

	public TitleComponent(Activity act, View v, String text, int resource, View toAnimateView) {
		initVariables(act, v, text, toAnimateView);
		if (resource != -1)
			imageView.setBackgroundResource(resource);
		else {
			((LinearLayout) parentView).removeView(imageView);

		}

	}

	private void log(String msg) {
		Log.d("TitleComponent", msg);
	}
}
