package com.riktamtech.android.ratethisstc.ui;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.protocol.ExecutionContext;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.dao.AchievementDAO;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.EditProfileException;
import com.riktamtech.android.ratethisstc.exceptions.LoginException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.ui.components.CustDropDown;
import com.riktamtech.android.ratethisstc.ui.components.ImageDownloader;
import com.riktamtech.android.ratethisstc.ui.components.RatingView;
import com.riktamtech.android.ratethisstc.ui.components.ShareOp;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;
import com.riktamtech.android.ratethisstc.util.FbTwConnector;
import com.riktamtech.android.ratethisstc.util.ImageDownloaderTaskCompletionListener;

public class MyProfileActivity extends Activity implements OnClickListener,
		ImageDownloaderTaskCompletionListener {
	private final int DATE_DIALOG = 10;
	private TitleComponent titleComponent;
	private LinearLayout myProfContainer;
	private TextView tabTV1, tabTV2;
	private ViewFlipper vf;
	private ShareOp shareOp;
	private Button button1, button2;

	private PrefsManager prefsManager;
	private Facebook facebook;
	AchievementDAO achievementDAO;

	private TextView tv2, tv4, tv8;
	private RatingView ratingView;

	private AppDialogs dialogs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.my_prof);

		dialogs = new AppDialogs(this);
		myProfContainer = (LinearLayout) findViewById(R.id.MyProfContainer);
		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp),
				"MyProfile!", myProfContainer);
		vf = (ViewFlipper) findViewById(R.id.MyProfViewFlipper01);
		tabTV1 = (TextView) findViewById(R.id.MyProfTabTextView01);
		tabTV2 = (TextView) findViewById(R.id.MyProfTabTextView02);
		tabTV1.setOnClickListener(this);
		tabTV2.setOnClickListener(this);
		myProfContainer.startAnimation(AppAnimations.pullingDoorClose());

		dateTextView = (TextView) findViewById(R.id.MyProfFormTextView001);
		dateTextView.setPadding(2, 2, 2, 2);
		dateTextView.setOnClickListener(this);

		final Calendar c = Calendar.getInstance();
		yy = c.get(Calendar.YEAR);
		mm = c.get(Calendar.MONTH) + 1;
		dd = c.get(Calendar.DAY_OF_MONTH);
		dateTextView.setText(dd + " - " + mm + " - " + yy);
		new CustomFontizer().fontize(myProfContainer, R.id.MyProfTabTextView01,
				R.id.MyProfTabTextView02, R.id.MyProfAchTextView01,
				R.id.MyProfAchTextView02, R.id.MyProfAchTextView03,
				R.id.MyProfAchTextView04, R.id.MyProfAchTextView05,
				R.id.MyProfAchTextView07, R.id.MyProfAchTextView08,
				R.id.MyProfFormTextView001, R.id.MyProfFormTextView01,
				R.id.MyProfFormTextView02, R.id.MyProfFormTextView03,
				R.id.MyProfFormTextView04, R.id.MyProfFormTextView05,
				R.id.MyProfFormTextView06, R.id.MyProfFormTextView07,
				R.id.MyProfFormTextView08, R.id.MyProfFormEditText01,
				R.id.MyProfFormEditText02, R.id.MyProfFormEditText04,
				R.id.MyProfFormEditText05, R.id.MyProfFormEditText06);
		button1 = (Button) findViewById(R.id.MyProfFormButton01);
		button2 = (Button) findViewById(R.id.MyProfFormButton02);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);

		countryCustDropDown = (CustDropDown) findViewById(R.id.MyProfFormTextView103);
		sexCustDropDown = (CustDropDown) findViewById(R.id.MyProfFormTextView101);
		// assign these from webservice
		countryCustDropDown.setParams("Select",
				R.array.NewRateFormVotersLocationCountry,
				AppSession.signedInUser.country_idInt);
		sexCustDropDown.setParams("Select", R.array.MyProfArGender,
				AppSession.signedInUser.sexInt);
		firstNameEditText = (EditText) findViewById(R.id.MyProfFormEditText01);
		lastNameEditText = (EditText) findViewById(R.id.MyProfFormEditText02);
		emailIdEditText = (EditText) findViewById(R.id.MyProfFormEditText04);
		passwordEditText = (EditText) findViewById(R.id.MyProfFormEditText05);
		retypePasswordEditText = (EditText) findViewById(R.id.MyProfFormEditText06);
		usernameEditText = (EditText) findViewById(R.id.MyProfFormEditText0411);
		// set the texts
		// countryCustDropDown.setText(getResources().getStringArray(R.array.NewRateFormVotersLocationCountry)[Integer.parseInt(AppSession.signedInUser.country_id)]);
		// sexCustDropDown.setText(AppSession.signedInUser.sex);
		dateTextView.setText(AppSession.signedInUser.dob);
		String[] split = AppSession.signedInUser.dob.split("-");
		dd = Integer.parseInt(split[2]);
		mm = Integer.parseInt(split[1]);
		yy = Integer.parseInt(split[0]);

		firstNameEditText.setText(AppSession.signedInUser.first_nameString);
		lastNameEditText.setText(AppSession.signedInUser.last_nameString);
		emailIdEditText.setText(AppSession.signedInUser.email_idString);
		passwordEditText.setText(AppSession.signedInUser.passwordString);
		retypePasswordEditText.setText(AppSession.signedInUser.passwordString);
		usernameEditText.setText(AppSession.signedInUser.user_nameString);

		tv2 = (TextView) findViewById(R.id.MyProfAchTextView02);
		tv4 = (TextView) findViewById(R.id.MyProfAchTextView04);
		tv8 = (TextView) findViewById(R.id.MyProfAchTextView08);
		ratingView = (RatingView) findViewById(R.id.MyProfAchView01);
		ratingView.initt(0, true, false, false);

		shareOp = new ShareOp(findViewById(R.id.include01),
				R.drawable.shareyourstatus_up, R.drawable.shareyourstatus_down);
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
		InputFilter usernameFilters[] = new InputFilter[] {
				AppUtils.userNamefilter, new InputFilter.LengthFilter(99) };
		InputFilter noTextEntryFilters[] = new InputFilter[] { AppUtils.noTextEntryFilter };
		InputFilter passwordFilters[] = new InputFilter[] { new InputFilter.LengthFilter(
				16) };

		firstNameEditText.setFilters(usernameFilters);
		lastNameEditText.setFilters(usernameFilters);
		passwordEditText.setFilters(passwordFilters);
		retypePasswordEditText.setFilters(passwordFilters);

		// should make username and email address uneditable
		emailIdEditText.setFilters(noTextEntryFilters);
		usernameEditText.setFilters(noTextEntryFilters);
		emailIdEditText.setEnabled(false);
		usernameEditText.setEnabled(false);

		tabTV1.performClick();
		
		new WebTask(this).execute("");
	}

	public void refreshAchievementsViews() {

		AppSession.signnedInUserAchievements = achievementDAO;

		float percent = achievementDAO.winningRate / 100.0f;
		ratingView.initt(percent, true, false, false);

		tv2.setText(achievementDAO.totalUploaded + "");

		tv4.setText(achievementDAO.totalRated + "");

		if (achievementDAO.badge != null) {
			tv8.setBackgroundResource(AppUtils
					.getResourceReferenceFromBadgeString(achievementDAO.badge));
			badgeString = achievementDAO.badge;
		}
	}

	private static class WebTask extends AsyncTask<Object, Object, Object> {
		WeakReference<MyProfileActivity> myProfileActivityReference;

		public WebTask(MyProfileActivity myProfileActivity) {
			myProfileActivityReference = new WeakReference<MyProfileActivity>(
					myProfileActivity);
		}

		@Override
		protected Object doInBackground(Object... params) {

			try {
				if (myProfileActivityReference.get() != null) {
					myProfileActivityReference.get().achievementDAO = ServiceConnector
							.getAchievements(myProfileActivityReference.get(),
									AppSession.signedInUser.idString + "");
				}
				return true;
			} catch (WebServiceException e) {
				return e;
			}
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (myProfileActivityReference.get() != null) {
				if (result instanceof WebServiceException) {
					((WebServiceException) result).printStackTrace();
					myProfileActivityReference.get().dialogs.getAlertDialog(
							myProfileActivityReference.get().getResources()
									.getString(R.string.ALRT_NOT_CONNECTED))
							.show();
				} else if (result instanceof Boolean) {
					myProfileActivityReference.get().refreshAchievementsViews();
				}
			}
		}

	}

	private EditText firstNameEditText, lastNameEditText, passwordEditText,
			retypePasswordEditText, emailIdEditText, usernameEditText;
	private CustDropDown countryCustDropDown, sexCustDropDown;
	private String badgeString;

	private TextView dateTextView;
	private int dd, mm, yy;

	DatePickerDialog.OnDateSetListener onDateSetListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			dd = dayOfMonth;
			mm = monthOfYear + 1;
			yy = year;
			dateTextView.setText(yy + " - " + mm + " - " + dd);

		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == tabTV1.getId()) {
			vf.setDisplayedChild(0);
			tabTV1.setTextColor(Color.WHITE);
			tabTV1.setTypeface(null, Typeface.BOLD);
			tabTV2.setTextColor(getResources().getColor(R.color.titleN));
			tabTV2.setTypeface(null, Typeface.NORMAL);

		} else if (v.getId() == tabTV2.getId()) {
			vf.setDisplayedChild(1);
			tabTV2.setTextColor(Color.WHITE);
			tabTV2.setTypeface(null, Typeface.BOLD);
			tabTV1.setTextColor(getResources().getColor(R.color.titleN));
			tabTV1.setTypeface(null, Typeface.NORMAL);
		}

		else if (v == dateTextView) {
			showDialog(DATE_DIALOG);
		} else if (v == button1) {

			if (isValidInput()) {
				new UpdateTask(this).execute(true);

			}
		} else if (v == button2) {
			button2.setEnabled(false);
			AppUtils.signOut(getApplication());
			Intent in = new Intent();
			in.putExtra("signingOut", true);
			setResult(RESULT_OK, in);
			titleComponent.imageView.performClick();

		}

		else if (v == shareOp.tv) {

			shareOp.toggleView();
		}

		else if (v == shareOp.facebookIcon) {
			Toast.makeText(this, "posting to facebook", 0).show();
			shareOp.toggleView();
			if (facebook.isSessionValid()) {

				log("got fb details from prefs");
				fbPost();
			} else {

				facebook.authorize(this, new String[] { "publish_stream",
						"read_stream", "offline_access" },
						new DialogListener() {

							@Override
							public void onFacebookError(FacebookError e) {
							}

							@Override
							public void onError(DialogError e) {
							}

							@Override
							public void onComplete(Bundle values) {
								String ACCESS_TOKEN = values
										.getString("access_token");
								String EXP_TIME = values
										.getString("expires_in");
								prefsManager.setFacebookCredentials(
										ACCESS_TOKEN, EXP_TIME);
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
				startActivityForResult(new Intent(this,
						TwitterLoginActivity.class), 31);
				overridePendingTransition(R.anim.ib, 0);
			}
		} else if (v == shareOp.mailIcon) {
			shareOp.toggleView();
			String ss = achievementDAO.badge + ".png";
			ss = ss.replaceAll(" ", "%20");
			String imageLink = getResources().getString(R.string.WSImages) + ss;
			new ImageDownloader(this, this).execute(imageLink,
					achievementDAO.badge + ".png");
		}
	}

	boolean backPressedOnce = false;

	@Override
	public void onBackPressed() {
		if (!backPressedOnce) {
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DATE_DIALOG) {
			return new DatePickerDialog(this, onDateSetListener, yy, mm - 1, dd);
		} else
			return null;
	}

	private boolean isValidInput() {
		String firstNameString = firstNameEditText.getText().toString();
		String lastNameString = lastNameEditText.getText().toString();
		String emailIdString = emailIdEditText.getText().toString();
		String passwordString = passwordEditText.getText().toString();
		String dobString = dateTextView.getText().toString().trim()
				.replaceAll(" ", "");
		String sexString = sexCustDropDown.getText().toString();
		String countryIdString = countryCustDropDown.getText().toString();
		String retypePasswordString = retypePasswordEditText.getText()
				.toString();
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(dobString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		boolean flagTextEntered = firstNameString.equals("")
				|| lastNameString.equals("") || emailIdString.equals("")
				|| passwordString.equals("") || dobString.trim().equals("")
				|| sexString.trim().equals("")
				|| countryIdString.trim().equals("");

		if (flagTextEntered) {
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_FILL_DETAILS))
					.show();
			return false;
		} else if (passwordString.length() < 6) {
			passwordEditText.setText("");
			passwordEditText.requestFocus();
			retypePasswordEditText.setText("");
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_INVALID_PASSWORD))
					.show();
			return false;
		}

		else if (!passwordString.equals(retypePasswordString)) {
			passwordEditText.setText("");
			passwordEditText.requestFocus();
			retypePasswordEditText.setText("");
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_PASSWORDS_MATCH))
					.show();
			return false;
		} else if (date != null && date.after(new Date())) {
			dialogs.getAlertDialog(
					getResources().getString(R.string.ALRT_INVALID_DATE))
					.show();
			dateTextView.requestFocus();
			return false;
		}

		else {
			return true;
		}

	}

	public String callWS() throws WebServiceException, LoginException,
			EditProfileException {
		String firstNameString = firstNameEditText.getText().toString();
		String lastNameString = lastNameEditText.getText().toString();
		String emailIdString = emailIdEditText.getText().toString();
		String passwordString = passwordEditText.getText().toString();
		String dobString = dateTextView.getText().toString().trim();

		int countryIdInt = countryCustDropDown.currentIndex;
		dobString = yy + "-" + mm + "-" + dd;

		AppUser appUser = new AppUser(AppSession.signedInUser.idString,
				firstNameString, lastNameString, emailIdString, passwordString,
				dobString, countryIdInt, sexCustDropDown.currentIndex);

		String response = ServiceConnector.editProfile(getApplication(),
				appUser);

		AppUser signedInUser = AppSession.signedInUser;
		signedInUser.first_nameString = appUser.first_nameString;
		signedInUser.last_nameString = appUser.last_nameString;
		signedInUser.dob = appUser.dob;
		signedInUser.sexInt = appUser.sexInt;
		signedInUser.country_idInt = appUser.country_idInt;
		signedInUser.email_idString = appUser.email_idString;
		signedInUser.passwordString = appUser.passwordString;
		new PrefsManager(this).setUser(new Gson().toJson(signedInUser));
		return response;

	}

	private static class UpdateTask extends
			AsyncTask<Boolean, Exception, String> {
		WeakReference<MyProfileActivity> reference;

		public UpdateTask(MyProfileActivity myProfileActivity) {
			reference = new WeakReference<MyProfileActivity>(myProfileActivity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return;
			progressDialog = ProgressDialog.show(activity, activity
					.getResources().getString(R.string.PRG_TITLE), activity
					.getResources().getString(R.string.PRG_MESSAGE));

		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return;
			Exception ex = values[0];
			if (ex.getClass() == EditProfileException.class) {
				activity.emailIdEditText.setText("");
				activity.emailIdEditText.requestFocus();
				activity.dialogs.getAlertDialog(ex.getMessage()).show();
			} else if (ex.getClass() == LoginException.class) {
				activity.dialogs.getAlertDialog(ex.getMessage()).show();
				ex.printStackTrace();
				activity.onClick(activity.button2);
			} else if (ex.getClass() == WebServiceException.class) {
				activity.dialogs.getAlertDialog(
						activity.getResources().getString(
								R.string.ALRT_NOT_CONNECTED)).show();
				ex.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(Boolean... params) {
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return null;

			if (params[0]) {
				try {
					String ss = activity.callWS();
					return ss;
				} catch (Exception e) {
					publishProgress(e);
					return null;
				}

			}
			return null;
		}

		ProgressDialog progressDialog;

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return;
			progressDialog.dismiss();
			if (result != null) {
				activity.log("returned true to post execute");
				activity.showMessageAndExit(result);
			} else {
				activity.log("result is null");
			}

		}
	}

	private void showMessageAndExit(String ss) {

		Toast.makeText(this, ss, 0).show();
		Intent in = new Intent();
		in.putExtra("signingOut", false);
		setResult(RESULT_OK, in);
		titleComponent.imageView.performClick();

	}

	private void tweet() {

		new TwitterTask(this).execute("-t");
	}

	private static class TwitterTask extends AsyncTask<String, String, String> {
		WeakReference<MyProfileActivity> reference;

		public TwitterTask(MyProfileActivity myProfileActivity) {
			reference = new WeakReference<MyProfileActivity>(myProfileActivity);
		}

		@Override
		protected String doInBackground(String... params) {
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return null;
			String msg = "";
			String imageLink = activity.getResources().getString(
					R.string.WSImages)
					+ activity.achievementDAO.badge + ".png";
			String content = activity.getResources().getString(
					R.string.FB_TW_MSG_CONTENT_MY_PROF,
					AppSession.signedInUser.user_nameString,
					activity.badgeString);
			String twitterUsername = activity.prefsManager.getTwUserName(), twitterPassword = activity.prefsManager
					.getTwPassword();
			if (params[0].equals("-t") && twitterUsername != null
					&& twitterPassword != null) {

				String res = new FbTwConnector(activity)
						.tweet(activity,
								twitterUsername,
								twitterPassword,
								AppUtils.getResourceReferenceFromBadgeString(activity.achievementDAO.badge),
								content);
				if (res.equals("ok")) {
					msg = activity.getResources().getString(
							R.string.TST_TWT_SUCCESS);
				} else if (res.equals("invalid details")) {
					msg = activity.getResources().getString(
							R.string.TST_TWT_INVALID_DETAILS);
					activity.startActivityForResult(new Intent(activity,
							TwitterLoginActivity.class), 31);
					activity.overridePendingTransition(R.anim.ib, 0);
				} else if (res.equals("failed")) {
					msg = activity.getResources().getString(
							R.string.TST_TWT_FAILED_UPLOAD);
					activity.startActivityForResult(new Intent(activity,
							TwitterLoginActivity.class), 31);
					activity.overridePendingTransition(R.anim.ib, 0);
				}
			} else if (params[0].equals("-f")) {
				String res = new FbTwConnector(activity).postToFacebookWall(
						activity.facebook, imageLink, content, activity
								.getResources()
								.getString(R.string.APPSTORE_URL));
				if (res.equals("ok")) {
					msg = activity.getResources().getString(
							R.string.TST_FB_SUCCESS);
				} else {
					msg = res;

				}
			}

			return msg;
		}

		@Override
		protected void onPostExecute(String result) {

			super.onPostExecute(result);
			MyProfileActivity activity = reference.get();
			if (activity == null)
				return;
			Toast.makeText(activity, result, 0).show();
		}
	}

	private void fbPost() {
		new TwitterTask(this).execute("-f");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)

	{

		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);

		if (requestCode == 31) {
			if (resultCode == RESULT_OK) {
				prefsManager = new PrefsManager(this);
				tweet();
			}

		}
	}

	@Override
	public void onImageDownloadComplete(File f) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, "RateThis! social status");
		i.putExtra(
				Intent.EXTRA_TEXT,
				Html.fromHtml(AppSession.signedInUser.user_nameString
						+ " RateThis! social status is..."
						+ badgeString
						+ " <p>Join the conversation. RateThis! is available for <a href=\"http://cookiejarsolutions.blogspot.com\">Apple</a> and <a	href=\"http://cookiejarsolutions.blogspot.com\">Android</a></p> "));
		i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
		i.setType("text/html");
		startActivity(Intent.createChooser(i, "Send mail"));
	}

	private void log(String str) {
		Log.d("MYPROFACT", str);
	}
}
