package com.riktamtech.android.ratethisstc.ui.components;

import android.view.View;
import android.widget.ImageView;

import com.riktamtech.android.ratethisstc.R;

public class ShareOp {
	public View view;
	public boolean isShown;
	public ImageView tv;
	public ImageView facebookIcon, twitterIcon, mailIcon;

	public int button_up, button_down;

	public ShareOp(View v, int buttonUp, int buttonDown) {
		view = v;
		isShown = true;
		tv = (ImageView) v.findViewById(R.id.SOImageView04);
		mailIcon = (ImageView) v.findViewById(R.id.SOImageView01);
		facebookIcon = (ImageView) v.findViewById(R.id.SOImageView02);
		twitterIcon = (ImageView) v.findViewById(R.id.SOImageView03);
		this.button_down = buttonDown;
		this.button_up = buttonUp;
		toggleView();

	}

	public void toggleView() {
		if (isShown) {
			facebookIcon.setVisibility(View.GONE);
			twitterIcon.setVisibility(View.GONE);
			mailIcon.setVisibility(View.GONE);
			tv.setBackgroundResource(button_up);
		} else {
			facebookIcon.setVisibility(View.VISIBLE);
			twitterIcon.setVisibility(View.VISIBLE);
			mailIcon.setVisibility(View.VISIBLE);
			tv.setBackgroundResource(button_down);
		}
		isShown = !isShown;

	}
}
