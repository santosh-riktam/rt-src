package com.riktamtech.android.ratethisstc.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.exceptions.LoginException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.util.AppDialogs;

public class MainActivity extends Activity {
	AppDialogs dialogs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageView imv = new ImageView(this);
		imv.setImageResource(R.drawable.splash);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(imv);
		BugSenseHandler.setup(this, getResources().getString(R.string.bugsense_key));
		dialogs = new AppDialogs(this);
		new MyTask(this).execute("");
	}

	private static class MyTask extends AsyncTask<Object, Object, Object> {
		WeakReference<MainActivity> mainActReference;

		public MyTask(MainActivity activity) {
			mainActReference = new WeakReference<MainActivity>(activity);
		}

		@Override
		protected Object doInBackground(Object... params) {
			MainActivity mainActivity = mainActReference.get();
			if (mainActivity == null)
				return null;

			DisplayMetrics displayMetrics = new DisplayMetrics();
			mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			AppSession.DEVICE_SCREEN_WIDTH = displayMetrics.widthPixels;
			AppSession.DEVICE_SCREEN_HEIGHT = displayMetrics.heightPixels;
			AppSession.DEVICE_DENSITY = displayMetrics.densityDpi;
			AppSession.EXTERNAL_CACHE_DIR_PATH = mainActivity.getExternalCacheDir().getAbsolutePath();

			mainActivity.log("dpi " + AppSession.DEVICE_DENSITY+" width  "+AppSession.DEVICE_SCREEN_WIDTH+" height "+AppSession.DEVICE_SCREEN_HEIGHT);

			TelephonyManager telephonyManager = (TelephonyManager) mainActivity.getSystemService(Context.TELEPHONY_SERVICE);
			AppSession.DEVICE_TOKEN = "android" + telephonyManager.getDeviceId();

			AppSession.initArrays(mainActivity.getApplication());

			PrefsManager prefsManager = new PrefsManager(mainActivity);
			boolean hasSession = prefsManager.getUser().equals("") ? false : true;
			if (hasSession) {
				AppUser oldUser = new Gson().fromJson(prefsManager.getUser(), AppUser.class);
				AppUser user = null;

				try {
					// user might be using the app offline too
					if (ServiceConnector.testConnection(mainActivity.getApplication())) {

						user = ServiceConnector.login(mainActivity.getApplication(), oldUser.user_nameString, oldUser.passwordString, oldUser.device_tokenString);
						AppSession.signedInUser = user;
						prefsManager.setUser(new Gson().toJson(user).toString());
						hasSession = true;
					} else {
						AppSession.signedInUser = oldUser;
						return mainActivity.getResources().getString(R.string.ALRT_NOT_CONNECTED);
					}
				} catch (LoginException e1) {
					hasSession = false;
					e1.printStackTrace();
					mainActivity.fin(hasSession);
					return null;
				} catch (WebServiceException e1) {
					e1.printStackTrace();
					hasSession = false;
					return mainActivity.getResources().getString(R.string.ALRT_NOT_CONNECTED);
				}

				try {
					Thread.sleep(33);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
			mainActivity.fin(hasSession);
			return null;

		}

		@Override
		protected void onPostExecute(Object result) {
			if (result != null) {
				final MainActivity mainActivity = mainActReference.get();
				if (mainActivity != null) {

					if (result.toString().equals(mainActivity.getResources().getString(R.string.ALRT_NOT_CONNECTED))) {
						mainActivity.dialogs.getAlertDialog(mainActivity.getResources().getString(R.string.ALRT_NOT_CONNECTED), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								mainActivity.fin(true);
							}
						}).show();
					} else {
						mainActivity.dialogs.getAlertDialog(result.toString()).show();
					}
				}
			}

		}
	}

	public void fin(boolean hasSession) {
		if (hasSession) {
			Intent intent=new Intent(this, MainMenuActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		} else {
			Intent intent=new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			overridePendingTransition(0, 0);
			finish();
		}
	}

	private void log(String a) {
		Log.d("MainActivity", a);
	}
}