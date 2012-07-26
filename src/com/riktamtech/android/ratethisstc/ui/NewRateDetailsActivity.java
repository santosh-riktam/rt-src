package com.riktamtech.android.ratethisstc.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.AchievementDAO;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.dao.NewRateDAO;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.ui.components.CustDropDown;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class NewRateDetailsActivity extends Activity implements OnClickListener {
	private LinearLayout topLayout;
	private ImageButton submitImageButton;

	private ImageView imageView1, imageView2;
	private TitleComponent titleComponent;

	private CustDropDown primaryTagSp, votersAgeGroupSp, votersLocationSp1, votersLocationSp2, votersLocationSp3, votingTimeSp;
	private EditText customTagEditText1, customTagEditText2;
	private AppDialogs dialogs;
	private NewRateDAO newRateDAO;
	private boolean backPressedOnce = false;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		newRateDAO = AppSession.newRateDAO;
		if (newRateDAO == null) {
			newRateDAO = new NewRateDAO();
		}
		handler = new Handler();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.new_rate_form);

		dialogs = new AppDialogs(this);

		imageView1 = (ImageView) findViewById(R.id.NRFImageView01);
		imageView2 = (ImageView) findViewById(R.id.NRFImageView02);
		topLayout = (LinearLayout) findViewById(R.id.NRFLinearLayout04);
		InputFilter filters[] = new InputFilter[2];
		filters[0] = new InputFilter.LengthFilter(20);
		filters[1] = AppUtils.noNewLineFilter;
		customTagEditText1 = (EditText) findViewById(R.id.NRFEditText02);
		customTagEditText1.setFilters(filters);

		customTagEditText2 = (EditText) findViewById(R.id.editText1);
		customTagEditText2.setFilters(filters);

		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "NewRate!  ", topLayout);
		titleComponent.imageView.setOnClickListener(this);
		new CustomFontizer().fontize(topLayout, R.id.NRFTextView01, R.id.NRFTextView02, R.id.NRFTextView03, R.id.NRFTextView04, R.id.NRFTextView05);
		imageView1.setOnClickListener(this);
		imageView2.setOnClickListener(this);
		submitImageButton = (ImageButton) findViewById(R.id.NRFImageButton01);
		submitImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Animation animation = AppAnimations.pullingDoorOpen();
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {

						topLayout.setVisibility(View.INVISIBLE);
					}
				});

				if (primaryTagSp.currentIndex == -1) {
					dialogs.getAlertDialog(getResources().getString(R.string.ALRT_PRIMARY_TAG_REQ)).show();
					primaryTagSp.requestFocus();
					return;
				} else if (customTagEditText1.getText().toString().trim().equals("")) {
					dialogs.getAlertDialog(getResources().getString(R.string.ALRT_SECONDARY_TAG_REQ)).show();
					customTagEditText1.requestFocus();
					return;
				} else if (customTagEditText2.getText().toString().trim().equals("")) {
					dialogs.getAlertDialog(getResources().getString(R.string.ALRT_SECONDARY_TAG_REQ)).show();
					customTagEditText2.requestFocus();
					return;
				}

				else {
					submitImageButton.setEnabled(false);
					if (ServiceConnector.testConnection(getApplication())) {
						saveNewRate();
						new UploadTask(NewRateDetailsActivity.this).execute();

					} else {
						dialogs.getAlertDialog(getResources().getString(R.string.ALRT_NOT_CONNECTED)).show();

					}
				}
			}
		});
		refreshScreen();
		initSpinners();

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

	private void refreshScreen() {
		new Thread(new Runnable() {
			Bitmap bitmap1 = null, bitmap2 = null;

			@Override
			public void run() {
				newRateDAO = AppSession.newRateDAO;

				if (newRateDAO.getImage1path() != null)
					bitmap1 = AppUtils.scaleBitmapToFit(newRateDAO.getImage1Bitmap(), 125, 75);
				if (newRateDAO.getImage2path() != null)
					bitmap2 = AppUtils.scaleBitmapToFit(newRateDAO.getImage2Bitmap(), 125, 75);

				handler.post(new Runnable() {

					@Override
					public void run() {
						imageView1.setImageBitmap(bitmap1);
						imageView2.setImageBitmap(bitmap2);
						customTagEditText1.setText(newRateDAO.secTagA);
						customTagEditText2.setText(newRateDAO.secTagB);

					}
				});

			}
		}).start();

	}

	private void saveNewRate() {

		newRateDAO.primaryTagId = primaryTagSp.currentIndex;
		newRateDAO.ageId = (votersAgeGroupSp.currentIndex);
		newRateDAO.qDurId = (votingTimeSp.currentIndex);
		newRateDAO.locationType = (votersLocationSp1.currentIndex);
		newRateDAO.secTagA = customTagEditText1.getText().toString();
		newRateDAO.secTagB = customTagEditText2.getText().toString();
		newRateDAO.datePosted = new SimpleDateFormat("dd MMM yyyy").format(new Date());
		newRateDAO.locationType2 = votersLocationSp2.currentIndex;
		newRateDAO.locationType3 = votersLocationSp3.currentIndex;

		String lDistance = "0";
		if (newRateDAO.locationType == 1) {
			Double distance = Double.parseDouble(votersLocationSp2.getText().toString().trim());
			int kmIndex = votersLocationSp3.currentIndex;
			if (kmIndex == 1)
				distance = distance * 1.6;
			lDistance = distance + "";

		} else if (newRateDAO.locationType == 2) {
			lDistance = votersLocationSp2.currentIndex + "";
		}
		newRateDAO.lDistance = lDistance;
		String lat = "37.0625", lng = "-95.677068";
		if (AppSession.currentUserLocation != null) {
			lat = AppSession.currentUserLocation.getLatitude() + "";
			lng = AppSession.currentUserLocation.getLongitude() + "";
		}

		newRateDAO.lat = lat;
		newRateDAO.lon = lng;

	}

	private static class UploadTask extends AsyncTask<Object, Object, Object> {
		WeakReference<NewRateDetailsActivity> reference;

		public UploadTask(NewRateDetailsActivity activity) {
			reference = new WeakReference<NewRateDetailsActivity>(activity);

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			NewRateDetailsActivity activity = reference.get();
			if (activity == null)
				return;
			AppSession.newRatePosted = AppSession.UPLOAD_IN_PROGRESS;
			AppSession.cachedRateDAO = new NewRateDAO(activity.newRateDAO);
			Intent intent = new Intent(activity, MainMenuActivity.class);
			intent.putExtra("goto", "myrates");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(intent);
			activity.overridePendingTransition(0, 0);
			
		}

		File image1, image2;

		@Override
		protected Object doInBackground(Object... params) {

			NewRateDetailsActivity activity = reference.get();
			if (activity == null)
				return null;
			try {
				HttpClient httpClient = new DefaultHttpClient();
				MultipartEntity multipartEntity = new MultipartEntity();

				image1 = new File(AppSession.newRateDAO.getImage1path());
				image2 = new File(AppSession.newRateDAO.getImage2path());

				multipartEntity.addPart("image_1.jpg", new FileBody(image1));
				multipartEntity.addPart("image_2.jpg", new FileBody(image2));
				multipartEntity.addPart("user_id", new StringBody(AppSession.signedInUser.idString + ""));
				multipartEntity.addPart("primarytag_id", new StringBody((activity.newRateDAO.primaryTagId + 1) + ""));
				multipartEntity.addPart("secondarytag_A", new StringBody(activity.newRateDAO.secTagA));
				multipartEntity.addPart("secondarytag_B", new StringBody(activity.newRateDAO.secTagB + ""));
				multipartEntity.addPart("age_id", new StringBody((activity.newRateDAO.ageId + 1) + ""));
				multipartEntity.addPart("q_dur_id", new StringBody((activity.newRateDAO.qDurId + 1) + ""));
				multipartEntity.addPart("date_posted", new StringBody(activity.newRateDAO.datePosted));
				multipartEntity.addPart("location_type", new StringBody((activity.newRateDAO.locationType + 1) + ""));
				if (activity.newRateDAO.locationType == 2) {
					int ii = Integer.parseInt(activity.newRateDAO.lDistance) + 1;
					multipartEntity.addPart("radius_or_country", new StringBody(ii + ""));
				} else {
					multipartEntity.addPart("radius_or_country", new StringBody(activity.newRateDAO.lDistance));
				}
				multipartEntity.addPart("latitude", new StringBody(activity.newRateDAO.lat));
				multipartEntity.addPart("longitude", new StringBody(activity.newRateDAO.lon));
				HttpPost post = new HttpPost(activity.getResources().getString(R.string.WSRoot) + "UploadImage1");
				post.setEntity(multipartEntity);
				HttpResponse response = httpClient.execute(post);
				int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					String str = EntityUtils.toString(entity);
					activity.log(str);
					return "y";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "n";
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			NewRateDetailsActivity activity = reference.get();
			if (activity == null)
				return;
			if (result.toString().equals("y")) {
				// reset session values
				AppSession.newRatePosted = AppSession.NEW;
				//clear bitmaps from memory
				AppSession.newRateDAO.getImage1Bitmap().recycle();
				AppSession.newRateDAO.getImage2Bitmap().recycle();

				AppSession.newRateDAO = new NewRateDAO();
				Toast.makeText(activity, "upload successful", 0).show();
			} else {
				AppSession.newRatePosted = AppSession.NEW;
				NewRateDAO newRateDAO = AppSession.newRateDAO;
				if (newRateDAO.getImage1OriginalPath() == null || newRateDAO.getImage2OriginalPath() == null || newRateDAO.getImage1path() == null
						|| newRateDAO.getImage2path() == null) {
					activity.log(new Exception("upload exception. original images= " + newRateDAO.getImage1OriginalPath() + "," + newRateDAO.getImage2OriginalPath()
							+ "  bitmaps= " + newRateDAO.getImage1path() + "," + newRateDAO.getImage2path()));
					newRateDAO = new NewRateDAO();
				}
				Toast.makeText(activity, activity.getResources().getString(R.string.TST_FAILED_UPLOAD), 0).show();

			}

		}
	}

	@Override
	public void onBackPressed() {
		if (!backPressedOnce) {
			//super.onBackPressed();
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	@Override
	public void onClick(View v) {

		if (v == imageView1) {
			startAct(1);
		} else if (v == imageView2) {
			startAct(2);
		} else if (v == votersLocationSp1) {
			int index = votersLocationSp1.currentIndex;
			if (index == 2) {

				votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationCountry, 0);
				votersLocationSp2.setVisibility(View.VISIBLE);
				votersLocationSp3.setVisibility(View.GONE);
			} else if (index == 1) {
				votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationRadiusSp, 0);
				votersLocationSp2.setVisibility(View.VISIBLE);
				votersLocationSp3.setParams("select", R.array.NewRateFormVotersLocationRadiusKMSp, 0);
				votersLocationSp3.setVisibility(View.VISIBLE);
			} else {
				votersLocationSp2.setVisibility(View.GONE);
				votersLocationSp3.setVisibility(View.GONE);
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
					titleComponent.toAnimateView.setVisibility(View.INVISIBLE);
					saveNewRate();
					Intent intent = new Intent(NewRateDetailsActivity.this, MainMenuActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					overridePendingTransition(0, 0);
				}
			});
			titleComponent.toAnimateView.startAnimation(animation);

		}

	}

	private void log(Exception ex) {
		Log.e("NewRateDetailsActivity", ex.getMessage());
	}

	private void log(String str) {
		Log.d("NewRateDetailsActivity", str);
	}

	private void startAct(int i) {
		saveNewRate();
		Intent intent = new Intent(this, NewRateActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.putExtra("image_no", i);
		startActivity(intent);
		overridePendingTransition(0, 0);
	}

	private void initSpinners() {

		// if session object has values assign them else assign default values

		primaryTagSp = (CustDropDown) findViewById(R.id.NRFTextView101);
		if (newRateDAO.primaryTagId == -1) {
			primaryTagSp.setParams("Select", R.array.NewRateFormPrimaryTagSp, getResources().getString(R.string.NewRateFormTV1DefText));
		} else {
			primaryTagSp.setParams("Select", R.array.NewRateFormPrimaryTagSp, newRateDAO.primaryTagId);
		}

		votersAgeGroupSp = (CustDropDown) findViewById(R.id.NRFTextView102);
		if (newRateDAO.ageId == -1)
			votersAgeGroupSp.setParams("Select", R.array.NewRateFormVotersAgeSp, AppSession.signedInUser.default_age_groupInt);
		else
			votersAgeGroupSp.setParams("Select", R.array.NewRateFormVotersAgeSp, newRateDAO.ageId);

		votingTimeSp = (CustDropDown) findViewById(R.id.NRFTextView105);
		if (newRateDAO.qDurId == -1) {
			votingTimeSp.setParams("Select", R.array.NewRateFormVotingTime, AppSession.signedInUser.default_voting_durationInt);
		} else {
			votingTimeSp.setParams("Select", R.array.NewRateFormVotingTime, AppSession.newRateDAO.qDurId);
		}

		try {
			// 1 year duration will be unlocked only when highest social status
			// is achieved
			AchievementDAO achievementDAO = null;
			if (AppSession.signnedInUserAchievements == null) {
				achievementDAO = ServiceConnector.getAchievements(getApplication(), AppSession.signedInUser.idString + "");
				AppSession.signnedInUserAchievements = achievementDAO;
			} else
				achievementDAO = AppSession.signnedInUserAchievements;
			if (!achievementDAO.badge.equalsIgnoreCase("Goddess") && !achievementDAO.badge.equalsIgnoreCase("I Live For This")) {
				votingTimeSp.removeLastItem();
			}

		} catch (WebServiceException e) {
			//AppUtils.getAlertDialog(this, e.getMessage()).show();
		}
		votersLocationSp1 = (CustDropDown) findViewById(R.id.NRFTextView103);
		votersLocationSp2 = (CustDropDown) findViewById(R.id.NRFTextView104);
		votersLocationSp3 = (CustDropDown) findViewById(R.id.NRFTextView1041);
		AppUser user = AppSession.signedInUser;
		int loc1 = (newRateDAO.locationType == -1) ? user.getDefLocation1() : newRateDAO.locationType;

		// first initialize with default settings
		votersLocationSp1.setParams("Select", R.array.NewRateFormVotersLocationCategoriesSp, loc1);

		if (votersLocationSp1.currentIndex == 1) {
			votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationRadiusSp, user.getDefLocation2Text());
			votersLocationSp2.setVisibility(View.VISIBLE);
			votersLocationSp3.setParams("select", R.array.NewRateFormVotersLocationRadiusKMSp, user.getDefLocation3Text());
			votersLocationSp3.setVisibility(View.VISIBLE);
		}

		else if (votersLocationSp1.currentIndex == 2) {
			votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationCountry, user.getDefLocation2Text());
			votersLocationSp2.setVisibility(View.VISIBLE);
			votersLocationSp3.setVisibility(View.GONE);
		}

		else {
			votersLocationSp2.setVisibility(View.GONE);
			votersLocationSp3.setVisibility(View.GONE);
		}

		// each rate settings take priority over user default preferences
		if (newRateDAO.locationType2 != -1) {
			if (votersLocationSp1.currentIndex == 1) {
				votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationRadiusSp, newRateDAO.locationType2);
			} else if (votersLocationSp1.currentIndex == 2) {
				votersLocationSp2.setParams("select", R.array.NewRateFormVotersLocationCountry, newRateDAO.locationType2);
			}
		}
		if (newRateDAO.locationType3 != -1) {
			votersLocationSp3.setParams("select", R.array.NewRateFormVotersLocationRadiusKMSp, newRateDAO.locationType3);
		}

		votersLocationSp1.onClickListener = this;
		primaryTagSp.requestFocus();

	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshScreen();
	}

}
