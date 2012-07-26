package com.riktamtech.android.ratethisstc.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.animations.MyAnimListener;
import com.riktamtech.android.ratethisstc.dao.NewRateDAO;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.MainMenuAdapter;

/**
 * 
 * @author santu
 * 
 *         App's main menu
 */
public class MainMenuActivity extends ListActivity {

	private ListView listView;
	private AppDialogs dialogs;
	int num;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		AppSession.initArrays(getApplication());
		setContentView(R.layout.main_menu_new);
		
		dialogs=new AppDialogs(this);		
		listView = getListView();
		listView.setAdapter(new MainMenuAdapter(getApplication()));
		listView.setDividerHeight(0);
		listView.setVerticalScrollBarEnabled(false);
		AppSession.newRateDAO=new Gson().fromJson(new PrefsManager(this).getnewRateObjectDao(), NewRateDAO.class);
		if (AppSession.newRateDAO==null) {
			AppSession.newRateDAO=new NewRateDAO();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);
		// first item is invisible, so we devrement click position by 1 - also refer adapter for details
		position--;
		if (position<0) 
			return; 
		
		if (!ServiceConnector.testConnection(getApplication()) && position != 0 && position != 7) {
			dialogs.getAlertDialog(getResources().getString(R.string.ALRT_NOT_CONNECTED)).show();
			return;
		}
		Intent intent = null;
		final View view = l;
		view.setEnabled(false);
		num = position;
		switch (position) {
		case 0:
			intent = new Intent(this, NewRateActivity.class);

			break;
		case 1:
			intent = new Intent(this, RateitActivityNew.class);
			break;
		case 2:
			intent = new Intent(this, RatedActivity.class);
			intent.putExtra("isMyRates", true);
			break;
		case 3:
			intent = new Intent(this, RatedActivity.class);
			break;
		case 4:
			intent = new Intent(this, MyProfileActivity.class);
			break;
		case 5:
			intent = new Intent(this, SettingsActivity.class);
			break;
		case 6:
			tellAFriend();
			view.setEnabled(true);
			return;
		case 7:
			intent = new Intent(this, HelpActivity.class);
			break;
		
		default:
			break;
		}

		final Intent in = intent;
		in.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		
		// dont open new rate if the image is still uploading
		if (position == 0) {
			if (AppSession.newRatePosted==AppSession.UPLOAD_IN_PROGRESS) {
				listView.setEnabled(true);
				return;
			}
		}
		Animation animation = AppAnimations.pullingDoorOpen(listView.getLeft(),listView.getHeight()/2);
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
				view.setEnabled(true);
				startActivityForResult(in, num);
				overridePendingTransition(0, 0);
			}
		});
		listView.startAnimation(animation);
		
	}

	private void tellAFriend() {
		Intent i=new Intent(Intent.ACTION_SEND);
		
		String msg="Hey,<br/> <p> I just downloaded RateThis! for Android. You should try it, it lets you put up 2 pictures of anything, and allow people to vote on either one. It can help you in any manner of ways. Choosing gifts, picking a new car, or where should you travel.</p>\r\n" + 
				"<p>It's awesome & free!</p>\r\n" + 
				"<p>Download it <a href=\"https://market.android.com/\">here</a>.</p>";
		i.putExtra(Intent.EXTRA_SUBJECT, "I just downloaded RateThis!");
		i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(msg));
		i.setType("text/html");
		startActivity(Intent.createChooser(i, "Send mail"));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("##################33", "on act result called");
		if (requestCode == 4) {
			if (data != null) {
				boolean bl = data.getBooleanExtra("signingOut", false);
				if (bl) {
					startActivity(new Intent(this, LoginActivity.class));
					overridePendingTransition(0, 0);
					finish();

				}
			}
		}
 
		Animation animation = AppAnimations.pushingDoorClose(listView.getLeft(),listView.getHeight()/2);
		animation.setAnimationListener(new MyAnimListener(false, listView));
		listView.startAnimation(animation);

	}

	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
		String reloaded = intent.getStringExtra("goto");
		if (reloaded != null && reloaded.equals("myrates")) {
			Intent intent1 = new Intent(this, RatedActivity.class);
			intent1.putExtra("isMyRates", true);
			intent1.putExtra("displayNewRate", true);
			AppSession.stopListeningToLocationUpdates();
			startActivity(intent1);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onDestroy() {
		PrefsManager prefsManager=new PrefsManager(this);
		Gson gson=new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		prefsManager.setNewRateDetails(gson.toJsonTree(AppSession.newRateDAO, NewRateDAO.class).toString(),AppSession.NEW );
		//System.runFinalizersOnExit(true);
		super.onDestroy();
		//System.exit(0);
		
	}
}
