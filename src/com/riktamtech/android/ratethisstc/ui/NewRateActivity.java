package com.riktamtech.android.ratethisstc.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.animations.AppAnimations;
import com.riktamtech.android.ratethisstc.db.AppSession;
import com.riktamtech.android.ratethisstc.db.ServiceConnector;
import com.riktamtech.android.ratethisstc.ui.components.ImageBoxComponent;
import com.riktamtech.android.ratethisstc.ui.components.InputDialog;
import com.riktamtech.android.ratethisstc.ui.components.InputDialog.InputDialogClickListener;
import com.riktamtech.android.ratethisstc.ui.components.TitleComponent;
import com.riktamtech.android.ratethisstc.util.AppDialogs;
import com.riktamtech.android.ratethisstc.util.AppUtils;

public class NewRateActivity extends Activity implements
		InputDialogClickListener {
	private static final int SELECT_PICTURE = 0;
	private static final int CAPTURE_PICTURE = 1;
	protected static final int ADD_DETAILS = 1;
	private TitleComponent titleComponent;
	private LinearLayout contentLayout;
	private ImageBoxComponent imageBoxComp1, imageBoxComp2;
	private ImageBoxComponent curBox;
	private ImageButton addButton;
	private Handler handler;
	private AppDialogs dialogs;
	private final int DIALOG_PROCEED = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.new_rate_new);
		dialogs = new AppDialogs(this);
		handler = new Handler();
		contentLayout = (LinearLayout) findViewById(R.id.NewRateNewLinearLayout02);
		imageBoxComp1 = (ImageBoxComponent) findViewById(R.id.NewRateNewBoxesFoto1);
		imageBoxComp2 = (ImageBoxComponent) findViewById(R.id.NewRateNewBoxesFoto2);
		addButton = (ImageButton) findViewById(R.id.NewRateNewBoxesImageButton01);
		addButton.setEnabled(false);
		addButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NewRateActivity.this,
						NewRateDetailsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}

		});

		registerForBoxClickEvents(imageBoxComp1);
		registerForBoxClickEvents(imageBoxComp2);

		titleComponent = new TitleComponent(this, findViewById(R.id.TitleComp),
				"NewRate!  ", contentLayout);
		titleComponent.imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				titleComponent.imageView.setEnabled(false);
				Animation animation = AppAnimations.pushingDoorOpen();
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {

					}

					@Override
					public void onAnimationRepeat(Animation animation) {

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						titleComponent.toAnimateView
								.setVisibility(View.INVISIBLE);
						// saveNewRate();
						Intent intent = new Intent(NewRateActivity.this,
								MainMenuActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						overridePendingTransition(0, 0);
					}
				});
				titleComponent.toAnimateView.startAnimation(animation);
			}
		});
		AppUtils.resizeNewRateBoxImageViews(3.2, imageBoxComp1.contentView,
				imageBoxComp2.contentView);
		contentLayout.startAnimation(AppAnimations.pullingDoorClose());
		new InitTask(this).execute(imageBoxComp1.contentView.getWidth(),
				imageBoxComp1.contentView.getHeight());
		// loadImages(imageBoxComp1.contentView.getWidth(),
		// imageBoxComp1.contentView.getHeight());
	}

	private static class InitTask extends AsyncTask<Integer, String, String> {
		// ProgressDialog progressDialog;
		Bitmap bitmap1, bitmap2;
		WeakReference<NewRateActivity> activityReference;

		public InitTask(NewRateActivity activity) {
			activityReference = new WeakReference<NewRateActivity>(activity);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			NewRateActivity activity = activityReference.get();
			if (activity == null)
				return;
			// progressDialog = ProgressDialog.show(activity, null,
			// activity.getResources().getString(R.string.PRG_LOADING));

		}

		@Override
		protected String doInBackground(Integer... arg0) {
			String image1Path = AppSession.newRateDAO.getImage1path();
			String image2Path = AppSession.newRateDAO.getImage2path();
			if (image1Path != null) {
				bitmap1 = AppSession.newRateDAO.getImage1Bitmap();
			}
			if (image2Path != null) {
				bitmap2 = AppSession.newRateDAO.getImage2Bitmap();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {

			NewRateActivity activity = activityReference.get();
			if (activity == null)
				return;
			if (bitmap1 != null) {
				activity.imageBoxComp1.contentView.setImageBitmap(bitmap1);
			}
			if (bitmap2 != null) {
				activity.imageBoxComp2.contentView.setImageBitmap(bitmap2);
			}
			activity.refreshAddButton();
			// progressDialog.dismiss();
			if (!ServiceConnector.testConnection(activity.getApplication())) {
				activity.showDialog(activity.DIALOG_PROCEED);
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		super.onCreateDialog(id);
		switch (id) {
		case DIALOG_PROCEED:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(
					getResources().getString(R.string.ALRT_DIALOG_TITLE))
					.setMessage(getResources().getString(R.string.ALRT_PROCEED))
					.setPositiveButton(getResources().getString(R.string.YES),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							})
					.setNegativeButton(getResources().getString(R.string.NO),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									titleComponent.imageView.performClick();
								}
							});
			AlertDialog dialog = builder.create();
			return dialog;

		default:
			break;
		}
		return null;
	}

	private void registerForBoxClickEvents(ImageBoxComponent imageBoxCompx) {
		final ImageBoxComponent imb = imageBoxCompx;

		imb.galleryImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				curBox = imb;
				openGallery();

			}
		});
		imb.cameraImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				curBox = imb;
				startCamera();
			}
		});
	}

	boolean backPressedOnce = false;

	@Override
	public void onBackPressed() {

		if (!backPressedOnce) {
			// super.onBackPressed();
			titleComponent.imageView.performClick();
			backPressedOnce = true;
		}
	}

	private void startCamera() {

		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		// intent.putExtra(MediaStore.EXTRA_OUTPUT,
		// getCacheDir()+"/tmp/"+System.currentTimeMillis()+".jpg");
		startActivityForResult(intent, CAPTURE_PICTURE);
	}

	private void openGallery() {

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PICTURE);
	}

	/**
	 * compresses given bitmap and writes to a file also sets the newrate
	 * attributes in appsession
	 * 
	 * @param bitmap
	 * @param isImage1
	 */
	public void doBitmapProcessing(Bitmap bitmap, boolean isImage1) {
		final Bitmap bitmapF = bitmap;
		new Thread(new Runnable() {
			Bitmap localBitmap;

			@Override
			public void run() {
				try {
					Bitmap resizedBitmap = AppUtils
							.scaleBitmapForUploading(bitmapF);
					bitmapF.recycle();
					if (curBox == imageBoxComp1) {
						File f1 = new File(getExternalCacheDir(),
								AppSession.newRateTempImage1Name);
						resizedBitmap.compress(CompressFormat.JPEG, 65,
								new FileOutputStream(f1));
						AppSession.newRateDAO.setImage1path(f1
								.getAbsolutePath());
						AppSession.newRateDAO.setImage1Bitmap(resizedBitmap);
					} else {
						File f1 = new File(getExternalCacheDir(),
								AppSession.newRateTempImage2Name);
						resizedBitmap.compress(CompressFormat.JPEG, 65,
								new FileOutputStream(f1));
						AppSession.newRateDAO.setImage2path(f1
								.getAbsolutePath());
						AppSession.newRateDAO.setImage2Bitmap(resizedBitmap);
					}
					localBitmap = AppUtils.scaleBitmapToFit(resizedBitmap,
							curBox.contentView.getWidth(),
							curBox.contentView.getHeight());
					handler.post(new Runnable() {
						@Override
						public void run() {
							curBox.contentView.setImageBitmap(localBitmap);
							refreshAddButton();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Uri selectedImageUri = data.getData();
				String selectedImagePath = getPath(selectedImageUri);
				String otherImagePath;
				otherImagePath = (curBox == imageBoxComp1) ? AppSession.newRateDAO
						.getImage2OriginalPath() : AppSession.newRateDAO
						.getImage1OriginalPath();
				if (selectedImagePath.equals(otherImagePath)) {
					dialogs.getAlertDialog(
							getResources().getString(R.string.ALRT_SAME_IMAGE))
							.show();
					return;
				} else {

					if (curBox == imageBoxComp1)
						AppSession.newRateDAO
								.setImage1OriginalPath(selectedImagePath);
					else
						AppSession.newRateDAO
								.setImage2OriginalPath(selectedImagePath);
				}
				Bitmap originalBitmap = BitmapFactory
						.decodeFile(selectedImagePath);
				String txt = (curBox == imageBoxComp1) ? getResources()
						.getString(R.string.INP_CUST_TAG1) : getResources()
						.getString(R.string.INP_CUST_TAG2);
				new InputDialog(this, this, txt, getResources().getString(
						R.string.INP_DONE_BUTTON), getResources().getString(
						R.string.INP_SKIP), 20).show();
				doBitmapProcessing(originalBitmap, (curBox == imageBoxComp1));
			} else if (requestCode == CAPTURE_PICTURE) {
				String txt = (curBox == imageBoxComp1) ? getResources()
						.getString(R.string.INP_CUST_TAG1) : getResources()
						.getString(R.string.INP_CUST_TAG2);
				new InputDialog(this, this, txt, getResources().getString(
						R.string.INP_DONE_BUTTON), getResources().getString(
						R.string.INP_SKIP), 20).show();
				Bitmap originalBitmap = (Bitmap) data.getExtras().get("data");
				doBitmapProcessing(originalBitmap, (curBox == imageBoxComp1));
			} else {
			}
		}

	}

	/**
	 * not required
	 * 
	 * @param i1
	 * @param i2
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unused")
	private boolean areImagesEqual(Bitmap i1, Bitmap i2) {
		if (i1.getHeight() != i2.getHeight())
			return false;
		if (i1.getWidth() != i2.getWidth())
			return false;

		for (int y = 0; y < i1.getHeight(); ++y)
			for (int x = 0; x < i1.getWidth(); ++x)
				if (i1.getPixel(x, y) != i2.getPixel(x, y))
					return false;

		return true;

	}

	private void refreshAddButton() {

		if (imageBoxComp1.hasImage() && imageBoxComp2.hasImage()) {
			addButton.setEnabled(true);
		}

	}

	public String getPath(Uri uri) {
		String selectedImagePath;
		// MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			selectedImagePath = cursor.getString(column_index);
		} else {
			selectedImagePath = null;
		}

		if (selectedImagePath == null) {
			// 2:OI FILE Manager --- call method: uri.getPath()
			selectedImagePath = uri.getPath();
		}
		return selectedImagePath;
	}

	@Override
	public void inputEntered(String text) {
		if (curBox == imageBoxComp1) {
			AppSession.newRateDAO.secTagA = text;
		} else {
			AppSession.newRateDAO.secTagB = text;
		}
	}

}
