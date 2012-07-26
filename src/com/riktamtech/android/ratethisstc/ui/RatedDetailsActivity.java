package com.riktamtech.android.ratethisstc.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.MyRatesDAO;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ImagesCacheSD;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.RatesException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.ui.components.GalleryLauncher;
import com.riktamtech.android.ratethisstc.ui.components.ImageDownloader;
import com.riktamtech.android.ratethisstc.ui.components.RatedDetailsGallery;
import com.riktamtech.android.ratethisstc.ui.components.RatingView;
import com.riktamtech.android.ratethisstc.ui.components.ShareOp;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;
import com.riktamtech.android.ratethisstc.util.FbTwConnector;
import com.riktamtech.android.ratethisstc.util.ImageDownloaderTaskCompletionListener;

/**
 * implements long touch zoom, star and disable flaged q's. also returns current index, also displays remaining time
 * 
 * @author Santosh Kumar D
 */

public class RatedDetailsActivity extends Activity implements OnClickListener, OnItemSelectedListener, ImageDownloaderTaskCompletionListener {
	TitleComponent titleComponent;
	
	private ShareOp shareOp;
	private boolean isMyRates;
	private RatedDetailsGallery gallery;
	private int indexInAllRates;
	private PrefsManager prefsManager;
	private Facebook facebook;
	private ImagesCacheSD imagesCache;

	private boolean backPressedOnce = false;

	private ArrayList<MyRatesDAO> rates1;
	private ArrayList<MyRatesDAO> unflaggedRates;
	private ImageView starImageView;
	private TextView durationTextView;
	private boolean hasMoreRates;
	private AppDialogs dialogs;
	public boolean isLoading = false;
	private OnTouchListener touchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			globalDetector.onTouchEvent(arg1);
			// we dont want to update star for myrates 
			if (!isMyRates) {
				if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
					starImageView.setVisibility(View.INVISIBLE);
				} else if (arg1.getAction() == MotionEvent.ACTION_CANCEL || arg1.getAction() == MotionEvent.ACTION_UP) {
					gallery.postDelayed(new Runnable() {

						@Override
						public void run() {
							starImageView.setVisibility(View.VISIBLE);
						}
					}, 750);
				}

			}
			return false;
		}

	};

	private GestureDetector globalDetector = new GestureDetector(new GlobalGestureListener());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.rated_details_new);

		dialogs = new AppDialogs(this);
		starImageView = (ImageView) findViewById(R.id.starImageView);
		durationTextView = (TextView) findViewById(R.id.durationTextView);

		if (savedInstanceState != null) {
			hasMoreRates = savedInstanceState.getBoolean("hasMoreRates");
		} else {
			hasMoreRates = getIntent().getBooleanExtra("hasMoreRates", true);
		}

		isMyRates = AppSession.isMyRates;
		lg("app session is myrates is " + isMyRates);

		gallery = (com.riktamtech.android.ratethisstc.ui.components.RatedDetailsGallery) findViewById(R.id.Gallery01);

		gallery.setOnTouchListener(touchListener);

		imagesCache = new ImagesCacheSD();

		if (isMyRates) {
			titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "MyRates!  ", R.drawable.back_btn, gallery);
			rates1 = AppSession.myRatesArrayList;
			lg("is my rates - rates1 " + rates1);

		} else {
			// TODO not gone
			findViewById(R.id.ShareOp).setVisibility(View.GONE);
			titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "Rated! ", R.drawable.back_btn, gallery);
			rates1 = AppSession.ratedArrayList;
			lg("is rated - rates1 " + rates1);
		}

		unflaggedRates = AppUtils.removeFlaggedRates(rates1);
		if (savedInstanceState != null) {
			indexInAllRates = savedInstanceState.getInt("index");
		} else {
			indexInAllRates = getIntent().getIntExtra("index", 0);
		}
		int indexInUnflaggedRates = unflaggedRates.indexOf(rates1.get(indexInAllRates));

		{

			shareOp = new ShareOp(findViewById(R.id.ShareOp), R.drawable.sharethisrate_up, R.drawable.sharethisrate_down);
			shareOp.tv.setOnClickListener(this);
			shareOp.facebookIcon.setOnClickListener(this);
			shareOp.twitterIcon.setOnClickListener(this);
			shareOp.mailIcon.setOnClickListener(this);

			prefsManager = new PrefsManager(this);

			facebook = new Facebook(getResources().getString(R.string.FBAppId));

			if (prefsManager.getFbAccessToken() != null) {
				facebook.setAccessToken(prefsManager.getFbAccessToken());
				facebook.setAccessExpiresIn(prefsManager.getFbExpiry());
			}

			titleComponent.imageClickListener = new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					starImageView.setVisibility(View.INVISIBLE);
					shareOp.view.setVisibility(View.INVISIBLE);
					durationTextView.setVisibility(View.INVISIBLE);

					Intent data = new Intent();
					int index =0;
					try {
					index=rates1.indexOf(unflaggedRates.get(gallery.getSelectedItemPosition()));
					}catch (Exception e) {
					}
					data.putExtra("currentIndex", index);
					data.putExtra("hasMoreRates", hasMoreRates);
					setResult(RESULT_OK, data);
				}
			};
		}
		if (isMyRates) {
			starImageView.setVisibility(View.INVISIBLE);
		}

		gallery.startAnimation(AppAnimations.pullingDoorClose());
		if (!ServiceConnector.testConnection(getApplication())) {
			dialogs.getAlertDialog(getResources().getString(R.string.ALRT_NOT_CONNECTED), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).show();
		} else {

			gallery.setAdapter(new MyAdap());
			gallery.setSelection(indexInUnflaggedRates);
			gallery.setOnItemSelectedListener(this);
			//gallery.setCallbackDuringFling(false);

		}
	}

	public void onBackPressed() {
		if (!backPressedOnce) {
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	@Override
	public void onClick(View v) {
		if (v == shareOp.tv) {
			shareOp.toggleView();
		} else if (v == shareOp.facebookIcon) {
			Toast.makeText(this, "posting to facebook", 0).show();
			shareOp.toggleView();
			if (facebook.isSessionValid()) {
				fbPost();
			} else {
				facebook.authorize(this, new String[] { "publish_stream", "read_stream", "offline_access" }, new DialogListener() {

					@Override
					public void onFacebookError(FacebookError e) {
					}

					@Override
					public void onError(DialogError e) {
					}

					@Override
					public void onComplete(Bundle values) {
						String ACCESS_TOKEN = values.getString("access_token");
						String EXP_TIME = values.getString("expires_in");

						prefsManager.setFacebookCredentials(ACCESS_TOKEN, EXP_TIME);

						fbPost();

					}

					@Override
					public void onCancel() {
					}
				});
			}
		} else if (v == shareOp.twitterIcon) {
			shareOp.toggleView();
			Toast.makeText(this, "posting to twitter", 0).show();
			if (prefsManager.getTwUserName() != null) {
				tweet();
			} else {
				startActivityForResult(new Intent(this, TwitterLoginActivity.class), 31);
				overridePendingTransition(R.anim.ib, 0);
			}
		} else if (v == shareOp.mailIcon) {
			shareOp.toggleView();
			String imageLink = getResources().getString(R.string.WSImages) + unflaggedRates.get(gallery.getSelectedItemPosition()).qid + "_result.jpg";
			new ImageDownloader(this, this).execute(imageLink, "ratethis.jpg");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		log("activity being destroyed");
		outState.putBoolean("hasMoreRates", hasMoreRates);
		outState.putInt("index", gallery.getSelectedItemPosition());

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	public void lg(String ss) {
		if (ss.contains("no more")) {
			Log.e("RatedDetailsAct", ss);
		} else {
			Log.d("RatedDetailsAct", ss);
		}
	}

	class MyAdap extends BaseAdapter {

		@Override
		public int getCount() {
			return unflaggedRates.size() + 1;
		}

		@Override
		public Object getItem(int arg0) {
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				LayoutInflater layoutInflater = getLayoutInflater();
				view = layoutInflater.inflate(R.layout.rated_details_comp, null);
				Gallery.LayoutParams layoutParams = new Gallery.LayoutParams(AppSession.DEVICE_SCREEN_WIDTH, LayoutParams.MATCH_PARENT);
				view.setLayoutParams(layoutParams);
				AppUtils.resizeBoxImageViews(3.5, view.findViewById(R.id.RatedDetailsImageView01), view.findViewById(R.id.RatedDetailsImageView02));
				ViewHolder viewHolder = new ViewHolder(view);
				view.setTag(viewHolder);
				new CustomFontizer().fontize((ViewGroup) view, R.id.RatedDetailsTextView01, R.id.RatedDetailsTextView02, R.id.RatedDetailsTextView03, R.id.RatedDetailsTextView04,
						R.id.TextView03, R.id.TextView02, R.id.textView1, R.id.textView2, R.id.TextView01);
			}
			convertView = initChild(view, position);
			return convertView;
		}

	}

	public View initChild(View v, int i) {
		if (i < unflaggedRates.size()) {
			ViewHolder h = (ViewHolder) v.getTag();
			try {
				MyRatesDAO currentMyRatesRowDAO = unflaggedRates.get(i);
				h.tv1.setText(" : " + currentMyRatesRowDAO.primarytag + "");
				String secondarytagA = currentMyRatesRowDAO.secondarytag_A, secondarytagB = currentMyRatesRowDAO.secondarytag_B;
				if (secondarytagA.length() > 15)
					secondarytagA = secondarytagA.substring(0, 15) + "...";
				if (secondarytagB.length() > 15)
					secondarytagB = secondarytagB.substring(0, 15) + "...";

				h.tv2a.setText(" : " + secondarytagA);
				h.tv2b.setText(" : " + secondarytagB);

				h.tv3.setText(" : " + currentMyRatesRowDAO.date_posted + "");
				String ratesText = (currentMyRatesRowDAO.votes == 1) ? getResources().getString(R.string.RatedRate) : getResources().getString(R.string.RatedRates);
				h.tv4.setText(currentMyRatesRowDAO.votes + " " + ratesText);

				if (!currentMyRatesRowDAO.isCompleted) {
					h.imv1.setBackgroundResource(R.drawable.orange_border_big);
					h.imv2.setBackgroundResource(R.drawable.orange_border_big);
					h.ratingView.initt(currentMyRatesRowDAO.getPercent(), false, false, false);
				} else {
					float percent = currentMyRatesRowDAO.getPercent();
					h.ratingView.initt(percent, false, true, false);
					if (percent > 0.5f) {
						h.imv1.setBackgroundResource(R.drawable.green_border_big);
						h.imv2.setBackgroundResource(R.drawable.red_border_big);

					} else if (percent < 0.5f) {
						h.imv1.setBackgroundResource(R.drawable.red_border_big);
						h.imv2.setBackgroundResource(R.drawable.green_border_big);
					} else {
						h.imv1.setBackgroundResource(R.drawable.blue_border_big);
						h.imv2.setBackgroundResource(R.drawable.blue_border_big);
					}
				}
				String WS_SERVER_IMAGES_DIRECTORY = getResources().getString(R.string.WSImages);
				if (currentMyRatesRowDAO.isLocalRate) {
					setImage(h.imv1, currentMyRatesRowDAO.localImage1);
					setImage(h.imv2, currentMyRatesRowDAO.localImage2);

					//new ImageLoaderTask(h.imv1).execute(currentMyRatesRowDAO.localImage1);
					//new ImageLoaderTask(h.imv2).execute(currentMyRatesRowDAO.localImage2);

				} else {
					if (currentMyRatesRowDAO.qidString != null) {
						//new ImageLoaderTask(h.imv1).execute(WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qidString + "_2_1.jpg");
						//new ImageLoaderTask(h.imv2).execute(WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qidString + "_2_2.jpg");
						setImage(h.imv1, WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qidString + "_1_1.jpg");
						setImage(h.imv2, WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qidString + "_1_2.jpg");

					} else {
						//new ImageLoaderTask(h.imv1).execute(WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qid + "_1_1.jpg");
						//new ImageLoaderTask(h.imv2).execute(WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qid + "_1_2.jpg");
						setImage(h.imv1, WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qid + "_1_1.jpg");
						setImage(h.imv2, WS_SERVER_IMAGES_DIRECTORY + currentMyRatesRowDAO.qid + "_1_2.jpg");

					}
				}
				return v;
			} catch (Exception e) {
				lg("pos :" + i + ", data " + unflaggedRates.get(i));
				e.printStackTrace();
			}
		} else {
			lg(" rates " + unflaggedRates);
			lg("postion = " + i);
			return new TextView(this);
		}
		return new TextView(this);
	}

	void setImage(ImageView imageView, String url) {
		if (url.startsWith(getResources().getString(R.string.WSImages))) {
			if (imagesCache.containsImage(url))
				imageView.setImageBitmap(imagesCache.getBitmapFromCache(url));
			else {
				WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(imageView);
				new ImageLoaderTask(this,imageViewReference).execute(url);
			}
		} else {
			imageView.setImageBitmap(BitmapFactory.decodeFile(url));
		}
	}

	private static class ImageLoaderTask extends AsyncTask<String, Object, Bitmap> {
		WeakReference<ImageView> imageViewRef;
		Context ctx;

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

		public ImageLoaderTask(Context ctx,WeakReference<ImageView> imageViewRef) {
			super();
			this.imageViewRef = imageViewRef;
			this.ctx = ctx;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			if (params[0].startsWith(ctx.getResources().getString(R.string.WSImages))) {
				ImagesCacheSD imagesCache = new ImagesCacheSD();
				if (imagesCache.downloadBitmap(params[0])) {
					return imagesCache.getBitmapFromCache(params[0]);
				} else {
					return null;
				}
			} else {
				return BitmapFactory.decodeFile(params[0]);
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			if (imageViewRef != null) {
				ImageView imageView = imageViewRef.get();
				if (imageView != null) {
					imageView.setImageBitmap(result);
				}
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		gallery.onTouchEvent(ev);
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		lg("item selected " + arg2 + " - " + unflaggedRates.get(arg2));
		MyRatesDAO myRatesRowDAO = unflaggedRates.get(arg2);
		// TODO handle this
		if (myRatesRowDAO.isCompleted) {
			shareOp.view.setVisibility(View.VISIBLE);
			durationTextView.setVisibility(View.INVISIBLE);
		} else {
			shareOp.view.setVisibility(View.INVISIBLE);
			durationTextView.setText(myRatesRowDAO.getDuration());
			durationTextView.setVisibility(View.VISIBLE);

		}
		refreshStar();
		if (arg2 == unflaggedRates.size() - 2 && hasMoreRates) {
			new LoadRatesTask(this).execute("");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		refreshStar();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("~~~~~~~~~~~~  RAD - ACT RES", " " + requestCode + "  " + resultCode + " " + data);
		facebook.authorizeCallback(requestCode, resultCode, data);

		if (requestCode == 31) {
			if (resultCode == RESULT_OK) {
				prefsManager = new PrefsManager(this);
				tweet();
			}

		}
	}

	private void tweet() {

		new TwitterTask(this).execute("-t");
	}

	private static class TwitterTask extends AsyncTask<String, String, String> {
		WeakReference<RatedDetailsActivity> reference;

		public TwitterTask(RatedDetailsActivity activity) {
			reference=new WeakReference<RatedDetailsActivity>(activity);
		}

		@Override
		protected String doInBackground(String... params) {
			RatedDetailsActivity activity=reference.get();
			if(activity==null) return null;
			String msg = "";
			MyRatesDAO currentDao = activity.unflaggedRates.get(activity.gallery.getSelectedItemPosition());
			int qid = currentDao.qid;
			String primaryTag = currentDao.primarytag;
			String imageLink = activity.getResources().getString(R.string.WSImages) + qid + "_result.jpg";
			if (currentDao.qidString != null) {
				imageLink = activity.getResources().getString(R.string.WSImages) + currentDao.qidString + "_result.jpg";
			}

			String content = "";
			if (activity.isMyRates)
				content = activity.getResources().getString(R.string.FB_TW_MSG_CONTENT_RATES, AppSession.signedInUser.user_nameString);
			else
				content = activity.getResources().getString(R.string.FB_TW_MSG_CONTENT_RATED, AppSession.signedInUser.user_nameString, primaryTag);

			if (params[0].equals("-t")) {

				String res = new FbTwConnector(activity).tweet(activity.prefsManager.getTwUserName(), activity.prefsManager.getTwPassword(), imageLink, content);
				if (res.equals("ok")) {
					msg = activity.getResources().getString(R.string.TST_TWT_SUCCESS);
				} else if (res.equals("invalid details")) {
					msg = activity.getResources().getString(R.string.TST_TWT_INVALID_DETAILS);
					activity.startActivityForResult(new Intent(activity, TwitterLoginActivity.class), 31);
					activity.overridePendingTransition(R.anim.ib, 0);
				} else if (res.equals("failed")) {
					msg = activity.getResources().getString(R.string.TST_TWT_FAILED_UPLOAD);
					activity.startActivityForResult(new Intent(activity, TwitterLoginActivity.class), 31);
					activity.overridePendingTransition(R.anim.ib, 0);
				}
			} else if (params[0].equals("-f")) {
				String res = new FbTwConnector(activity).postToFacebookWall(activity.facebook, imageLink, content, null);
				if (res.equals("ok")) {
					msg = activity.getResources().getString(R.string.TST_FB_SUCCESS);
				} else {
					msg = res;

				}
			}

			return msg;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			RatedDetailsActivity activity=reference.get();
			if(activity==null) return;
			Toast.makeText(activity, result, 0).show();
		}
	}

	private void fbPost() {
		new TwitterTask(this).execute("-f");
	}

	class ViewHolder implements OnClickListener, OnTouchListener {
		ImageView imv1, imv2;
		TextView tv1, tv2a, tv2b, tv3, tv4;
		RatingView ratingView;

		public ViewHolder(View v) {
			// TODO edited this 
			tv1 = (TextView) v.findViewById(R.id.RatedDetailsTextView02);
			tv2a = (TextView) v.findViewById(R.id.TextView03);
			tv2b = (TextView) v.findViewById(R.id.textView2);
			tv3 = (TextView) v.findViewById(R.id.RatedDetailsTextView04);
			tv4 = (TextView) v.findViewById(R.id.TextView01);
			imv1 = (ImageView) v.findViewById(R.id.RatedDetailsImageView01);
			imv2 = (ImageView) v.findViewById(R.id.RatedDetailsImageView02);
			ratingView = (RatingView) v.findViewById(R.id.RatedDetailsView01);
		}

		@Override
		public void onClick(View v) {

		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (v == imv1)

			{
				boolean flag1 = gestureDetector1.onTouchEvent(event);
				if (!flag1)
					return gallery.onTouchEvent(event);
				return true;
			} else if (v == imv2) {
				if (!gestureDetector2.onTouchEvent(event))
					return gallery.onTouchEvent(event);
				return true;
			} else {
				return false;
			}

		}

		GestureDetector gestureDetector1, gestureDetector2;
	}

	@Override
	public void onImageDownloadComplete(File f) {
		String primaryTag = unflaggedRates.get(gallery.getSelectedItemPosition()).primarytag;
		String username = AppSession.signedInUser.user_nameString;
		String msg = (isMyRates) ? username
				+ "\'s "
				+ primaryTag
				+ "\'s Rate has finished. Check out the results below.<p>Join the conversation. RateThis! is available for <a href=\"http://itunes.apple.com/us/app/ratethis!/id446379782?mt=8\">Apple</a> and <a href=\"http://cookiejarsolutions.blogspot.com\">Android</a>.</p>"
				: username
						+ " has rated on  "
						+ primaryTag
						+ "\'s Rate. Check out the results below.<p>Join the conversation. RateThis! is available for <a href=\"http://itunes.apple.com/us/app/ratethis!/id446379782?mt=8\">Apple</a> and <a href=\"http://cookiejarsolutions.blogspot.com\">Android</a>.</p>";
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, "RateThis! Results of " + primaryTag + " Rate");
		i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(msg));
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
		i.setType("text/html");
		startActivity(Intent.createChooser(i, "Send mail"));
	}

	@SuppressWarnings("unused")
	private class LoadRatesTask1 extends AsyncTask<Object, Exception, Boolean> {
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			lg("Loading list questions,  showing progress = " + progressDialog);
			isLoading = true;
			progressDialog = ProgressDialog.show(RatedDetailsActivity.this, null, getResources().getString(R.string.PRG_TITLE) + "...");
		}

		@Override
		protected Boolean doInBackground(Object... params) {

			int size = unflaggedRates.size(), qid = 0;
			if (size > 0) {
				qid = isMyRates ? unflaggedRates.get(size - 4).qid : unflaggedRates.get(size - 3).rateId;
			}

			try {

				ArrayList<MyRatesDAO> itemsReturnesd = ServiceConnector.myRatesRequest(getApplication(), qid, isMyRates);
				int nHelpRates = (isMyRates) ? 4 : 3;
				rates1.addAll(rates1.size() - nHelpRates, itemsReturnesd);
				unflaggedRates = AppUtils.removeFlaggedRates(rates1);
				return true;
			} catch (Exception e) {
				publishProgress(e);
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			Exception exception = values[0];
			if (exception instanceof WebServiceException) {
				exception.printStackTrace();
				lg(exception.getMessage());
				dialogs.getAlertDialog(getResources().getString(R.string.ALRT_NOT_CONNECTED));
			} else if (exception instanceof RatesException) {
				exception.printStackTrace();
				lg(exception.getMessage());
				hasMoreRates = false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				int pos = gallery.getSelectedItemPosition();
				((MyAdap) gallery.getAdapter()).notifyDataSetChanged();
				gallery.setSelection(pos);
				if (isMyRates)
					AppSession.myRatesArrayList = rates1;
				else
					AppSession.ratedArrayList = rates1;
			}
			isLoading = false;
			progressDialog.dismiss();
			lg("loading over , size of list is " + rates1.size());
		}
	}

	void loadRatesTaskPreExecute() {

	}

	void loadRatesTaskBg() {

	}

	void loadRatesTaskPostExecute() {

	}

	void loadRatesTaskProgressUpdate() {

	}

	private static class LoadRatesTask extends AsyncTask<Object, Exception, Boolean> {
		ProgressDialog progressDialog;
		WeakReference<RatedDetailsActivity> reference;

		public LoadRatesTask(RatedDetailsActivity activity) {
			reference = new WeakReference<RatedDetailsActivity>(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			RatedDetailsActivity activity = reference.get();
			if (activity == null)
				return;
			else
				;

			activity.lg("Loading list questions,  showing progress = " + progressDialog);
			activity.isLoading = true;
			progressDialog = ProgressDialog.show(activity, null, activity.getResources().getString(R.string.PRG_TITLE) + "...");
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			RatedDetailsActivity activity = reference.get();
			if (activity == null)
				return false;
			else
				;
			int size = activity.unflaggedRates.size(), qid = 0;
			if (size > 0) {
				qid = activity.isMyRates ? activity.unflaggedRates.get(size - 1).qid : activity.unflaggedRates.get(size - 1).rateId;
			}

			try {

				ArrayList<MyRatesDAO> itemsReturned = ServiceConnector.myRatesRequest(activity.getApplication(), qid, activity.isMyRates);
				activity.rates1.addAll(itemsReturned);
				activity.unflaggedRates.addAll(AppUtils.removeFlaggedRates(itemsReturned));
				return true;
			} catch (Exception e) {
				publishProgress(e);
				return false;
			}
		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			RatedDetailsActivity activity = reference.get();
			if (activity == null)
				return;
			else
				;

			super.onProgressUpdate(values);
			Exception exception = values[0];
			if (exception instanceof WebServiceException) {
				exception.printStackTrace();
				activity.lg(exception.getMessage());
				activity.dialogs.getAlertDialog(activity.getResources().getString(R.string.ALRT_NOT_CONNECTED));
			} else if (exception instanceof RatesException) {
				exception.printStackTrace();
				activity.lg(exception.getMessage());
				activity.hasMoreRates = false;
				ArrayList<MyRatesDAO> helpfulRatesDAOs = ServiceConnector.getHelpfulRates(activity.isMyRates);
				activity.rates1.addAll(helpfulRatesDAOs);
				activity.unflaggedRates.addAll(AppUtils.removeFlaggedRates(helpfulRatesDAOs));
				int pos = activity.gallery.getSelectedItemPosition();
				((MyAdap) activity.gallery.getAdapter()).notifyDataSetChanged();
				activity.gallery.setSelection(pos);
				if (activity.isMyRates)
					AppSession.myRatesArrayList = activity.rates1;
				else
					AppSession.ratedArrayList = activity.rates1;

			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			RatedDetailsActivity activity = reference.get();
			if (activity == null)
				return;
			else
				;

			if (result) {
				int pos = activity.gallery.getSelectedItemPosition();
				((MyAdap) activity.gallery.getAdapter()).notifyDataSetChanged();
				activity.gallery.setSelection(pos);
				if (activity.isMyRates)
					AppSession.myRatesArrayList = activity.rates1;
				else
					AppSession.ratedArrayList = activity.rates1;
			}
			activity.isLoading = false;
			progressDialog.dismiss();
			activity.lg("loading over , size of list is " + activity.rates1.size());
		}
	}

	private void refreshStar() {

		if (!isMyRates) {
			int top = AppSession.DEVICE_SCREEN_HEIGHT / 4, bottom = AppSession.DEVICE_SCREEN_HEIGHT / 6;
			FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) starImageView.getLayoutParams();
			if (unflaggedRates.get(gallery.getSelectedItemPosition()).answer == 1) {
				layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
				layoutParams.setMargins(0, top, 0, 0);
			} else {
				layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
				layoutParams.setMargins(0, 0, 0, bottom);
			}

		}
	}

	private void log(String string) {
		Log.d("RDAN", string);
	}

	class GlobalGestureListener extends SimpleOnGestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			int image = -1;
			Rect rect1 = new Rect(), rect2 = new Rect();
			View imageView1 = ((ViewGroup) gallery.getSelectedView()).findViewById(R.id.RatedDetailsImageView01), imageView2 = ((ViewGroup) gallery.getSelectedView())
					.findViewById(R.id.RatedDetailsImageView02);
			imageView1.getHitRect(rect1);
			imageView2.getHitRect(rect2);

			if (rect1.contains((int) e.getX(), (int) e.getY()))
				image = 1;
			else if (rect2.contains((int) e.getX(), (int) e.getY()))
				image = 2;
			if (image > 0) {

				MyRatesDAO currentRate = unflaggedRates.get(gallery.getSelectedItemPosition());
				if (currentRate.isLocalRate) {
					if (image == 1) {
						new GalleryLauncher(RatedDetailsActivity.this).execute(getExternalCacheDir().getAbsolutePath() + "/" + AppSession.newRateTempImage1Name);
					} else {
						new GalleryLauncher(RatedDetailsActivity.this).execute(getExternalCacheDir().getAbsolutePath() + "/" + AppSession.newRateTempImage2Name);
					}
				} else {
					if (currentRate.qidString != null) {
						new GalleryLauncher(RatedDetailsActivity.this).execute(getResources().getString(R.string.WSImages) + currentRate.qidString + "_0_" + image + ".jpg");
					} else {
						new GalleryLauncher(RatedDetailsActivity.this).execute(getResources().getString(R.string.WSImages)
								+ unflaggedRates.get(gallery.getSelectedItemPosition()).qid + "_0_" + image + ".jpg");
					}
				}
			}

		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
