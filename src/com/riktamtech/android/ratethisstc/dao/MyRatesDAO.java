package com.riktamtech.android.ratethisstc.dao;

import android.content.Context;
import android.util.Log;

import com.riktamtech.android.ratethisstc.db.AppSession;

public class MyRatesDAO {
	public int answer;
	public String date_posted, primarytag, secondarytag_A, secondarytag_B, duration;
	public int image1, image2, qid, rateId, votes;
	public boolean isCompleted, isFlagged;

	// for local rates
	public String localImage1, localImage2;
	public boolean isLocalRate;

	// for help rates
	public String qidString;

	@Override
	public String toString() {
		return "{" + primarytag + "," + qid + "}";
	}

	public MyRatesDAO(String date_posted, String primarytag, String secondarytag_A, String secondarytag_B, int image1, int image2, String qString, int votes, boolean isCompleted, boolean isFlagged, boolean isLocalFile, String duration) {
		super();
		this.date_posted = date_posted;
		this.primarytag = primarytag;
		this.secondarytag_A = secondarytag_A;
		this.secondarytag_B = secondarytag_B;
		this.image1 = image1;
		this.image2 = image2;
		this.qidString = qString;
		this.votes = votes;
		this.isCompleted = isCompleted;
		this.isFlagged = isFlagged;
		this.duration = duration;
	}

	/**
	 * for displaying the newrate immediately after posting post request
	 * 
	 * @param context
	 * @param newRateDAO
	 */
	public MyRatesDAO(Context context, NewRateDAO newRateDAO) {
		log(newRateDAO.primaryTagId);
		log(AppSession.primaryTagsArrayList.get(newRateDAO.primaryTagId));
		String[] durations=new String[] {"0 hours","0 hours","0 hours", "1 hours","3 hours","7 hours","23 hours","47 hours","167 hours","8759 hours"};
		init(newRateDAO.datePosted, AppSession.primaryTagsArrayList.get(newRateDAO.primaryTagId), newRateDAO.secTagA, newRateDAO.secTagB, -25, -25, 0, 0, 0, false, false, newRateDAO.getImage1path(), newRateDAO.getImage2path(), true,durations[newRateDAO.qDurId]);
	}

	private void log(Object string) {
		Log.d("My rates dao", string.toString());
	}

	public void init(String date_posted, String primarytag, String secondarytag_A, String secondarytag_B, int qid, int rateId, int votes, int im1, int im2, boolean isCompleted, boolean isFlagged, String localImage1, String localImage2, boolean isLocalRate,String duration) {
		this.date_posted = date_posted;
		this.primarytag = primarytag;
		this.secondarytag_A = secondarytag_A;
		this.secondarytag_B = secondarytag_B;
		this.qid = qid;
		this.rateId = rateId;
		this.votes = votes;
		this.isCompleted = isCompleted;
		this.isFlagged = isFlagged;
		this.localImage1 = localImage1;
		this.localImage2 = localImage2;
		this.isLocalRate = isLocalRate;
		this.duration=duration;
	}

	public String getDuration() {
		log(qid+" duration is "+duration);
		int hours = 0;
		int index = duration.indexOf(" hours");
		if (index > 0) {
			hours = Integer.parseInt(duration.substring(0, index));
		}
		if (hours <= 0) {
			return "Within an hour";
		} else if (hours > 23) {
			int days = hours / 24, remHours = hours % 24;
			String str = days+((days>1)?" days":" day");
			if (remHours>0) {
				str=str+", "+remHours+((remHours>1)?" hours":" hour");
			}
			str+=" remaining";
			return str;
		}
		else {
			String string=hours+((hours>1)?" hours":" hour");
			string+=" remaining";
			return string;
		}
		
	}
	
	
	public float getPercent() {
		float percent = -1.0f;
		if (image1 == 0 && image2 > 0)
			percent = 0;
		else if (image1 > 0 && image2 == 0) {
			percent = 1.0f;

		} else if (image1 == 0 && image2 == 0) {
			percent = 0.5f;
		} else {
			percent = image1 / (float) (image1 + image2);
		}
		return percent;
	}
	
}
