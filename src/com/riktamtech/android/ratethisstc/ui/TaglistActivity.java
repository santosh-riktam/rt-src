package com.riktamtech.android.ratethisstc.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.app.ListActivity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.TagWrapper;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class TaglistActivity extends ListActivity {
	private ArrayList<TagWrapper> items;
	private LinearLayout panelLayout;
	private ImageButton upButton, downButton;
	private ListView listView;

	private TitleComponent titleComponent;
	private LinearLayout containerLayout;

	private CustomFontizer customFontizer;
	private AppDialogs dialogs;
	private int currentPostion = -1;
	private OnClickListener onClickListener;
	private boolean backPressedOnce = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.tag_list);

		dialogs=new AppDialogs(this);
		customFontizer = new CustomFontizer();

		items =AppUtils.getFiltersList();

		setListAdapter(new MyAdapter());

		listView = getListView();
		listView.setDivider(new ColorDrawable(Color.TRANSPARENT));

		onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == upButton) {
					if (currentPostion > 0) {
						swap(currentPostion, currentPostion - 1);
						if (listView.getFirstVisiblePosition() >= currentPostion || listView.getLastVisiblePosition() <= currentPostion) {
							listView.setSelection(currentPostion);

						}

					}
				} else if (v == downButton) {
					if (currentPostion < (items.size() - 1) && items.get(currentPostion + 1).enabled) {
						swap(currentPostion, currentPostion + 1);

						if (listView.getLastVisiblePosition() <= currentPostion || listView.getFirstVisiblePosition() >= currentPostion) {
							int numberOfVisibleRows = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition();
							listView.setSelection(currentPostion - numberOfVisibleRows + 2);

						}
					}
				} else if (v == titleComponent.imageView) {
					titleComponent.imageView.setEnabled(false);
					Animation animation = AppAnimations.pushingDoorOpen();
					animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {

							containerLayout.setVisibility(View.INVISIBLE);
							String tags = "";
							int i;
							for (i = 0; i < items.size(); i++) {
								TagWrapper tagWrapper = items.get(i);
								String t = (tagWrapper.initialPosition + 1) + "," + (tagWrapper.enabled ? 1 : 0);
								tags = tags + "," + t;
							}
							AppSession.signedInUser.filtersString=tags.substring(1);
							setResult(RESULT_OK);
							finish();
							overridePendingTransition(0, 0);
							
						}
					});
					containerLayout.startAnimation(animation);
				}
			}
		};

		panelLayout=(LinearLayout) findViewById(R.id.LinearLayout02);
		panelLayout.setVisibility(View.GONE);
		
		upButton = (ImageButton) findViewById(R.id.ImageButton01);
		upButton.setOnClickListener(onClickListener);

		downButton = (ImageButton) findViewById(R.id.ImageButton02);
		downButton.setOnClickListener(onClickListener);

		containerLayout = (LinearLayout) findViewById(R.id.LinearLayout01);
		containerLayout.startAnimation(AppAnimations.pullingDoorClose());

		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "Preferences!  ", R.drawable.back_btn, containerLayout);
		titleComponent.imageView.setOnClickListener(onClickListener);
	}
	
	protected void swap(int currentPostion2, int i) {
		Collections.swap(items, currentPostion2, i);
		currentPostion = i;
		((MyAdapter) getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		if (items.get(position).enabled) {
			panelLayout.setVisibility(View.VISIBLE);
		}
				
		TagWrapper tagWrapper = items.get(position);
		if (tagWrapper.enabled) {
			int first = listView.getFirstVisiblePosition(), last = listView.getLastVisiblePosition();
			View prevView = null;
			if (currentPostion >= first || currentPostion <= last) {
				int curpos = currentPostion - first;
				prevView = listView.getChildAt(curpos);
			}
			if (prevView != null)
				prevView.setBackgroundColor(Color.BLACK);
			v.setBackgroundColor(Color.GRAY);
			currentPostion = position;
		} else {
			enable(position);
		}
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return items.size();
		}

		@Override
		public Object getItem(int arg0) {

			return arg0;
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			View v = arg1;
			LinearLayout ll;
			TextView textView;
			ImageView imageView;

			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.tag_list_row, null);
				customFontizer.fontize((LinearLayout) v, R.id.textView1);
				imageView = (ImageView) ((LinearLayout) v).findViewById(R.id.imageView1);

			}
			ll = (LinearLayout) v;
			textView = (TextView) ll.findViewById(R.id.textView1);
			imageView = (ImageView) ll.findViewById(R.id.imageView1);
			final int position = arg0;
			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					disable(position);
				}
			});

			TagWrapper tagWrapper = items.get(arg0);
			textView.setText(tagWrapper.text);
			if (tagWrapper.enabled) {
				imageView.setImageResource(R.drawable.x_mark);
				textView.setTextColor(Color.WHITE);
			} else {
				imageView.setImageBitmap(null);
				textView.setTextColor(Color.parseColor("#7F7F7F"));
			}
			if (arg0 == currentPostion)
				ll.setBackgroundColor(Color.GRAY);
			else
				ll.setBackgroundColor(Color.BLACK);
			v = ll;
			return v;
		}

	}

	@Override
	public void onBackPressed() {
		if (!backPressedOnce) {
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	public void disable(int position) {
		log("postion clicked  " + position);
		if (position == 0 && (!items.get(1).enabled)) {
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_ATLEAST_ONE_TAG)).show();
		}
		else {
			TagWrapper wrapper = items.get(position);
			wrapper.enabled = false;
			items.remove(position);
			items.add(items.size(), wrapper);
			((MyAdapter) listView.getAdapter()).notifyDataSetChanged();
		}
	}

	public void enable(int pos) {
		// cycle the items to find the last enabled item and insert it there
		int i = items.size() - 1;
		int lastEnabledPos = -2;
		while (i >= 0) {
			if (items.get(i).enabled) {
				lastEnabledPos = i;
				break;
			}
			i--;
		}
		if (!items.get(0).enabled) {
			lastEnabledPos = -1;
		}
		if (lastEnabledPos > -2) {
			TagWrapper tagWrapper = items.get(pos);
			tagWrapper.enabled = true;
			items.remove(pos);
			items.add(lastEnabledPos + 1, tagWrapper);
			((MyAdapter) listView.getAdapter()).notifyDataSetChanged();
		}
	}

	public void log(String msg) {
		Log.d("TagList", msg);
	}
}
