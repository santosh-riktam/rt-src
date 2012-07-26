package com.riktamtech.android.ratethisstc.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.AchievementDAO;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.ui.components.CustDropDown;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class SettingsActivity extends Activity implements OnClickListener {
	private TitleComponent titleComponent;
	private ScrollView scrollView;
	private AppUser user;
	private AppDialogs dialogs;
	private boolean backPressedOnce = false;
	private CustDropDown loadingRatesCustDropDown, locationCustDropDown1, locationCustDropDown2, durationCustDropDown, locationCustDropDown3, ageGroupCustDropDown;
	// PrefsManager prefsManager;
	private ImageButton taglistButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.settings);
		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "Settings", scrollView);
		titleComponent.imageView.setOnClickListener(this);
		dialogs = new AppDialogs(this);
		new StartUpTask(this).execute("");
	}

	private static class StartUpTask extends AsyncTask<Object, Object, Boolean> {
		ProgressDialog progressDialog;
		WeakReference<SettingsActivity> reference;

		StartUpTask(SettingsActivity activity) {
			reference = new WeakReference<SettingsActivity>(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SettingsActivity activity = reference.get();
			if (activity == null)
				return;
			progressDialog = ProgressDialog.show(activity, null, activity.getResources().getString(R.string.PRG_LOADING));
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			SettingsActivity activity = reference.get();
			if (activity == null)
				return false;
			boolean connected = ServiceConnector.testConnection(activity.getApplication());
			return connected;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			final SettingsActivity activity = reference.get();
			if (activity == null)
				return;

			if (result) {

				activity.scrollView = (ScrollView) activity.findViewById(R.id.ScrollView01);

				new CustomFontizer().fontize(activity.scrollView,  R.id.NRFTextView005, R.id.NRFTextView1004, R.id.NRFTextView04,
						R.id.NRFTextView05, R.id.NRFTextView104, R.id.TextView710, R.id.TextView707);
				
				activity.loadingRatesCustDropDown = (CustDropDown) activity.findViewById(R.id.NRFTextView1004);

				activity.locationCustDropDown1 = (CustDropDown) activity.findViewById(R.id.NRFTextView102);
				activity.locationCustDropDown2 = (CustDropDown) activity.findViewById(R.id.NRFTextView103);
				activity.locationCustDropDown3 = (CustDropDown) activity.findViewById(R.id.NRFTextView1031);

				activity.durationCustDropDown = (CustDropDown) activity.findViewById(R.id.NRFTextView104);

				activity.ageGroupCustDropDown = (CustDropDown) activity.findViewById(R.id.TextView710);

				activity.taglistButton = (ImageButton) activity.findViewById(R.id.ImageButton01);
				activity.taglistButton.setOnClickListener(activity);

				activity.user = AppSession.signedInUser;
				activity.initSpinners();
				// scrollView.startAnimation(AppAnimations.pullingDoorClose());
				progressDialog.dismiss();

			} else {
				progressDialog.dismiss();
				activity.dialogs.getAlertDialog(activity.getResources().getString(R.string.ALRT_NOT_CONNECTED), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				}).show();
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

	private void initSpinners() {

		loadingRatesCustDropDown.setParams("Select", R.array.STSp1, user.prioritizing_ratesInt);

		durationCustDropDown.setParams("Select", R.array.NewRateFormVotingTime, user.default_voting_durationInt);
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
				durationCustDropDown.removeLastItem();
			}

		} catch (WebServiceException e) {
			dialogs.getAlertDialog(getResources().getString(R.string.ALRT_NOT_CONNECTED)).show();
		}
		ageGroupCustDropDown.setParams("Select", R.array.NewRateFormVotersAgeSp, user.default_age_groupInt);

		locationCustDropDown1.setParams("Select", R.array.NewRateFormVotersLocationCategoriesSp, user.getDefLocation1());
		locationCustDropDown1.onClickListener = this;

		if (locationCustDropDown1.currentIndex == 1) {
			locationCustDropDown2.setParams("select", R.array.NewRateFormVotersLocationRadiusSp, user.getDefLocation2Text());
			locationCustDropDown2.setVisibility(View.VISIBLE);
			locationCustDropDown3.setParams("select", R.array.NewRateFormVotersLocationRadiusKMSp, user.getDefLocation3Text());
			locationCustDropDown3.setVisibility(View.VISIBLE);
		}

		else if (locationCustDropDown1.currentIndex == 2) {
			locationCustDropDown2.setParams("select", R.array.NewRateFormVotersLocationCountry, user.getDefLocation2Text());
			locationCustDropDown2.setVisibility(View.VISIBLE);
		}

		else {
			locationCustDropDown2.setVisibility(View.GONE);
			locationCustDropDown3.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {

		if (v == locationCustDropDown1) {

			int index = locationCustDropDown1.currentIndex;
			if (index == 2) {
				locationCustDropDown2.setParams("select", R.array.NewRateFormVotersLocationCountry, 0);
				locationCustDropDown2.setVisibility(View.VISIBLE);
				locationCustDropDown3.setVisibility(View.GONE);
			} else if (index == 1) {
				locationCustDropDown2.setParams("select", R.array.NewRateFormVotersLocationRadiusSp, 0);
				locationCustDropDown2.setVisibility(View.VISIBLE);
				locationCustDropDown3.setParams("select", R.array.NewRateFormVotersLocationRadiusKMSp, 0);
				locationCustDropDown3.setVisibility(View.VISIBLE);
			} else {
				locationCustDropDown2.setVisibility(View.GONE);
				locationCustDropDown3.setVisibility(View.GONE);
			}
		} else if (v == titleComponent.imageView) {
			new UpdateTask(this).execute(true);
		} else if (v == taglistButton) {
			taglistButton.setEnabled(false);
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
					scrollView.setVisibility(View.INVISIBLE);
					Intent intent = new Intent(SettingsActivity.this, TaglistActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivityForResult(intent, 0);
					overridePendingTransition(0, 0);
				}
			});
			scrollView.startAnimation(animation);
		}
	}

	private String commit() throws WebServiceException {

		String default_locationString = "Everyone";
		if (locationCustDropDown1.currentIndex == 1) {
			default_locationString = "Within " + locationCustDropDown2.getDisplayedText() + " " + locationCustDropDown3.getDisplayedText() + " radius";
		} else if (locationCustDropDown1.currentIndex == 2) {
			default_locationString = locationCustDropDown2.getDisplayedText();
		}
		AppUser u = new AppUser(user.idString, default_locationString, user.filtersString, loadingRatesCustDropDown.currentIndex, ageGroupCustDropDown.currentIndex,
				durationCustDropDown.currentIndex, locationCustDropDown3.currentIndex, AppSession.signedInUser.push_notificationsBoolean, AppSession.signedInUser.share_fbBoolean,
				AppSession.signedInUser.sh_twitterBoolean,true);
		String result = ServiceConnector.saveSettings(getApplication(), u);
		AppUser signedInUser = AppSession.signedInUser;
		signedInUser.push_notificationsBoolean = u.push_notificationsBoolean;
		signedInUser.share_fbBoolean = u.share_fbBoolean;
		signedInUser.sh_twitterBoolean = u.sh_twitterBoolean;
		signedInUser.background_on_3gBoolean = u.background_on_3gBoolean;
		signedInUser.prioritizing_ratesInt = u.prioritizing_ratesInt;
		signedInUser.default_locationString = u.default_locationString;
		signedInUser.default_voting_durationInt = u.default_voting_durationInt;
		signedInUser.default_age_groupInt = u.default_age_groupInt;
		new PrefsManager(this).setUser(new Gson().toJson(signedInUser).toString());
		return result;

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == RESULT_OK) {
			scrollView.setVisibility(View.VISIBLE);
			Animation animation = AppAnimations.pushingDoorClose();
			scrollView.startAnimation(animation);
			taglistButton.setEnabled(true);
		}
	}

	private static class UpdateTask extends AsyncTask<Boolean, Exception, String> {
		WeakReference<SettingsActivity> reference;

		public UpdateTask(SettingsActivity activity) {
			reference=new WeakReference<SettingsActivity>(activity);
					
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SettingsActivity activity=reference.get();
			if(activity==null) return ;
			progressDialog = ProgressDialog.show(activity, null, activity.getResources().getString(R.string.PRG_SAVEING));

		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			final SettingsActivity activity=reference.get();
			if(activity==null) return ;
			activity.dialogs.getAlertDialog(activity.getResources().getString(R.string.ALRT_NOT_CONNECTED), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					activity.finish();
				}
			}).show();
		}

		@Override
		protected String doInBackground(Boolean... params) {
			SettingsActivity activity=reference.get();
			if(activity==null) return null;
			if (params[0]) {
				try {
					String ss = activity.commit();
					return ss;
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(e);
				}

			}
			return null;
		}

		Dialog progressDialog;

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			SettingsActivity activity=reference.get();
			if(activity==null) return ;
			progressDialog.dismiss();
			if (result != null) {
				activity.log("returned true to post execute");
				activity.showMessageAndExit(result);
			} else {
				// in case of web service exception
				activity.log("result is null");
			}

		}
	}

	/**
	 * shows the message (if not null ) and performs animation to exit
	 * 
	 * @param ss
	 */
	private void showMessageAndExit(String ss) {
		if (ss != null) {
			//Toast.makeText(this, ss, 0).show();
			log(ss);
		}
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

				scrollView.setVisibility(View.INVISIBLE);
				finish();
				overridePendingTransition(0, 0);
			}
		});
		scrollView.startAnimation(animation);

	}

	public void log(String m) {
		Log.d("SettingsActivity", m);
	}
}
