package com.riktamtech.android.ratethisstc.dao;

import android.content.Context;

import com.riktamtech.android.ratethisstc.R;

public class RateItRate {
	// initialized from webservice
	public String qid, duration;
	public int primarytagId;
	public int userAnswer;
	/**
	 * the time at which this object is created used to check whether question expired or not
	 */
	private long createdTime;
	Context ctx;

	public RateItRate(Context ctx, String qid, String duration, String primarytagId) {
		super();
		this.ctx = ctx;
		this.qid = qid;
		this.duration = duration;
		this.primarytagId = Integer.parseInt(primarytagId) - 1;
		this.createdTime = System.currentTimeMillis();

	}

	public boolean isExpired() {
		// TODO calculate whether question is over or not
		long timeElapsed = (System.currentTimeMillis() - createdTime)/1000;
		String durationStringInPieces[] = duration.split("[\\s,]");
		//converting hrs-mins-sec to milliseconds
		long durationInSeconds = (Integer.parseInt(durationStringInPieces[0]) * 60 * 60 + Integer.parseInt(durationStringInPieces[2]) * 60 + Integer
				.parseInt(durationStringInPieces[4]));
		boolean isRateExpired = (timeElapsed > durationInSeconds) ? true : false;
		return isRateExpired;
	}

	public String getThumbnailImageLink1() {
		String ss = "";
		//log("thumb1  - qid - "+qid+"  "+ctx);
		ss = ctx.getResources().getString(R.string.WSImages) + qid + "_1_1.jpg";
		//log("thumb1  - qid("+qid+")  link - "+ss);
		return ss;
	}

	public String getThumbnailImageLink2() {
		String ss = "";
		ss = ctx.getResources().getString(R.string.WSImages) + qid + "_1_2.jpg";
		//log("thumb2  - qid("+qid+")  link - "+ss);
		return ss;
	}

	public String getFullImageLink1() {
		String ss = ctx.getResources().getString(R.string.WSImages) + qid + "_0_1.jpg";
		return ss;
	}

	public String getFullImageLink2() {
		String ss = ctx.getResources().getString(R.string.WSImages) + qid + "_0_2.jpg";
		return ss;
	}

	public String getIconLink1() {
		String ss = ctx.getResources().getString(R.string.WSImages) + qid + "_2_1.jpg";
		return ss;
	}

	public String getIconLink2() {
		String ss = ctx.getResources().getString(R.string.WSImages) + qid + "_2_1.jpg";
		return ss;
	}

	@Override
	public String toString() {
		return qid;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o instanceof RateItRate && ((RateItRate) o).qid.equals(this.qid)) {
			return true;
		}
		return false;

	}
}
