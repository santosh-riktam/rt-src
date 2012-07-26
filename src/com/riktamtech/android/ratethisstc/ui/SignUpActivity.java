package com.riktamtech.android.ratethisstc.ui;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.RegistrationException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.ui.components.CustDropDown;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class SignUpActivity extends Activity implements OnClickListener {
	private static final int DATE_DIALOG = 0;
	private LinearLayout topLayout;
	@SuppressWarnings("unused")
	private TitleComponent titleComponent;
	private AppDialogs dialogs;
	private EditText firstNameEditText, lastNameEditText, emailIdEditText, userNameEditText, passwordEditText, retypePasswordEditText;
	private ProgressDialog progressDialog;
	private Button createButton;
	private CustDropDown countryCustDropDown, sexCustDropDown;
	private TextView dateTextView;
	private int dd, mm, yy;
	private DatePickerDialog.OnDateSetListener onDateSetListener = new OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			dd = dayOfMonth;
			mm = monthOfYear + 1;
			yy = year;
			dateTextView.setText(dd + " - " + mm + " - " + yy);

		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.signup);
		dialogs=new AppDialogs(this);
		topLayout = (LinearLayout) findViewById(R.id.SignupFormLinearLayout01);
		new CustomFontizer().fontize(topLayout, R.id.SignupFormTextView001, R.id.SignupFormTextView01, R.id.SignupFormTextView02, R.id.SignupFormTextView03, R.id.SignupFormTextView04, R.id.SignupFormTextView05, R.id.SignupFormTextView06, R.id.SignupFormTextView07, R.id.SignupFormTextView08, R.id.SignupFormTextView1001, R.id.SignupFormTextView101, R.id.SignupFormTextView103);
		dateTextView = (TextView) findViewById(R.id.SignupFormTextView001);
		dateTextView.setPadding(2, 2, 2, 2);
		sexCustDropDown = (CustDropDown) findViewById(R.id.SignupFormTextView101);
		countryCustDropDown = (CustDropDown) findViewById(R.id.SignupFormTextView103);
		firstNameEditText = (EditText) findViewById(R.id.SignupFormEditText01);
		lastNameEditText = (EditText) findViewById(R.id.SignupFormEditText02);
		emailIdEditText = (EditText) findViewById(R.id.SignupFormEditText04);
		userNameEditText = (EditText) findViewById(R.id.SignupFormEditText1001);
		passwordEditText = (EditText) findViewById(R.id.SignupFormEditText05);
		retypePasswordEditText = (EditText) findViewById(R.id.SignupFormEditText06);

		sexCustDropDown.setParams("select", R.array.MyProfArGender, -1);
		countryCustDropDown.setParams("select", R.array.NewRateFormVotersLocationCountry, -1);
		createButton = (Button) findViewById(R.id.SignupFormButton01);
		createButton.setOnClickListener(this);
		final Calendar c = Calendar.getInstance();
		yy = c.get(Calendar.YEAR);
		mm = c.get(Calendar.MONTH) + 1;
		dd = c.get(Calendar.DAY_OF_MONTH);
		dateTextView.setText(" ");
		dateTextView.setOnClickListener(this);
		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "Ratethis!  ", -1, topLayout);
		
		InputFilter usernameFilters[] = new InputFilter[] { AppUtils.userNamefilter, new InputFilter.LengthFilter(99) };
		InputFilter emailFilters[] = new InputFilter[] { AppUtils.emailfilter, new InputFilter.LengthFilter(50) };
		InputFilter passwordFilters[] = new InputFilter[] { new InputFilter.LengthFilter(16) };

		firstNameEditText.setFilters(usernameFilters);
		lastNameEditText.setFilters(usernameFilters);
		userNameEditText.setFilters(usernameFilters);
		passwordEditText.setFilters(passwordFilters);
		retypePasswordEditText.setFilters(passwordFilters);
		emailIdEditText.setFilters(emailFilters);

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DATE_DIALOG) {
			return new DatePickerDialog(this, onDateSetListener, yy, mm - 1, dd);
		} else
			return null;
	}

	@Override
	public void onClick(View v) {
		if (v == dateTextView) {
			showDialog(DATE_DIALOG);
		} else if (v == createButton) {
			validateInputAndSubmit();
		}
	}

	private void showMessageAndExit() {

		Toast.makeText(this, getResources().getString(R.string.ALRT_REGISTRATION_SUCCESSFUL), 0).show();
		Intent in = new Intent(SignUpActivity.this, MainMenuActivity.class);
		in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(in);
		overridePendingTransition(0, 0);
		Intent finishIntent = new Intent();
		finishIntent.putExtra("success", true);
		setResult(RESULT_OK, finishIntent);
		finish();

	}
	
	private static class SignupTask extends AsyncTask<AppUser, Exception, String> {
		
		WeakReference<SignUpActivity> reference;
		
		SignupTask(SignUpActivity activity)
		{
			reference=new WeakReference<SignUpActivity>(activity);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SignUpActivity activity=reference.get();
			if(activity==null) return;
			
			activity.progressDialog = ProgressDialog.show(activity, activity.getResources().getString(R.string.PRG_TITLE), activity.getResources().getString(R.string.PRG_MESSAGE));
		}

		@Override
		protected String doInBackground(AppUser... params) {
			SignUpActivity activity=reference.get();
			if(activity==null) return null;
			AppUser appUser = params[0];
			try {
				String returnId = ServiceConnector.register(activity.getApplication(), appUser);
				appUser.idString = returnId;
				appUser.push_notificationsBoolean = true;
				appUser.share_fbBoolean = true;
				appUser.sh_twitterBoolean = true;
				appUser.background_on_3gBoolean = true;
				appUser.prioritizing_ratesInt = 0;
				appUser.default_locationString = "Everyone";
				appUser.default_voting_durationInt = 2;
				appUser.default_age_groupInt = 0;
				appUser.filtersString="1,1,2,1,3,1,4,1,5,1,6,1,7,1,8,1,9,1,10,1,11,1,12,1,13,1,14,1,15,1,16,1,17,1,18,1,19,1,20,1,21,1,22,1,23,1,24,1,25,1,26,1,27,1,28,1,29,1";
				appUser.showDemo=true;
				AppSession.signedInUser = appUser;
				new PrefsManager(activity).setUser(new Gson().toJson(appUser).toString());
				return returnId;
			} catch (Exception e) {
				publishProgress(e);
			}
			return null;

		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			SignUpActivity activity=reference.get();
			if(activity==null) return;
			
			Exception e1 = values[0];
			if (e1.getClass() == WebServiceException.class) {
				activity.dialogs.getAlertDialog( activity.getResources().getString(R.string.ALRT_NOT_CONNECTED)).show();
				e1.printStackTrace();
			} else if (e1.getClass() == RegistrationException.class) {
				RegistrationException e12 = (RegistrationException) e1;
				if (e12.type == RegistrationException.INVALID_EMAIL) {
					activity.emailIdEditText.requestFocus();
				} else if (e12.type == RegistrationException.INVALID_USERNAME) {
					activity.userNameEditText.requestFocus();
				}
				activity.dialogs.getAlertDialog( e1.getMessage()).show();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			SignUpActivity activity=reference.get();
			if(activity==null) return;
			
			super.onPostExecute(result);
			activity.progressDialog.dismiss();
			if (result != null) {
				activity.showMessageAndExit();
			}
		}
	}

	private boolean validateInputAndSubmit() {
		String firstNameString = firstNameEditText.getText().toString();
		String lastNameString = lastNameEditText.getText().toString();
		String emailIdString = emailIdEditText.getText().toString();
		String userNameString = userNameEditText.getText().toString();
		String passwordString = passwordEditText.getText().toString();
		String dobString = dateTextView.getText().toString().replaceAll(" ", "");
		String sexString = sexCustDropDown.getText().toString();
		String countryIdString = countryCustDropDown.getText().toString();
		int countryIdInt = countryCustDropDown.currentIndex;
		String retypePasswordString = retypePasswordEditText.getText().toString();
		Date date = null;
		try {
			date = new SimpleDateFormat("dd-MM-yyyy").parse(dobString);
		} catch (ParseException e) {

			e.printStackTrace();
		}
		boolean flagTextEntered = firstNameString.equals("") || lastNameString.equals("") || emailIdString.equals("") || userNameString.equals("") || passwordString.equals("") || dobString.trim().equals("") || sexString.trim().equals("") || countryIdString.trim().equals("");

		if (flagTextEntered) {
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_FILL_DETAILS)).show();
			return false;
		} else if (!emailIdString.matches("[A-Za-z0-9\\._\\-]+@[A-Za-z0-9\\-]+\\.[A-Za-z0-9\\.]+")) {
			emailIdEditText.requestFocus();
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_INVALID_EMAIL_ID)).show();
			return false;
		}

		else if (passwordString.length() < 6) {
			passwordEditText.setText("");
			passwordEditText.requestFocus();
			retypePasswordEditText.setText("");
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_INVALID_PASSWORD)).show();
			return false;
		}

		else if (!passwordString.equals(retypePasswordString)) {
			passwordEditText.setText("");
			passwordEditText.requestFocus();
			retypePasswordEditText.setText("");
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_PASSWORDS_MATCH)).show();
			return false;
		}
		else if (date != null && date.after(new Date())) {
			dialogs.getAlertDialog( getResources().getString(R.string.ALRT_INVALID_DATE)).show();
			dateTextView.requestFocus();
			return false;
		}

		else {
			dobString = yy + "-" + mm + "-" + dd;
			int sexInt = sexString.trim().equalsIgnoreCase("Male") ? 0 : 1;
			AppUser appUser = new AppUser(firstNameString, lastNameString, emailIdString, userNameString, passwordString, AppSession.DEVICE_TOKEN, dobString, countryIdInt, sexInt);
			new SignupTask(this).execute(appUser);
			return true;
		}

	}

}
