package com.riktamtech.android.ratethisstc.dao;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.annotations.Expose;

public class NewRateDAO {
	@Expose
	private String image1path, image2path;
	@Expose
	private String image1OriginalPath,image2OriginalPath;
	@Expose
	public String  secTagA, secTagB, lDistance, datePosted, lat, lon;
	@Expose
	public int primaryTagId, ageId, qDurId, locationType, locationType2, locationType3;
	
	private Bitmap image1Bitmap, image2Bitmap;

	public NewRateDAO(String image1, String image2, String secTagA, String secTagB, String lDistance, String datePosted, String lat, String lon, int primaryTagId, int ageId,
			int qDurId, int locationType) {
		super();
		setImage1path(image1);
		setImage2path(image2);
		this.secTagA = secTagA;
		this.secTagB = secTagB;
		this.lDistance = lDistance;
		this.datePosted = datePosted;
		this.lat = lat;
		this.lon = lon;
		this.primaryTagId = primaryTagId;
		this.ageId = ageId;
		this.qDurId = qDurId;
		this.locationType = locationType;

	}

	public NewRateDAO() {
		primaryTagId = ageId = qDurId = locationType = locationType2 = locationType3 = -1;
	}

	/**
	 * to create duplicate of an object
	 * 
	 * @param n
	 */
	public NewRateDAO(NewRateDAO n) {
		super();
		setImage1path(n.image1path);
		setImage2path(n.image2path);
		image1OriginalPath=n.image1OriginalPath;
		image2OriginalPath=n.image2OriginalPath;
		this.secTagA = n.secTagA;
		this.secTagB = n.secTagB;
		this.lDistance = n.lDistance;
		this.datePosted = n.datePosted;
		this.lat = n.lat;
		this.lon = n.lon;
		this.primaryTagId = n.primaryTagId;
		this.ageId = n.ageId;
		this.qDurId = n.qDurId;
		this.locationType = n.locationType;
		this.locationType2 = n.locationType2;
		this.locationType3 = n.locationType3;
	}

	/**
	 * 
	 * @return bitmap object if its null, its decodes the file and returns bitmap objet
	 */
	public Bitmap getImage1Bitmap() {
		if (image1Bitmap == null) {
			image1Bitmap = BitmapFactory.decodeFile(image1path);
		}

		return image1Bitmap;
	}

	
	public void setImage1Bitmap(Bitmap image1Bitmap) {
		this.image1Bitmap = image1Bitmap;
	}

	public void setImage2Bitmap(Bitmap image2Bitmap) {
		this.image2Bitmap = image2Bitmap;
	}

	/**
	 * 
	 * @return bitmap object if its null, its decodes the file and returns bitmap objet
	 */
	public Bitmap getImage2Bitmap() {
		if (image2Bitmap == null) {
			image2Bitmap = BitmapFactory.decodeFile(image2path);
		}
		return image2Bitmap;
	}

	public String getImage1path() {
		return image1path;
	}

	
	
	public void setImage1path(String image1path) {
		this.image1path = image1path;
				
	}

	public String getImage2path() {
		return image2path;
	}

	public void setImage2path(String image2path) {
		this.image2path = image2path;
	}

	public String getImage1OriginalPath() {
		return image1OriginalPath;
	}

	public void setImage1OriginalPath(String image1OriginalPath) {
		this.image1OriginalPath = image1OriginalPath;
	}

	public String getImage2OriginalPath() {
		return image2OriginalPath;
	}

	public void setImage2OriginalPath(String image2OriginalPath) {
		this.image2OriginalPath = image2OriginalPath;
	}

}
