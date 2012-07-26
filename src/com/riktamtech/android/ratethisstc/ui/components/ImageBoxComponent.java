package com.riktamtech.android.ratethisstc.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.riktamtech.android.ratethisstc.R;

public class ImageBoxComponent extends FrameLayout implements OnClickListener {
	public FrameLayout frameLayout;
	public ImageView contentView, galleryImageView, cameraImageView;

	public ImageBoxComponent(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		LayoutInflater.from(context).inflate(R.layout.image_selection_comp_new,
				this, true);
		initVariables();
		registerEvents();
	}

	private void registerEvents() {
		// TODO Auto-generated method stub
		frameLayout.setOnClickListener(this);
		galleryImageView.setOnClickListener(this);
		cameraImageView.setOnClickListener(this);

	}

	public void initVariables() {
		frameLayout = (FrameLayout) findViewById(R.id.IMCNFrameLayout01);
		contentView = (ImageView) findViewById(R.id.IMCNImageView01);
		cameraImageView = (ImageView) findViewById(R.id.IMCNImageView02);
		galleryImageView = (ImageView) findViewById(R.id.IMCNImageView03);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	public boolean hasImage() {
		if (contentView.getDrawable() == null)
			return false;
		else
			return true;
	}
}
