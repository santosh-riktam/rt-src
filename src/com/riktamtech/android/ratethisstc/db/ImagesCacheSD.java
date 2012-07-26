package com.riktamtech.android.ratethisstc.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * light version - doesnot maintain in-memory cache. Doesnot hold references to context
 * @author Santosh Kumar D
 *
 */
public class ImagesCacheSD {
	String externalCacheDirectoryPathString;
	
	public ImagesCacheSD() {
		super();
		this.externalCacheDirectoryPathString = AppSession.EXTERNAL_CACHE_DIR_PATH+"/thumbs/";
		File file = new File(externalCacheDirectoryPathString);
		if (!file.exists())
			file.mkdirs();

	}

	
	/**
	 * downloads bitmap from str and saves in sd card
	 * @param str
	 * @return
	 * true if downloaded successfully
	 */
	public boolean downloadBitmap(String str) {
		Bitmap bmp = null;
		String location = getLocalFileLocation(str);
		File file=new File(location);
		if (!file.exists()) {
			try {
				URL url = new URL(str);
				bmp = BitmapFactory.decodeStream(url.openStream());
				if (bmp != null) {
					if (!writeFile(location, bmp)) {
						Log.e(">>>>>>>>>><<<<<<<<", "write unsuccessful");
					}
				}
			} catch (MalformedURLException e) {
				System.out.println(e.getMessage());
				return false;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}

		}
		return true;
	}
	
	
	public boolean containsImage(String imageURL) {
		String fileString = getLocalFileLocation(imageURL);
		File file=new File(fileString);
		return file.exists();
	}
	
	public Bitmap  getBitmapFromCache(String str) {
		Bitmap bmp = null;
		String file =getLocalFileLocation(str);
		bmp = readFile(file);
		return bmp;
	}

	/**
	 * Returns the local file location corresponding to url. Downloads the file if it does not exist
	 * 
	 * @param url
	 *            location of file on the web
	 * @return url of the local file
	 */
	public String getLocalFileLocation(String url) {
		String file = externalCacheDirectoryPathString+ url.substring(url.lastIndexOf('/') + 1);
		return file;
	}

	
	public boolean writeFile(String str, Bitmap bmp) {
		File file = new File(str);
		try {
			return bmp.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private Bitmap readFile(String str) {
		//Log.d("IIIIIIIII", "reading "+str);
		File file = new File(str);
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

}
