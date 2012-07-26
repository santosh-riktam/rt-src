package com.riktamtech.android.ratethisstc.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.MyRatesDAO;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ImagesCacheSD;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.RatesException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.ui.components.RatingView;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class RatedActivity extends Activity implements OnItemClickListener {
	private ListView listView;
	private TitleComponent titleComponent;
	private ImagesCacheSD imagesCache;
	private boolean isMyRates;
	private boolean backPressedOnce = false;
	private ProgressDialog progressDialog;
	public boolean hasMoreRates = true;
	private AppDialogs dialogs;
	private ArrayList<MyRatesDAO> listItems;
	private boolean loading;
	private boolean displayNewRate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.rated);
		dialogs = new AppDialogs(this);
		imagesCache = new ImagesCacheSD();
		listView = (ListView) findViewById(R.id.RatedListView01);
		isMyRates = getIntent().getBooleanExtra("isMyRates", false);
		if (isMyRates) {
			titleComponent = new TitleComponent(this,
					findViewById(R.id.TitleComp), "MyRates! ", listView);
		} else {
			titleComponent = new TitleComponent(this,
					findViewById(R.id.TitleComp), "Rated!   ", listView);
		}

		if (ServiceConnector.testConnection(getApplication()))
			initt();
		else {
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_NOT_CONNECTED),
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
		}

	}

	void postLoadListTask(ArrayList<MyRatesDAO> result,
			boolean showLoadingDialog, boolean appendNewRate,
			boolean appendHelpRates) {
		MyListAdapter listAdapter = (MyListAdapter) listView.getAdapter();
		if (listAdapter == null)
			listView.setAdapter(new MyListAdapter());
		listAdapter = (MyListAdapter) listView.getAdapter();

		if (showLoadingDialog && progressDialog != null) {
			progressDialog.dismiss();
			// new item should be displayed
			if (appendNewRate && isMyRates) {
				if (listItems.size() > 0) {
					listItems.add(0, new MyRatesDAO(this,
							AppSession.cachedRateDAO));
				} else {
					listItems
							.add(new MyRatesDAO(this, AppSession.cachedRateDAO));
				}
			}
		}
		if (result != null)
			listItems.addAll(result);
		if (appendHelpRates)
			listItems.addAll(ServiceConnector.getHelpfulRates(isMyRates));

		int firstVisiblePosition = 0, top = 0;
		if (listView.getChildCount() > 0) {
			firstVisiblePosition = listView.getFirstVisiblePosition();
			top = listView.getChildAt(0).getTop();
		}
		listAdapter.notifyDataSetChanged();
		listView.setSelectionFromTop(firstVisiblePosition, top);
		loading = false;
		log("Size of list is " + listItems.size()
				+ " child count of list view is " + listView.getChildCount());
		log("lsit " + listItems);

		if (isMyRates)
			AppSession.myRatesArrayList = listItems;
		else
			AppSession.ratedArrayList = listItems;

		if (showLoadingDialog) {
			listView.startAnimation(AppAnimations.pullingDoorClose());
		}

	}

	void progressUpdateLoadListTask(Exception e1) {

		if (e1.getClass() == WebServiceException.class) {
			e1.printStackTrace();
			dialogs.getAlertDialog(getResources().getString(
					R.string.ALRT_NOT_CONNECTED));
		}
	}

	private static class LoadListTask extends
			AsyncTask<Object, Exception, ArrayList<MyRatesDAO>> {

		private boolean showLoadingDialog;
		private boolean appendNewRate;
		WeakReference<RatedActivity> reference;
		private boolean appendHelpRates = false;

		public LoadListTask(RatedActivity activity, boolean showLoadingDialog,
				boolean appendNewRate) {
			super();
			reference = new WeakReference<RatedActivity>(activity);
			this.showLoadingDialog = showLoadingDialog;
			this.appendNewRate = appendNewRate;
		}

		@Override
		protected void onPreExecute() {
			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return;
			super.onPreExecute();
			ratedActivity.loading = true;
			ratedActivity.log("show dialog = " + showLoadingDialog
					+ ", loading started ");
			if (showLoadingDialog) {
				ratedActivity.progressDialog = ProgressDialog.show(
						ratedActivity, null, ratedActivity.getResources()
								.getString(R.string.PRG_TITLE) + "...");
				// progressDialog.setCancelable(true);
			}

		}

		@Override
		protected ArrayList<MyRatesDAO> doInBackground(Object... params) {

			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return null;

			int size = ratedActivity.listItems.size(), qid = 0;
			if (size > 0) {
				qid = ratedActivity.isMyRates ? ratedActivity.listItems
						.get(size - 1).qid : ratedActivity.listItems
						.get(size - 1).rateId;
			}
			try {
				ArrayList<MyRatesDAO> itemsReturnesd = ServiceConnector
						.myRatesRequest(ratedActivity.getApplication(), qid,
								ratedActivity.isMyRates);
				return itemsReturnesd;
			} catch (RatesException e) {
				// TODO Handle here
				appendHelpRates = true;
				ratedActivity.log(e.getMessage());
				ratedActivity.hasMoreRates = false;
			} catch (Exception e) {

				publishProgress(e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<MyRatesDAO> result) {

			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return;
			ratedActivity.postLoadListTask(result, showLoadingDialog,
					appendNewRate, appendHelpRates);
		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);

			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return;
			ratedActivity.progressUpdateLoadListTask(values[0]);
		}

	}

	private void log(String msg) {
		if (msg.contains("no more"))
			Log.e("RatedActivity", msg);
		else {
			Log.d("RatedActivity", msg);

		}
	}

	@Override
	public void onBackPressed() {
		if (!backPressedOnce) {
			// super.onBackPressed();
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	class MyListAdapter extends BaseAdapter {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == (listItems.size()) && hasMoreRates) {
				TextView textView = new TextView(RatedActivity.this);
				// TODO Externalize this string
				textView.setText("Loading items....");
				textView.setPadding(10, 10, 10, 10);
				textView.setTextColor(Color.WHITE);
				textView.setGravity(Gravity.CENTER);
				return textView;
			}
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.rated_row, null);
				holder = new ViewHolder(v);
				v.setTag(holder);
				new CustomFontizer().fontize((ViewGroup) v,
						R.id.RatedRowTextView01, R.id.RatedRowTextView02,
						R.id.RatedRowTextView03, R.id.RatedRowTextView04,
						R.id.RatedRowTextView05, R.id.TextView02,
						R.id.textView1, R.id.textView2, R.id.TextView01);
			} else {
				holder = (ViewHolder) v.getTag();

			}

			MyRatesDAO myRatesRowDAO = listItems.get(position);

			String secondarytagA = " : " + myRatesRowDAO.secondarytag_A, secondarytagB = " : "
					+ myRatesRowDAO.secondarytag_B, primaryTagString = " : "
					+ myRatesRowDAO.primarytag;
			// String secondarytagA = myRatesRowDAO.secondarytag_A,
			// secondarytagB = myRatesRowDAO.secondarytag_B, primaryTagString =
			// myRatesRowDAO.primarytag;
			int len = 15;// for hdpis
			if (AppSession.DEVICE_DENSITY <= 160) {
				len = 12;

			}
			if (primaryTagString.length() > (len + 1)) {
				primaryTagString = primaryTagString.substring(0, len + 1)
						+ "..";
			}
			if (secondarytagA.length() > len)
				secondarytagA = secondarytagA.substring(0, len) + "...";
			if (secondarytagB.length() > len)
				secondarytagB = secondarytagB.substring(0, len) + "...";

			holder.tv1.setText(primaryTagString);
			holder.tv2a.setText(secondarytagA);
			holder.tv2b.setText(secondarytagB);

			holder.tv3.setText(" : " + myRatesRowDAO.date_posted);
			String ratesText = (myRatesRowDAO.votes == 1) ? getResources()
					.getString(R.string.RatedRate) : getResources().getString(
					R.string.RatedRates);
			holder.tv4.setText(+myRatesRowDAO.votes + " " + ratesText);
			if (!myRatesRowDAO.isCompleted) {

				holder.im1.setBackgroundResource(R.drawable.orange_border);
				holder.im2.setBackgroundResource(R.drawable.orange_border);
				holder.ratingView.initt(myRatesRowDAO.getPercent(), false,
						false, false);
			}
			if (myRatesRowDAO.isFlagged) {
				holder.im1.setBackgroundResource(R.drawable.red_border);
				holder.im2.setBackgroundResource(R.drawable.red_border);
				holder.ratingView.initt(myRatesRowDAO.getPercent(), false,
						true, true);
			}

			if (myRatesRowDAO.isCompleted && !myRatesRowDAO.isFlagged) {

				float lPercent = myRatesRowDAO.getPercent();
				holder.ratingView.initt(lPercent, false, true, false);
				if (lPercent < 0.5) {
					holder.im1.setBackgroundResource(R.drawable.red_border);
					holder.im2.setBackgroundResource(R.drawable.green_border);
				} else if (lPercent == 0.5) {
					holder.im1.setBackgroundResource(R.drawable.blue_border);
					holder.im2.setBackgroundResource(R.drawable.blue_border);
				} else {
					holder.im1.setBackgroundResource(R.drawable.green_border);
					holder.im2.setBackgroundResource(R.drawable.red_border);
				}
			}

			log(primaryTagString + "," + secondarytagA + "," + secondarytagB
					+ "-");

			holder.im1.setImageBitmap(null);
			holder.im2.setImageBitmap(null);

			String WS_SERVER_IMAGES_DIRECTORY = getResources().getString(
					R.string.WSImages);

			if (myRatesRowDAO.isLocalRate) {
				setImage(holder.im1, position, myRatesRowDAO.localImage1);
				setImage(holder.im2, position, myRatesRowDAO.localImage2);
			} else {
				if (myRatesRowDAO.qidString != null) {
					setImage(holder.im1, position, WS_SERVER_IMAGES_DIRECTORY
							+ myRatesRowDAO.qidString + "_2_1.jpg");
					setImage(holder.im2, position, WS_SERVER_IMAGES_DIRECTORY
							+ myRatesRowDAO.qidString + "_2_2.jpg");

				} else {
					setImage(holder.im1, position, WS_SERVER_IMAGES_DIRECTORY
							+ myRatesRowDAO.qid + "_2_1.jpg");
					setImage(holder.im2, position, WS_SERVER_IMAGES_DIRECTORY
							+ myRatesRowDAO.qid + "_2_2.jpg");

				}

			}
			return v;
		}

		void setImage(ImageView imageView, int position, String url) {
			if (url.startsWith(getResources().getString(R.string.WSImages))) {
				if (imagesCache.containsImage(url))
					imageView.setImageBitmap(imagesCache
							.getBitmapFromCache(url));
				else {
					WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(
							imageView);
					new BitmapDownloaderTask(RatedActivity.this,
							imageViewReference, position).execute(url);
				}
			} else {
				imageView.setImageBitmap(AppUtils
						.scaleBitmapForThumbnailDisplay(url));
			}
		}

		@Override
		public int getCount() {
			return listItems.size();
		}

		@Override
		public Object getItem(int position) {

			return position;
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

	}

	private static class BitmapDownloaderTask extends
			AsyncTask<String, Object, Bitmap> {
		WeakReference<ImageView> imageViewRef;
		int position;
		WeakReference<RatedActivity> reference;

		public BitmapDownloaderTask(RatedActivity activity,
				WeakReference<ImageView> imageViewRef, int position) {
			super();
			this.imageViewRef = imageViewRef;
			this.position = position;
			reference = new WeakReference<RatedActivity>(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (imageViewRef != null) {
				ImageView imageView = imageViewRef.get();
				if (imageView != null) {
					imageView.setImageBitmap(null);
				}
			}
		}

		@Override
		protected Bitmap doInBackground(String... params) {

			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return null;

			if (params[0].startsWith(ratedActivity.getResources().getString(
					R.string.WSImages))) {
				if (ratedActivity.imagesCache.downloadBitmap(params[0])) {
					return ratedActivity.imagesCache
							.getBitmapFromCache(params[0]);
				} else {
					return null;
				}
			} else {
				return AppUtils.scaleBitmapForThumbnailDisplay(params[0]);
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);

			RatedActivity ratedActivity = reference.get();
			if (ratedActivity == null)
				return;

			if (result != null
					&& ratedActivity.listView.getFirstVisiblePosition() <= position
					&& ratedActivity.listView.getLastVisiblePosition() >= position) {
				{
					if (imageViewRef != null) {
						ImageView imageView = imageViewRef.get();
						if (imageView != null) {
							imageView.setImageBitmap(result);
						}
					}
				}
			}
		}

	}

	class ViewHolder {
		public ImageView im1, im2;
		public RatingView ratingView;
		public TextView tv1, tv2a, tv2b, tv3, tv4;

		public ViewHolder(View v) {
			im1 = (ImageView) v.findViewById(R.id.RatedRowImageView01);
			im2 = (ImageView) v.findViewById(R.id.RatedRowImageView02);

			ratingView = (RatingView) v.findViewById(R.id.RatedRowView01);
			tv1 = (TextView) v.findViewById(R.id.RatedRowTextView02);
			tv2a = (TextView) v.findViewById(R.id.RatedRowTextView03);
			tv2b = (TextView) v.findViewById(R.id.textView2);
			tv3 = (TextView) v.findViewById(R.id.RatedRowTextView05);
			tv4 = (TextView) v.findViewById(R.id.TextView02);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (!listItems.get(arg2).isFlagged) {

			listView.setEnabled(false);
			final int arg2Copy = arg2;
			Animation animation = AppAnimations.pullingDoorOpen(0, listView
					.getChildAt(0).getTop());
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					listView.setVisibility(View.INVISIBLE);
					Intent intent = new Intent(RatedActivity.this,
							RatedDetailsActivity.class);
					AppSession.isMyRates = isMyRates;// set global value
					intent.putExtra("index", arg2Copy);
					intent.putExtra("hasMoreRates", hasMoreRates);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivityForResult(intent, 0);
					overridePendingTransition(0, 0);
				}
			});
			listView.startAnimation(animation);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("isMyRates", isMyRates);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {
			int currentIndex = listView.getFirstVisiblePosition();
			if (data != null) {
				currentIndex = data.getIntExtra("currentIndex",
						listView.getFirstVisiblePosition());
				hasMoreRates = data.getBooleanExtra("hasMoreRates", true);
			}
			int visbleCount = listView.getLastVisiblePosition()
					- listView.getFirstVisiblePosition();
			currentIndex = currentIndex - visbleCount;
			if (currentIndex < 0) {
				currentIndex = 0;
			}
			int top = listView.getChildAt(0).getTop();

			listItems = isMyRates ? AppSession.myRatesArrayList
					: AppSession.ratedArrayList;
			listView.setVisibility(View.VISIBLE);
			listView.setEnabled(true);
			MyListAdapter listAdapter = (MyListAdapter) listView.getAdapter();
			listAdapter.notifyDataSetChanged();

			listView.setSelectionFromTop(currentIndex, top);
			log("bottom = "
					+ listView.getChildAt(listView.getChildCount() - 1)
							.getBottom() + " against " + listView.getHeight());
			if (listView.getChildAt(listView.getChildCount() - 1).getBottom() > (listView
					.getHeight() + 6)) {
				listView.setSelection(currentIndex + 1);
			}
			Animation animation = AppAnimations.pushingDoorClose();

			listView.startAnimation(animation);

		}
	}

	public void initt() {

		listItems = new ArrayList<MyRatesDAO>();

		listView.setDividerHeight(0);
		listView.setVerticalScrollBarEnabled(false);
		listView.startAnimation(AppAnimations.pullingDoorClose());
		listView.setOnItemClickListener(this);

		listView.setEmptyView(findViewById(android.R.id.empty));
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO check the side effects of : listView.getChildCount()>0
				if (!loading
						&& (listView.getChildCount() > 0)
						&& ((firstVisibleItem + visibleItemCount) >= totalItemCount)
						&& hasMoreRates) {
					new LoadListTask(RatedActivity.this, false, false)
							.execute("");
				}

			}
		});
		listView.setAdapter(new MyListAdapter());

		displayNewRate = getIntent().getBooleanExtra("displayNewRate", false)
				|| (AppSession.newRatePosted == AppSession.UPLOAD_IN_PROGRESS);
		log(displayNewRate + "");
		new LoadListTask(this, true, displayNewRate).execute("");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// unbindDrawables(findViewById(R.id.RatedTopLinearLayout01));

		// if upload successful , delete the cached rate
		if (AppSession.newRatePosted == AppSession.NEW && isMyRates) {
			if (AppSession.cachedRateDAO != null) {
				AppSession.cachedRateDAO.getImage1Bitmap().recycle();
				AppSession.cachedRateDAO.getImage2Bitmap().recycle();
				AppSession.cachedRateDAO = null;
			}

			// delete the images on sdcard if exists
			String[] images = new String[] { AppSession.newRateTempImage1Name,
					AppSession.newRateTempImage2Name };
			File file;
			for (String name : images) {
				file = new File(getExternalCacheDir(), name);
				if (file.exists()) {
					file.delete();
				}
			}
		}
		System.gc();
	}

	@SuppressWarnings("unused")
	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			try {
				((ViewGroup) view).removeAllViews();
			} catch (Exception e) {
				log(e.getMessage());
			}
		}
	}

}