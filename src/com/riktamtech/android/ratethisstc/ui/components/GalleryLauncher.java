package com.riktamtech.android.ratethisstc.ui.components;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.db.ImagesCacheSD;

public class GalleryLauncher extends AsyncTask<String, String, String> {

	Context ctx;
	ProgressDialog progressDialog;

	public GalleryLauncher(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(ctx, null, "Loading..");
	}

	@Override
	protected void onPostExecute(String result) {

		super.onPostExecute(result);
		progressDialog.dismiss();
		Toast.makeText(ctx, "click back button to return", 0).show();
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			String s = params[0];
			if (s.startsWith(ctx.getResources().getString(R.string.WSImages))) {
				ImagesCacheSD imagesCacheSD=new ImagesCacheSD();
				if (!imagesCacheSD.containsImage(s)) {
					imagesCacheSD.downloadBitmap(s);
				}
				String s1=imagesCacheSD.getLocalFileLocation(s);
				s=s1;
			}
			Intent i = new Intent();
			i.setAction(android.content.Intent.ACTION_VIEW);
			i.setDataAndType(Uri.fromFile(new File(s)), "image/jpg");
			ctx.startActivity(i);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... values) {

		super.onProgressUpdate(values);
		progressDialog.setMessage(values[0]);
	}

}
