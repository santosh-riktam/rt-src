package com.riktamtech.android.ratethisstc.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Debug;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.MyRatesDAO;
import com.riktamtech.android.ratethisstc.dao.TagWrapper;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.prefs.PrefsManager;

public class AppUtils {

	public static void resizeBoxImageViews(double ratio, View... imageViews) {
		int availableWidth = AppSession.DEVICE_SCREEN_WIDTH, availableHeight = (int) (AppSession.DEVICE_SCREEN_HEIGHT / ratio);
		availableWidth = (int) (availableHeight * 4.0 / 3);
		for (View imageView : imageViews) {
			LayoutParams layoutParams = (LayoutParams) imageView.getLayoutParams();
			layoutParams.width = availableWidth;
			layoutParams.height = availableHeight;
		}

	}

	public static void resizeNewRateBoxImageViews(double ratio, View... imageViews) {
		int availableWidth = AppSession.DEVICE_SCREEN_WIDTH, availableHeight = (int) (AppSession.DEVICE_SCREEN_HEIGHT / ratio);
		availableWidth = (int) (availableHeight * 5.0 / 3);
		for (View imageView : imageViews) {
			FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) imageView.getLayoutParams();
			layoutParams.width = availableWidth;
			layoutParams.height = availableHeight;
		}

	}

	public static InputFilter userNamefilter = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {
				if (Character.isSpaceChar(source.charAt(i)) || !Character.isLetterOrDigit(source.charAt(i))) {
					return "";
				}
			}
			return null;
		}
	};

	public static InputFilter noTextEntryFilter = new InputFilter() {
		public CharSequence filter(CharSequence src, int start, int end, Spanned dst, int dstart, int dend) {
			return src.length() < 1 ? dst.subSequence(dstart, dend) : "";
		}
	};

	public static InputFilter emailfilter = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {
				char c = source.charAt(i);
				if (!(Character.isLetterOrDigit(c) || (c == '@') || (c == '.') || (c == '_')))
					return "";
			}
			return null;
		}
	};

	public static InputFilter noNewLineFilter = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {
				char c = source.charAt(i);
				if (c == '\n')
					return "";
			}
			return null;
		}
	};

	public static Bitmap scaleBitmapForUploading(Bitmap bitmap) {
		if (bitmap != null) {
			final int iphoneWidth = 640, iphoneHeight = 960;
			int w = bitmap.getWidth(), h = bitmap.getHeight();
			boolean isLandScape = w > h ? true : false;
			int tw = isLandScape ? iphoneHeight : iphoneWidth;
			int th = isLandScape ? iphoneWidth : iphoneHeight;
			float widthRatio = tw / (float) w, heightRatio = th / (float) h;
			float ratio = Math.min(heightRatio, widthRatio);
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (w * ratio), (int) (h * ratio), true);
			return resizedBitmap;
		}
		return null;
	}

	public static Bitmap scaleBitmapForThumbnailDisplay(String localURL) {
		Bitmap bitmap = BitmapFactory.decodeFile(localURL);
		if (bitmap != null) {
			int w = bitmap.getWidth(), h = bitmap.getHeight();
			int n = Math.max(w, h);
			float ratio = 160.0f / n;
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (w * ratio), (int) (h * ratio), true);
			bitmap.recycle();
			return resizedBitmap;
		}
		return null;
	}

	public static Bitmap scaleBitmapToFit(Bitmap bitmap, int width, int height) {

		if (bitmap != null) {
			int w = bitmap.getWidth(), h = bitmap.getHeight();
			boolean isLandScape = w > h ? true : false;
			int tw = isLandScape ? height : width;
			int th = isLandScape ? width : height;
			float widthRatio = tw / (float) w, heightRatio = th / (float) h;
			float ratio = Math.min(heightRatio, widthRatio);
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, (int) (w * ratio), (int) (h * ratio), true);
			return resizedBitmap;
		}
		return null;

	}

	/**
	 * checks prefs and decides to load bg or not. If wifi rates are loaded in background else if its a mobile network and background loading is enabled , then rates are loaded
	 * 
	 * @param ctx
	 *            - context
	 * @return can load rates in background or not
	 */
	public static boolean canLoadRatesFromBackground(Context ctx) {

		boolean backgroundEnabled = AppSession.signedInUser.background_on_3gBoolean;
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI), mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		Log.d("AppUtils", "wifi connected : " + wifi.isConnected() + " mobile connected  : " + mobile.isConnected());

		if (wifi.isConnected())
			return true;
		if (backgroundEnabled && mobile.isConnected())
			return true;
		return false;

	}

	/**
	 * to clear cache as a part of sign out process
	 */
	private static void clearCache(Context ctx) {
		File rootDir = ctx.getExternalCacheDir();
		File thumbsDir = new File(rootDir.getAbsolutePath() + "/thumbs/");
		if (thumbsDir.exists()) {
			for (File f1 : thumbsDir.listFiles()) {
				if (!f1.delete())
					log("couldnot delete file " + f1);
			}
			for (File f1 : rootDir.listFiles()) {
				if (!f1.delete())
					log("couldnot delete file " + f1);
			}
		}
		log("Cache cleared");
	}

	public static void signOut(Context ctx) {

		clearCache(ctx);

		Facebook facebook = new Facebook(ctx.getResources().getString(R.string.FBAppId));
		PrefsManager prefsManager = new PrefsManager(ctx);
		facebook.setAccessExpiresIn(prefsManager.getFbExpiry());
		facebook.setAccessToken(prefsManager.getFbAccessToken());
		AsyncFacebookRunner asyncFacebookRunner = new AsyncFacebookRunner(facebook);
		asyncFacebookRunner.logout(ctx, new RequestListener() {

			@Override
			public void onMalformedURLException(MalformedURLException e, Object state) {
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e, Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
				log(e.getMessage());
			}

			@Override
			public void onComplete(String response, Object state) {
				log(response);
			}
		});
		prefsManager.clearAllPrefs();

	}

	public static int getResourceReferenceFromBadgeString(String str) {
		String ar[] = { "Am I Winning", "Busy Body", "Choir Boy", "Cookie Monster", "Diva", "Everybody has an opinion", "Goddess", "Goody Two Shoes", "Hello World",
				"I Live For This", "Magnetic", "Mediocrity", "Metro", "My Mom says I m Cool", "Prima Donna", "Social Butterfly", "Socially Eclectic", "The Commissioner" };
		int i = 0;
		for (; i < ar.length; i++) {
			if (ar[i].equals(str))
				break;
		}
		if (i < ar.length) {
			switch (i) {
			case 0:
				return R.drawable.am_i_winning;
			case 1:
				return R.drawable.busy_body;
			case 2:
				return R.drawable.choir_boy;
			case 3:
				return R.drawable.cookie_monster;
			case 4:
				return R.drawable.diva;
			case 5:
				return R.drawable.everybody_has_an_opinion;
			case 6:
				return R.drawable.goddess;
			case 7:
				return R.drawable.goody_two_shoes;
			case 8:
				return R.drawable.hello_world;
			case 9:
				return R.drawable.i_live_for_this;
			case 10:
				return R.drawable.magnetic;
			case 11:
				return R.drawable.mediocrity;
			case 12:
				return R.drawable.metro;
			case 13:
				return R.drawable.my_mom_says_i_m_cool;
			case 14:
				return R.drawable.prima_donna;
			case 15:
				return R.drawable.social_butterfly;
			case 16:
				return R.drawable.socially_eclectic;
			case 17:
				return R.drawable.the_commissioner;

			default:
				return -1;
			}
		} else
			return -1;
	}

	private static void log(String s) {
		Log.d("AppUtils", s);
	}

	/**
	 * 
	 * @return get list of object representing the tags
	 */
	public static ArrayList<TagWrapper> getFiltersList() {
		String tags = AppSession.signedInUser.filtersString;
		tags = tags.replaceAll(" ", "");
		StringTokenizer tokenizer = new StringTokenizer(tags, ",", false);
		ArrayList<TagWrapper> arrayList = new ArrayList<TagWrapper>();
		while (tokenizer.hasMoreTokens()) {
			int i1 = Integer.parseInt(tokenizer.nextToken()) - 1;
			int i2 = Integer.parseInt(tokenizer.nextToken());
			TagWrapper tagWrapper = new TagWrapper(i1, (i2 == 1 ? true : false));
			arrayList.add(tagWrapper);
		}

		return arrayList;
	}

	public static String getFiltersString() {
		ArrayList<TagWrapper> tagWrappers = getFiltersList();
		String filters = "";
		for (TagWrapper tagWrapper : tagWrappers) {
			if (tagWrapper.enabled) {
				filters = filters + "," + (tagWrapper.initialPosition + 1);
			} else {
				break;
			}
		}
		filters = filters.substring(1);
		return filters;
	}

	public static ArrayList<MyRatesDAO> removeFlaggedRates(ArrayList<MyRatesDAO> ratesDAOs) {
		ArrayList<MyRatesDAO> unflaggedRatesDAOs = new ArrayList<MyRatesDAO>();
		if (ratesDAOs != null) {

			for (MyRatesDAO myRatesDAO : ratesDAOs) {
				if (!myRatesDAO.isFlagged) {
					unflaggedRatesDAOs.add(myRatesDAO);
				}
			}
		}
		return unflaggedRatesDAOs;
	}

	/**
	 * testing memory
	 */
	public static void getMemoryStatus() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				for (;;) {
					try {
						Thread.sleep(999);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					log(Debug.getNativeHeapSize() / 1024 + " free :" + Debug.getNativeHeapFreeSize() / 1024 + " alloc : " + Debug.getNativeHeapAllocatedSize() / 1024);
				}
			}
		}).start();

	}
}
