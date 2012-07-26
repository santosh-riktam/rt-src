package com.riktamtech.android.ratethisstc.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.riktamtech.android.ratethisstc.db.AppSession;

public class PrefsManager
{
	private final String PREF_USER = "signedInUser";
	private final String PREF_PUSH_NTF = "push notification", PREF_AUTO_FB = "auto fb", PREF_AUTO_TWT = "auto twitter", PREF_BG_LDNG = "bg loading", PREF_DEF_LOCATION1 = "def location1", PREF_DEF_LOCATION2 = "def location2", PREF_DEF_LOCATION3 = "def location3", PREF_DEF_VOTING_DUR = "def vote dur", PREF_DEF_LOADING_RATES = "def loading rates", PREF_DEF_AGE_GROUP="age group",PREF_TAG_LIST = "tag list";
	private final String PREFS_NAME = "com.riktamtech.android.ratethisstc.prefs";
	private final String PREF_FB_ACC_TOKEN="fb acc tkn",PREF_FB_EX="fb expiry",PREF_TW_UNAME="Tw signedInUser",PREF_TW_PWD="Tw pwd";
	
	//for  saving new rate state
	private final String PREF_NEWRATE_POSTED="newRatePosted",PREF_NEWRATE_OBG="newRateObject";
	
	
	private String user, tagList;
	private boolean pushNotification, autoShareFb, autoShareTw, bgLoading;
	private int defLocation1, defLocation2, defLocation3, defVotingDuration, defLoadingRates,defAgeGroup;
	
	private String fbAccessToken,fbExpiry,twUserName,twPassword;
	
	private String newRateObjectString;
	
	
 	public void commit( boolean pushNotification, boolean autoShareFb, boolean autoShareTw, boolean bgLoading, int defLocation1, int defLocation2, int defLocation3, int defVotingDuration, int defLoadingRates,int defAgeGroup)
	{
		
		this.pushNotification = pushNotification;
		this.autoShareFb = autoShareFb;
		this.autoShareTw = autoShareTw;
		this.bgLoading = bgLoading;
		this.defLocation1 = defLocation1;
		this.defLocation2 = defLocation2;
		this.defLocation3 = defLocation3;
		this.defVotingDuration = defVotingDuration;
		this.defLoadingRates = defLoadingRates;
		this.defAgeGroup=defAgeGroup;
		commit();
	}

	SharedPreferences sharedPreferences;

	public PrefsManager(Context ctx)
	{
		sharedPreferences = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		user=sharedPreferences.getString(PREF_USER, "");
		tagList = sharedPreferences.getString(PREF_TAG_LIST, "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24");
		pushNotification = sharedPreferences.getBoolean(PREF_PUSH_NTF, false);
		autoShareFb = sharedPreferences.getBoolean(PREF_AUTO_FB, false);
		autoShareTw = sharedPreferences.getBoolean(PREF_AUTO_TWT, false);
		bgLoading = sharedPreferences.getBoolean(PREF_BG_LDNG, false);
		defLoadingRates = sharedPreferences.getInt(PREF_DEF_LOADING_RATES, 0);
		defLocation1 = sharedPreferences.getInt(PREF_DEF_LOCATION1, 0);
		defLocation2 = sharedPreferences.getInt(PREF_DEF_LOCATION2, 0);
		defLocation3 = sharedPreferences.getInt(PREF_DEF_LOCATION3, 0);
		defVotingDuration = sharedPreferences.getInt(PREF_DEF_VOTING_DUR, 0);
		defAgeGroup=sharedPreferences.getInt(PREF_DEF_AGE_GROUP, 0);
		
		fbAccessToken=sharedPreferences.getString(PREF_FB_ACC_TOKEN,null);
		fbExpiry=sharedPreferences.getString(PREF_FB_EX	, null);
		
		twUserName=sharedPreferences.getString(PREF_TW_UNAME, null);
		twPassword=sharedPreferences.getString(PREF_TW_PWD, null);
		
		sharedPreferences.getInt(PREF_NEWRATE_POSTED, AppSession.NEW);
		newRateObjectString=sharedPreferences.getString(PREF_NEWRATE_OBG, null);
		
	}

	
	private void commit()
	{
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_USER, user);
		editor.putBoolean(PREF_PUSH_NTF, pushNotification);
		editor.putBoolean(PREF_AUTO_FB, autoShareFb);
		editor.putBoolean(PREF_AUTO_TWT, autoShareTw);
		editor.putBoolean(PREF_BG_LDNG, bgLoading);
		editor.putInt(PREF_DEF_LOCATION1, defLocation1);
		editor.putInt(PREF_DEF_LOCATION2, defLocation2);
		editor.putInt(PREF_DEF_LOCATION3, defLocation3);
		editor.putInt(PREF_DEF_VOTING_DUR, defVotingDuration);
		editor.putInt(PREF_DEF_LOADING_RATES, defLoadingRates);
		editor.putInt(PREF_DEF_AGE_GROUP, defAgeGroup);
		editor.putString(PREF_TAG_LIST, tagList);
		editor.commit();
	}

	public String getTagList()
	{
		return tagList;
	}

	public boolean isPushNotification()
	{
		return pushNotification;
	}

	public boolean isAutoShareFb()
	{
		return autoShareFb;
	}

	public boolean isAutoShareTw()
	{
		return autoShareTw;
	}

	public boolean isBgLoading()
	{
		return bgLoading;
	}

	public int getDefLocation1()
	{
		return defLocation1;
	}

	public int getDefLocation2()
	{
		return defLocation2;
	}

	public int getDefLocation3()
	{
		return defLocation3;
	}

	public int getDefVotingDuration()
	{
		return defVotingDuration;
	}

	public int getDefLoadingRates()
	{
		return defLoadingRates;
	}

	public String getUser()
	{
		return user;
	}
	public void setUser(String user)
	{
		this.user = user;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_USER, user);
		editor.commit();
	}

	public void setTagList(String s)
	{
		tagList=s;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_TAG_LIST, tagList);
		editor.commit();
	}


	public String getFbAccessToken()
	{
		return fbAccessToken;
	}


	public int getDefAgeGroup()
	{
		return defAgeGroup;
	}
	
	public String getFbExpiry()
	{
		return fbExpiry;
	}


	public String getTwUserName()
	{
		return twUserName;
	}


	public String getTwPassword()
	{
		return twPassword;
	}

	
	public void setFacebookCredentials(String accessToken,String expiry)
	{
		fbAccessToken=accessToken;
		fbExpiry=expiry;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_FB_ACC_TOKEN, fbAccessToken);
		editor.putString(PREF_FB_EX, fbExpiry);
		editor.commit();
	}
	
	public void setTwitterLoginDetails(String u,String p)
	{
		twUserName=u;twPassword=p;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PREF_TW_UNAME, twUserName);
		editor.putString(PREF_TW_PWD, twPassword);
		editor.commit();
	}

	public String getnewRateObjectDao() {
		return newRateObjectString;
	}
	
	/**
	 * for saving
	 * @param isPosted
	 * @param cust1
	 * @param cust2
	 * @return
	 */
	public boolean setNewRateDetails(String s,int isPosted) {
		newRateObjectString=s;
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_NEWRATE_POSTED, isPosted);
		editor.putString(PREF_NEWRATE_OBG, newRateObjectString);
		return editor.commit();
		
	}
	
	/**
	 * called on succesful upload with true param
	 * @param b
	 * @return
	 */
	
	public boolean setNewRatePosted(int b) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(PREF_NEWRATE_POSTED, b);
		return editor.commit();
	}

	public void clearAllPrefs()
	{
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}
}
