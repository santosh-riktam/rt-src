package com.riktamtech.android.ratethisstc.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;

public class MainMenuAdapter extends BaseAdapter {
	Context ctx;

	public MainMenuAdapter(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public int getCount() {

		return 9;
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// we ll show an empty view at first for smooth animation
		position--;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.main_menu_text_view, null);
			convertView = layout;
		}
		TextView tv = (TextView) convertView.findViewById(R.id.TextView01);
		switch (position) {
		case -1:
			tv.setBackgroundColor(Color.TRANSPARENT);
			break;
		case 0:
			tv.setBackgroundResource(R.drawable.newrate_text);
			break;
		case 1:
			tv.setBackgroundResource(R.drawable.rateit_text);
			break;
		case 2:
			tv.setBackgroundResource(R.drawable.myrates_text);
			break;
		case 3:
			tv.setBackgroundResource(R.drawable.rated_text);
			break;
		case 4:
			tv.setBackgroundResource(R.drawable.myprofile_text);
			break;
		case 5:
			tv.setBackgroundResource(R.drawable.settings_text);
			break;
		case 6:
			tv.setBackgroundResource(R.drawable.shareit_text);
			break;
		case 7:
			tv.setBackgroundResource(R.drawable.help_text);
			break;
		default:
			break;
		}

		return convertView;
	}
}
