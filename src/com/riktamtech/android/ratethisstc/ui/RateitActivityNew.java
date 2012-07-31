package com.riktamtech.android.ratethisstc.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.RateItRate;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ImagesCacheSD;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.RateItException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.ui.components.GalleryLauncher;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class RateitActivityNew extends Activity implements OnClickListener,
		OnPageChangeListener {
	public static final String TAG = "RateitActivityNew";
	private TitleComponent titleComponent;
	private ImageView flagButton;
	private ViewPager pager;
	private RelativeLayout containerLayout;
	private LinkedList<RateItRate> items1;
	private ImagesCacheSD imagesCache;
	private GestureDetector gestureDetector;
	private AppDialogs dialogs;
	private boolean backPressedOnce = false;
	private boolean isRatesOver = false;
	private boolean respondToTouchEvents = true;
	private RateItPagerAdapter rateItPagerAdapter;
	private boolean answeringInProgress = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.rate_it);

		containerLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp),
				"RateIt!  ", containerLayout);
		dialogs = new AppDialogs(this);
		if (!ServiceConnector.testConnection(this))
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_NOT_CONNECTED,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									finish();
								}

							})).show();
		else
			initt();
	}

	@Override
	protected void onStart() {
		AppSession.listenToLocationChanges(getApplication());
		super.onStart();
	}

	@Override
	protected void onStop() {
		AppSession.stopListeningToLocationUpdates();
		super.onStop();
	}

	private static class RateItTask extends AsyncTask<Object, Object, Object> {
		ProgressDialog progressDialog;
		WeakReference<RateitActivityNew> rateitactWeakReference;
		LinkedList<RateItRate> newQuestions = new LinkedList<RateItRate>();;
		boolean showProgressDialog;

		RateItTask(RateitActivityNew rateitActivityNew,
				boolean showProgressDialog) {
			rateitactWeakReference = new WeakReference<RateitActivityNew>(
					rateitActivityNew);
			this.showProgressDialog = showProgressDialog;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			RateitActivityNew rateitActivityNew = rateitactWeakReference.get();
			if (rateitActivityNew != null && showProgressDialog)
				progressDialog = ProgressDialog.show(
						rateitActivityNew,
						null,
						rateitActivityNew.getResources().getString(
								R.string.PRG_LOADING));
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			RateitActivityNew rateitActivityNew = rateitactWeakReference.get();
			if (rateitActivityNew != null)
				newQuestions = rateitActivityNew.rateItRequest();
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			RateitActivityNew rateitActivityNew = rateitactWeakReference.get();
			if (rateitActivityNew != null) {
				if (newQuestions != null && newQuestions.size() != 0) {
					// firsttime user and loading rates first time
					if (AppSession.signedInUser.showDemo && showProgressDialog)
						newQuestions.addAll(0, ServiceConnector
								.getRateItHelpfulRates(rateitActivityNew
										.getApplication()));
					rateitActivityNew.refreshItems(newQuestions);
				} else {
					// show dialog if its the first time
					if (showProgressDialog)
						rateitActivityNew.showNoMoreQuestionsDialog();
				}

			}
			if (showProgressDialog && progressDialog != null) {
				progressDialog.dismiss();
			}
		}

	}

	private static class RateItBackgroundTask extends
			AsyncTask<Object, Object, Object> {
		WeakReference<RateitActivityNew> rateitactWeakReference;
		LinkedList<RateItRate> newQuestions = new LinkedList<RateItRate>();;

		RateItBackgroundTask(RateitActivityNew rateitActivityNew) {
			rateitactWeakReference = new WeakReference<RateitActivityNew>(
					rateitActivityNew);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Object doInBackground(Object... arg0) {
			RateitActivityNew rateitActivityNew = rateitactWeakReference.get();
			if (rateitActivityNew != null)
				for (;;) {
					if (!rateitActivityNew.answeringInProgress) {
						newQuestions = rateitActivityNew.rateItRequest();
						break;
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			RateitActivityNew rateitActivityNew = rateitactWeakReference.get();
			if (rateitActivityNew != null) {
				if (newQuestions != null && newQuestions.size() != 0) {
					rateitActivityNew.refreshItems(newQuestions);
				}
			}

		}
	}

	private void initt() {
		flagButton = (ImageView) findViewById(R.id.ImageView01);
		flagButton.setOnClickListener(this);
		items1 = new LinkedList<RateItRate>();
		containerLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
		containerLayout.startAnimation(AppAnimations.pullingDoorClose());
		pager = (ViewPager) findViewById(R.id.rateItPager);
		rateItPagerAdapter = new RateItPagerAdapter();
		pager.setAdapter(rateItPagerAdapter);
		pager.setOnPageChangeListener(this);
		gestureDetector = new GestureDetector(new MyTouchListener());
		pager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (respondToTouchEvents)
					return gestureDetector.onTouchEvent(arg1);
				return true;
			}
		});

		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp),
				"RateIt!  ", containerLayout);
		titleComponent.imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				titleComponent.imageView.setEnabled(false);

				if (getRateAt(pager.getCurrentItem() - 1) != null)
					new AnswerRateTask(RateitActivityNew.this, false)
							.execute(pager.getCurrentItem() - 1);

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
						titleComponent.toAnimateView
								.setVisibility(View.INVISIBLE);
						finish();
						overridePendingTransition(0, 0);
					}
				});
				titleComponent.toAnimateView.startAnimation(animation);
			}
		});
		imagesCache = new ImagesCacheSD();
		new RateItTask(this, true).execute(1);

	}

	private LinkedList<RateItRate> rateItRequest() {
		LinkedList<RateItRate> newQuestions = new LinkedList<RateItRate>();
		if (!isRatesOver) {
			try {
				ArrayList<RateItRate> rateitRequestResult = ServiceConnector
						.rateitRequest(getApplication(),
								getQuestionIdsAsStirng());
				newQuestions.addAll(rateitRequestResult);
				lg("After rateit request newQs = " + newQuestions);
				return newQuestions;
			} catch (WebServiceException e) {
				e.printStackTrace();
			} catch (RateItException e) {
				e.printStackTrace();
				isRatesOver = true;
			}
		}
		return null;
	}

	private void refreshItems(LinkedList<RateItRate> newQuestions) {
		boolean isFirst = false;
		LinkedList<RateItRate> arrayList = new LinkedList<RateItRate>();
		if (items1.size() == 0) {
			isFirst = true;
		} else if (items1.size() == 1) {
			arrayList.add(items1.get(0));

		} else if (items1.size() > 1) {

			for (int i = pager.getCurrentItem() - 1; i < items1.size(); i++) {
				arrayList.add(items1.get(i));
			}
		} else
			;

		arrayList.addAll(newQuestions);

		// for(RateItRate rate: newQuestions)
		// if(!arrayList.contains(rate))
		// arrayList.add(rate);
		//
		items1 = arrayList;
		rateItPagerAdapter.notifyDataSetChanged();

		if (!isFirst) {
			pager.setCurrentItem(1, false);
		}

	}

	@Override
	public void onClick(View v) {

		if (v == flagButton && respondToTouchEvents) {
			RateItRate currentRate = items1.get(pager.getCurrentItem());
			currentRate.userAnswer = 3;
			if (getRateAt(pager.getCurrentItem() - 1) != null)
				new AnswerRateTask(RateitActivityNew.this, true).execute(pager
						.getCurrentItem() - 1);
			else

				pager.setCurrentItem(pager.getCurrentItem() + 1, true);
		}
	}

	private class RateItPagerAdapter extends PagerAdapter {
		public View currentView;

		@Override
		public int getItemPosition(Object object) {
			// int position = (Integer) ((View) object).getTag();
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return (items1.size() + 1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			Log.d(TAG, "setPrimaryItem: " + position);
			currentView = (View) object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			try {

				RateItRate rate = items1.get(position);

				if (position == items1.size() || rate.qid.equals("-1")) {
					TextView textView = new TextView(RateitActivityNew.this);
					textView.setText("");
					if (items1.size() == 0) {
						flagButton.setVisibility(View.INVISIBLE);
					}
					textView.setTag(position);
					return textView;
				} else {
					View v = getLayoutInflater().inflate(R.layout.rateit_sc,
							null);
					// Gallery.LayoutParams layoutParams = new
					// Gallery.LayoutParams(
					// AppSession.DEVICE_SCREEN_WIDTH,
					// LayoutParams.FILL_PARENT);
					// v.setLayoutParams(layoutParams);
					ViewHolder holder = new ViewHolder(v, position);
					v.setTag(holder);
					AppUtils.resizeBoxImageViews(3.0, holder.im1, holder.im2);
					initView(v, position);
					container.addView(v);
					return v;
				}

			} catch (Exception e) {
				lg("Exception   :   " + e.getMessage() + " position="
						+ position + "items=" + items1);
				TextView textView = new TextView(RateitActivityNew.this);
				textView.setWidth(AppSession.DEVICE_SCREEN_WIDTH - 20);
				textView.setTag(position);
				return textView;
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	private void showNoMoreQuestionsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.ALRT_DIALOG_TITLE))
				.setMessage(
						getResources().getString(R.string.ALRT_NO_MORE_RATES))
				.setPositiveButton(R.string.ALRT_BUTTON_OK,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								titleComponent.imageView.performClick();
							}
						});

		AlertDialog dialog = builder.create();
		dialog.show();
		flagButton.setVisibility(View.INVISIBLE);
	}

	private void initView(View v, int position) {
		RateItRate currentRate = items1.get(position);
		ViewHolder holder = (ViewHolder) v.getTag();
		setImage(holder.im1, currentRate.getThumbnailImageLink1());
		setImage(holder.im2, currentRate.getThumbnailImageLink2());

		// new
		// ImageLoaderTask(holder.im1).execute(currentRate.getThumbnailImageLink1());
		// new
		// ImageLoaderTask(holder.im2).execute(currentRate.getThumbnailImageLink2());
		holder.tv.setText(AppSession.primaryTagsArrayList
				.get(currentRate.primarytagId));

	}

	private static class ViewHolder {
		ImageView im1, im2;
		TextView tv;
		int position;

		public ViewHolder(View v, int position) {
			im1 = (ImageView) v.findViewById(R.id.RateitSCImageView01);
			im2 = (ImageView) v.findViewById(R.id.RateitSCImageView03);
			tv = (TextView) v.findViewById(R.id.textView1);
			tv.setClickable(false);
			this.position = position;
			new CustomFontizer().fontize((ViewGroup) v, R.id.textView1);
		}

	}

	private void setImage(ImageView imageView, String url) {
		if (url.startsWith(getResources().getString(R.string.WSImages))) {
			if (imagesCache.containsImage(url))
				imageView.setImageBitmap(imagesCache.getBitmapFromCache(url));
			else {
				WeakReference<ImageView> imageViewReference = new WeakReference<ImageView>(
						imageView);
				new ImageLoaderTask(this, imageViewReference).execute(url);
			}
		} else {
			imageView.setImageBitmap(BitmapFactory.decodeFile(url));
		}
	}

	private class MyTouchListener extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			super.onSingleTapUp(e);
			int id = getIdOfComponentClicked(e);
			if (id == 1 || id == 2) {

				ImageView imv1 = (ImageView) rateItPagerAdapter.currentView
						.findViewById(R.id.RateitSCImageView01), imv2 = (ImageView) rateItPagerAdapter.currentView
						.findViewById(R.id.RateitSCImageView03);
				if (id == 1) {
					imv1.setBackgroundResource(R.drawable.green_border_big);
					imv2.setBackgroundResource(R.drawable.button_border_big);
				} else if (id == 2) {
					imv1.setBackgroundResource(R.drawable.button_border_big);
					imv2.setBackgroundResource(R.drawable.green_border_big);
				} else
					;
				RateItRate currentRate = items1.get(pager.getCurrentItem());
				currentRate.userAnswer = id;
				if (getRateAt(pager.getCurrentItem() - 1) != null)
					new AnswerRateTask(RateitActivityNew.this, true)
							.execute(pager.getCurrentItem() - 1);
				else {
					pager.setCurrentItem(pager.getCurrentItem() + 1, true);
				}
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			resetImageBorders(pager.getCurrentItem());
			return super.onScroll(e1, e2, distanceX, distanceY);

		}

		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			int id = getIdOfComponentClicked(e);
			if (id != -1) {
				String imageLink = "";
				ImageView imv1 = (ImageView) rateItPagerAdapter.currentView
						.findViewById(R.id.RateitSCImageView01), imv2 = (ImageView) rateItPagerAdapter.currentView
						.findViewById(R.id.RateitSCImageView03);
				if (id == 1) {
					imv1.setBackgroundResource(R.drawable.button_border);
					imageLink = items1.get(pager.getCurrentItem())
							.getFullImageLink1();
				} else if (id == 2) {
					imv2.setBackgroundResource(R.drawable.button_border);
					imageLink = items1.get(pager.getCurrentItem())
							.getFullImageLink2();
				}

				else
					;
				if (!imageLink.equals(""))
					new GalleryLauncher(RateitActivityNew.this)
							.execute(imageLink);

			}
		}

	}

	private int getIdOfComponentClicked(MotionEvent e) {
		try {
			View imv1 = rateItPagerAdapter.currentView
					.findViewById(R.id.RateitSCImageView01), imv2 = rateItPagerAdapter.currentView
					.findViewById(R.id.RateitSCImageView03);
			Rect rect = new Rect();
			imv1.getHitRect(rect);
			if (rect.contains((int) e.getX(), (int) e.getY())) {
				return 1;
			}
			imv2.getHitRect(rect);
			if (rect.contains((int) e.getX(), (int) e.getY())) {
				return 2;
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return -1;
	}

	private static class ImageLoaderTask extends
			AsyncTask<String, Object, Bitmap> {
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

		public ImageLoaderTask(Context ctx,
				WeakReference<ImageView> imageViewRef) {
			super();
			this.imageViewRef = imageViewRef;
			this.ctx = ctx;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			if (params[0].startsWith(ctx.getResources().getString(
					R.string.WSImages))) {
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
	public void onBackPressed() {
		if (!backPressedOnce) {
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	private void resetImageBorders(int newPageNumber) {
		View selectedView = null;
		try {

			for (int i = 0; i < pager.getChildCount(); i++) {
				int page = -1;
				View view = pager.getChildAt(i);
				if (view instanceof TextView)
					page = (Integer) view.getTag();
				else
					page = ((ViewHolder) view.getTag()).position;
				if (page >= 0 && page == newPageNumber) {
					selectedView = view;
					break;
				}
			}
			if (selectedView != null) {
				ImageView imv1 = (ImageView) selectedView
						.findViewById(R.id.RateitSCImageView01), imv2 = (ImageView) selectedView
						.findViewById(R.id.RateitSCImageView03);
				imv1.setBackgroundResource(R.drawable.button_border_big);
				imv2.setBackgroundResource(R.drawable.button_border_big);
			}
		} catch (Exception e) {
			// lg(e.getMessage());
			lg("null ptr exception");
		}
	}

	private void lg(String string) {
		if (string!=null && string.contains("xception")) {
			Log.e(TAG, string+"");
		} else {
			Log.d(TAG, string+"");
		}
	}

	/**
	 * 
	 * @return questions currently in the app
	 */
	private String getQuestionIdsAsStirng() {
		String str = "";
		// for (int i = pager.getCurrentItem() - 1; i <
		// items1.size(); i++) {
		// str = str + "," + items1.get(i).qid;
		// }

		for (RateItRate rate : items1) {
			str = str + "," + rate.qid;
		}
		if (!str.equals(""))
			return str.substring(1);
		else
			return " ";
	}

	private static class AnswerRateTask extends
			AsyncTask<Object, Object, Boolean> {
		WeakReference<RateitActivityNew> rateitActivityReference;
		boolean flipAfterDone;

		public AnswerRateTask(RateitActivityNew rateitActivity,
				boolean flipAfterDone) {
			super();
			this.rateitActivityReference = new WeakReference<RateitActivityNew>(
					rateitActivity);
			this.flipAfterDone = flipAfterDone;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (rateitActivityReference.get() != null) {
				rateitActivityReference.get().answeringInProgress = true;
				if (flipAfterDone)
					rateitActivityReference.get().respondToTouchEvents = false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			RateitActivityNew rateitActivityNew = rateitActivityReference.get();
			if (rateitActivityNew != null) {
				rateitActivityNew.answeringInProgress = false;
				rateitActivityNew.lg("answer successful : "
						+ rateitActivityNew.items1);
				if (flipAfterDone) {
					rateitActivityNew.pager.setCurrentItem(
							rateitActivityNew.pager.getCurrentItem() + 1, true);
					rateitActivityNew.respondToTouchEvents = true;
				}

			}
		}

		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
			RateitActivityNew rateitActivityNew = null;
			if (rateitActivityReference != null) {
				rateitActivityNew = rateitActivityReference.get();
				if (rateitActivityNew == null) {
					return;
				}
			} else {
				return;
			}
			Object object = values[0];
			if (object instanceof String) {
				rateitActivityNew.dialogs.getAlertDialog(object.toString())
						.show();
			} else if (object instanceof Exception) {

			} else
				;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			RateitActivityNew rateitActivityNew = rateitActivityReference.get();
			if (rateitActivityNew != null) {
				int index = (Integer) params[0];
				RateItRate question = null;
				if (index < rateitActivityNew.items1.size())
					question = rateitActivityNew.items1.get(index);
				if (question != null && !question.qid.equals("-1")) {
					String qid = question.qid;
					if (qid.equals("RateIt_Helpful_Rate1")
							|| qid.equals("RateIt_Helpful_Rate2")
							|| qid.equals("RateIt_Helpful_Rate3")
							|| qid.equals("RateIt_Helpful_Rate4")
							|| qid.equals("RateIt_Helpful_Rate5")
							|| qid.equals("RateIt_Helpful_Rate6")) {
						question.qid = "-1";
						return true;
					} else {
						if (question.isExpired()) {
							// check if the rate expired
							publishProgress(rateitActivityNew.getResources()
									.getString(R.string.ALRT_RATE_EXPRIRED));
						} else {
							try {
								// lg("ansering last question " + question.qid +
								// " at index " + i + " answer is  " +
								// question.userAnswer);
								ServiceConnector.answerRateNew(
										rateitActivityNew.getApplication(),
										question.qid, question.userAnswer);
								if (index < (rateitActivityNew.pager
										.getCurrentItem())
										&& rateitActivityNew.items1.get(index).qid
												.equals(question.qid))
									question.qid = "-1";
								return true;
							} catch (WebServiceException e) {
								// AppUtils.getAlertDialog(this,
								// getResources().getString(R.string.ALRT_NOT_CONNECTED));
								rateitActivityNew.lg(e.getMessage());
							} catch (Exception e) {
								e.printStackTrace();

							}

						}

					}

				}
				return false;
			}
			return false;

		}
	}

	/**
	 * @param i
	 *            - index of rate to be answered
	 * @return rate at i if unanswered , null otherwise
	 */
	private RateItRate getRateAt(int i) {
		RateItRate rateItRate = null;
		if (i >= 0 && i < items1.size())
			rateItRate = items1.get(i);
		if (rateItRate != null && !rateItRate.qid.equals("-1"))
			return rateItRate;
		else
			return null;
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg2) {
		lg("onPageSelected: " + arg2);
		if (arg2 >= 0 && arg2 < items1.size()
				&& items1.get(arg2).qid.equals("-1")) {
			pager.setCurrentItem(arg2 + 1, true);
			return;
		}
		if (arg2 == items1.size() && isRatesOver) {
			if (getRateAt(pager.getCurrentItem() - 1) != null)
				new AnswerRateTask(RateitActivityNew.this, false).execute(pager
						.getCurrentItem() - 1);
			if (getRateAt(pager.getCurrentItem() - 2) != null)
				new AnswerRateTask(RateitActivityNew.this, false).execute(pager
						.getCurrentItem() - 2);

			showNoMoreQuestionsDialog();
			return;
		}

		if ((items1.size() == 1)
				|| (items1.size() == 2)
				|| (items1.size() > 2 && pager.getCurrentItem() == (items1
						.size() - 2))) {
			// new RateItTask(this,false).execute(1);
			new RateItBackgroundTask(this).execute(1);
		}
		resetImageBorders(arg2);
		try {
			// reset the user answer for selected item
			int pos = pager.getCurrentItem();
			if (pos >= 0 && pos < items1.size())
				items1.get(pos).userAnswer = 0;
			RateItRate prevRate = getRateAt(pager.getCurrentItem() - 1);
			// previous rate answer should be set to 4
			if (prevRate != null && prevRate.userAnswer == 0) {
				prevRate.userAnswer = 4;
			}
		} catch (Exception e) {
			lg("tried to acces " + (pager.getCurrentItem() - 1)
					+ " in items of size " + items1.size());
		}
		// answer its previous- previous rate
		if (getRateAt(pager.getCurrentItem() - 2) != null) {
			new AnswerRateTask(RateitActivityNew.this, false).execute(pager
					.getCurrentItem() - 2);

		}

	}

}
