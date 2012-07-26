package com.riktamtech.android.ratethisstc.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.riktamtech.android.ratethisstc.R;

public class AppDialogs {
	Context ctx;

	public AppDialogs(Context ctx) {
		super();
		this.ctx = ctx;
	}

	public Dialog getAlertDialog(String title, String message, String buttonText, DialogInterface.OnClickListener clickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		final OnClickListener lOnClickListener = clickListener;
		builder.setTitle(title).setMessage(message).setPositiveButton(buttonText, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				if (lOnClickListener != null) {
					lOnClickListener.onClick(dialog, which);
				} else {
					dialog.dismiss();
				}

			}
		});
		return builder.create();
	}

	public Dialog getAlertDialog(String message) {
		return getAlertDialog(ctx.getResources().getString(R.string.ALRT_DIALOG_TITLE), message, ctx.getResources().getString(R.string.ALRT_BUTTON_OK), null);
	}

	public Dialog getAlertDialog(String message, OnClickListener listener) {
		return getAlertDialog(ctx.getResources().getString(R.string.ALRT_DIALOG_TITLE), message, ctx.getResources().getString(R.string.ALRT_BUTTON_OK), listener);
	}

}
