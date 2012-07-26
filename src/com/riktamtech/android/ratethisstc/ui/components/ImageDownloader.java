package com.riktamtech.android.ratethisstc.ui.components;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.riktamtech.android.ratethisstc.util.ImageDownloaderTaskCompletionListener;

/**
 * downloads file and
 * 
 * @author santu
 * 
 */
public class ImageDownloader extends AsyncTask<String, String, File> {
	Context ctx;
	ProgressDialog progressDialog;
	ImageDownloaderTaskCompletionListener listener;

	public ImageDownloader(Context ctx,
			ImageDownloaderTaskCompletionListener listener) {
		super();
		this.ctx = ctx;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {

		super.onPreExecute();
		progressDialog = ProgressDialog.show(ctx, null, "Loading..");
	}

	@Override
	protected File doInBackground(String... params) {
		try {
			log(params[0]);
			Bitmap bitmap = BitmapFactory.decodeStream(new URL(params[0])
					.openStream());
			File file = new File(ctx.getExternalFilesDir(null), params[1]);
			file.createNewFile();
			bitmap.compress(CompressFormat.JPEG, 100,
					new FileOutputStream(file));
			return file;
		} catch (Exception e) {
			e.printStackTrace();

		}
		return null;
	}

	@Override
	protected void onPostExecute(File result) {

		super.onPostExecute(result);
		if (result != null) {

			listener.onImageDownloadComplete(result);
		} else {
			Toast.makeText(ctx, "Error", 0).show();
		}
		progressDialog.dismiss();
	}

	private void log(String s) {
		Log.d("IMAGEDOWNLOADER", s);
	}
}
