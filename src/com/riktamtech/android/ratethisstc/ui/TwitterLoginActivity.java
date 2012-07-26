package com.riktamtech.android.ratethisstc.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.CustomFontizer;

public class TwitterLoginActivity extends Activity implements OnClickListener
{
	private EditText userNameEditText, pwdEditText;
	private ImageButton signinButton, cancelButton;
	private PrefsManager prefsManager;
	private AppDialogs dialogs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.tw_login);

		dialogs=new AppDialogs(this);
		new CustomFontizer().fontize((LinearLayout) findViewById(R.id.LinearLayout01), R.id.TextView01, R.id.TextView02, R.id.TextView03);

		userNameEditText = (EditText) findViewById(R.id.EditText01);
		pwdEditText = (EditText) findViewById(R.id.EditText02);

		signinButton = (ImageButton) findViewById(R.id.ImageButton01);
		signinButton.setOnClickListener(this);

		cancelButton = (ImageButton) findViewById(R.id.ImageButton02);
		cancelButton.setOnClickListener(this);

		prefsManager=new PrefsManager(this);
		
		
	}

	@Override
	public void onClick(View v)
	{
		if (v == signinButton)
			{
				String u=userNameEditText.getText().toString(),p=pwdEditText.getText().toString();
				if(u.equals("") || p.equals(""))
					{
						dialogs.getAlertDialog(getResources().getString(R.string.ALRT_INVALID_LOGIN));
					}
				else
					{
						prefsManager.setTwitterLoginDetails(u, p);
						setResult(RESULT_OK);
						finish();
						overridePendingTransition(0, R.anim.ob);
					}
			}
		else if (v == cancelButton)
			{
				setResult(RESULT_CANCELED);
				finish();
				overridePendingTransition(0, R.anim.ob);
			}
	}

}
