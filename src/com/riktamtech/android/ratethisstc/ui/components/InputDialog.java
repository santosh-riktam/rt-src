package com.riktamtech.android.ratethisstc.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.util.AppUtils;

public class InputDialog extends Dialog implements OnClickListener
{
	TextView textView;
	EditText editText;
	Context ctx;
	InputDialogClickListener clickListener;
	Button okButton, cancelButton;

	public InputDialog(Context context, InputDialogClickListener listener,String hint,String okButtonText,String cancelButtonText,int maxLengthInt)
	{
		super(context);
		setTitle(null);
		
		
		ctx = context;
		clickListener = listener;
		setContentView(R.layout.input_dialog_ui);
		editText = (EditText) findViewById(R.id.EditText01);
		
		textView=(TextView) findViewById(R.id.textView1);
		setPrompt(hint);
		InputFilter filters[]=new InputFilter[2];
		filters[0]=new InputFilter.LengthFilter(maxLengthInt);
		filters[1]=AppUtils.noNewLineFilter;
		
		editText.setFilters(filters);
		okButton=(Button) findViewById(R.id.Button01);
		okButton.setOnClickListener(this);
		okButton.setText(okButtonText);
		
		cancelButton=(Button) findViewById(R.id.Button02);
		cancelButton.setOnClickListener(this);
		cancelButton.setText(cancelButtonText);		
		
		
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		    }
		});

	}

	
	public void setPrompt(String hint)
	{
		textView.setText(hint);
	}
		
	public interface InputDialogClickListener
	{
		public void inputEntered(String text);
	}

	@Override
	public void onClick(View v)
	{
		if(v==cancelButton)
			{
				dismiss();
				//clickListener.inputEntered("");
			}
		else if(v==okButton)
			{
				dismiss();
				clickListener.inputEntered(editText.getText().toString());
			}
		else;
	}

}
