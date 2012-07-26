package com.riktamtech.android.ratethisstc.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.LoginException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.ui.components.InputDialog;
import com.riktamtech.android.ratethisstc.ui.components.InputDialog.InputDialogClickListener;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class LoginActivity extends Activity implements OnClickListener,
		InputDialogClickListener {

	private ImageView loginView, signupImageView;
	private RelativeLayout containerLayout;
	private EditText usernameEditText, passwordEditText;
	private TextView tv;
	private AppDialogs dialogs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.login);

		dialogs = new AppDialogs(this);
		containerLayout = (RelativeLayout) findViewById(R.id.RelativeLayout01);
		loginView = (ImageView) findViewById(R.id.ImageView01);
		loginView.setOnClickListener(this);
		tv = (TextView) findViewById(R.id.TextView03);
		tv.setOnClickListener(this);
		signupImageView = (ImageView) findViewById(R.id.ImageView02);
		signupImageView.setOnClickListener(this);
		new CustomFontizer().fontize(containerLayout, R.id.TextView01,
				R.id.TextView02, R.id.TextView03, R.id.TextView04);
		usernameEditText = (EditText) findViewById(R.id.EditText01);
		passwordEditText = (EditText) findViewById(R.id.EditText02);
		new TitleComponent(this, findViewById(R.id.TitleComp), "RateThis!  ",
				-1, containerLayout);
		usernameEditText.setFilters(new InputFilter[] {
				AppUtils.userNamefilter, new InputFilter.LengthFilter(40) });
		passwordEditText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
		passwordEditText
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_GO) {
							onClick(loginView);
						}
						return false;
					}
				});

	}

	@Override
	public void onClick(View v) {
		if (v == loginView) {

			String username = usernameEditText.getText().toString(), password = passwordEditText
					.getText().toString();
			if (username.equals("")) {
				dialogs.getAlertDialog(
						getResources().getString(R.string.ALRT_INVALID_LOGIN))
						.show();
				return;
			} else if (password.length() < 6) {
				dialogs.getAlertDialog(
						getResources()
								.getString(R.string.ALRT_INVALID_PASSWORD))
						.show();
				passwordEditText.setText("");
				return;
			} else {
				new LoginTask(this).execute(username, password);
			}
		} else if (v == tv) {
			new InputDialog(this, this, getResources().getString(
					R.string.INP_EMAIL_ID), getResources().getString(
					R.string.INP_OK_BUTTON), getResources().getString(
					R.string.INP_CANCEL_BUTTON), 50).show();

		} else if (v == signupImageView) {
			startActivityForResult(new Intent(this, SignUpActivity.class), 0);

		}
	}

	public void forgotPassword(String str) {
		try {
			String s = ServiceConnector.forgotPassword(getApplication(), str);
			Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

		} catch (WebServiceException e) {
			dialogs.getAlertDialog(getResources().getString(
					R.string.ALRT_NOT_CONNECTED));
			e.printStackTrace();
		}

	}

	@Deprecated
	@SuppressWarnings("unused")
	private static class ForgotPasswordTask extends
			AsyncTask<String, String, String> {
		WeakReference<LoginActivity> loginActReference;
		ProgressDialog progressDialog;

		public ForgotPasswordTask(LoginActivity loginActivity) {
			loginActReference = new WeakReference<LoginActivity>(loginActivity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LoginActivity loginActivity = loginActReference.get();
			if (loginActivity != null) {
				progressDialog = ProgressDialog.show(
						loginActivity,
						null,
						loginActivity.getResources().getString(
								R.string.PRG_MESSAGE));
			}
		}

		@Override
		protected String doInBackground(String... params) {
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			LoginActivity loginActivity = loginActReference.get();
			if (loginActivity != null) {
				if (result != null)
					Toast.makeText(loginActivity, result, 0);
				progressDialog.dismiss();
			}
		}
	}

	private static class LoginTask extends
			AsyncTask<String, Exception, Boolean> {
		ProgressDialog progressDialog;
		WeakReference<LoginActivity> loginActReference;

		public LoginTask(LoginActivity loginActivity) {
			loginActReference = new WeakReference<LoginActivity>(loginActivity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			LoginActivity loginActivity = loginActReference.get();
			if (loginActivity != null)
				progressDialog = ProgressDialog.show(
						loginActivity,
						null,
						loginActivity.getResources().getString(
								R.string.PRG_LOADING));
		}

		@Override
		protected Boolean doInBackground(String... params) {
			LoginActivity loginActivity = loginActReference.get();
			if (loginActivity != null) {
				try {
					String username = params[0], password = params[1];

					AppUser appUser = ServiceConnector.login(
							loginActivity.getApplication(), username, password,
							AppSession.DEVICE_TOKEN);
					AppSession.signedInUser = appUser;
					// TODO prefs ?
					new PrefsManager(loginActivity.getApplication())
							.setUser(new Gson().toJson(appUser).toString());
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(e);
				}
			}
			return false;
		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			LoginActivity loginActivity = loginActReference.get();
			if (loginActivity != null) {
				Exception e1 = values[0];
				if (e1 instanceof LoginException) {
					loginActivity.passwordEditText.setText("");
					if (((LoginException) e1).type == LoginException.UNREGISTERED_USERNAME) {
						loginActivity.usernameEditText.setText("");
						loginActivity.usernameEditText.requestFocus();
					}
					loginActivity.dialogs.getAlertDialog(e1.getMessage())
							.show();
				} else if (e1 instanceof WebServiceException) {
					loginActivity.dialogs.getAlertDialog(
							loginActivity.getResources().getString(
									R.string.ALRT_NOT_CONNECTED)).show();
				} else {
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if (result) {
				LoginActivity loginActivity = loginActReference.get();
				if (loginActivity != null) {
					loginActivity.startActivity(new Intent(loginActivity,
							MainMenuActivity.class));
					loginActivity.finish();
				}
			}

		}

	}

	@Override
	public void inputEntered(String email) {
		forgotPassword(email);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == RESULT_OK) {
			log("fnishing login act");
			finish();
		}
	}

	private void log(String m) {
		Log.d("LoginActivity", m);
	}
}
