package com.riktamtech.android.ratethisstc.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class HelpActivity extends Activity {
	ViewHolder holder;
	TitleComponent titleComponent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.help1);
		//new CustomFontizer().fontize((ViewGroup)findViewById(R.id.LinearLayout01), R.id.HelpTabsTextView01,R.id.HelpTabsTextView02);
		
		holder = new ViewHolder();
		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp), "Help  ", findViewById(R.id.HelpContainerLL));
	}

	class ViewHolder implements OnClickListener {
		TextView tab1, tab2;
		WebView webView;
		LinearLayout container;
		TitleComponent titleComponent;

		public ViewHolder() {
			tab1 = (TextView) findViewById(R.id.HelpTabsTextView01);
			tab2 = (TextView) findViewById(R.id.HelpTabsTextView02);
			//CustomFontizer customFontizer=new CustomFontizer();
			//customFontizer.fontize(HelpActivity.this,tab1);customFontizer.fontize(HelpActivity.this,tab2);
			
			container = (LinearLayout) findViewById(R.id.HelpContainerLL);
			webView = (WebView) findViewById(R.id.HelpWebView01);
			webView.setVerticalScrollBarEnabled(false);
			webView.setBackgroundColor(0);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setSupportZoom(false);
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (url.contains("cookiejar")) {
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("text/plain");
						i.putExtra(Intent.EXTRA_EMAIL, new String[] { "ratethis@cookiejarsolutions.com" });
						i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
						startActivity(Intent.createChooser(i, "Send mail"));
						return true;
					}
					view.loadUrl(url);
					return true;
				}
			});
			tab1.setOnClickListener(this);
			tab2.setOnClickListener(this);
			titleComponent = new TitleComponent(HelpActivity.this, findViewById(R.id.TitleComp), "Help", container);
			new CustomFontizer().fontize(container, R.id.HelpTabsTextView01, R.id.HelpTabsTextView02);
			container.startAnimation(AppAnimations.pullingDoorClose());
			tab1.performClick();
		}

		@Override
		public void onClick(View v) {
			if (v == tab1) {
				webView.loadUrl("file:///android_asset/help.html");
			tab1.setTypeface(null, Typeface.BOLD);
				tab1.setTextColor(Color.WHITE);
				tab2.setTypeface(null,Typeface.NORMAL);
				tab2.setTextColor(getResources().getColor(R.color.titleN));
			} else if (v == tab2) {
				webView.loadUrl("file:///android_asset/aboutus.html");
				tab2.setTypeface(tab2.getTypeface(), Typeface.BOLD);
				tab2.setTextColor(Color.WHITE);
				tab1.setTypeface(tab2.getTypeface(), Typeface.NORMAL);
				tab1.setTextColor(getResources().getColor(R.color.titleN));
			}
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

}
