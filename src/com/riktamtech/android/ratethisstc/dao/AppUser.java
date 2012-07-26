package com.riktamtech.android.ratethisstc.dao;

import java.net.URLEncoder;

public class AppUser {
	// for edit texts
	public String idString, first_nameString, last_nameString, email_idString, user_nameString, passwordString, device_tokenString, default_locationString, dob, filtersString;

	// for drop downs
	public int country_idInt, prioritizing_ratesInt, default_age_groupInt, sexInt, default_voting_durationInt, miles_kilometersInt;

	// for check boxes
	public boolean push_notificationsBoolean, share_fbBoolean, sh_twitterBoolean, background_on_3gBoolean;

	
	//show demo for first time user
	public boolean showDemo=false;
	
	public AppUser() {

	}

	// used by login
	public AppUser(String idString, String first_nameString, String last_nameString, String email_idString, String user_nameString, String passwordString,
			String device_tokenString, String default_locationString, String dob, String filtersString, int country_idInt, int prioritizing_ratesInt, int default_age_groupInt,
			int sexInt, int default_voting_durationInt, int miles_kilometersInt, boolean push_notificationsBoolean, boolean share_fbBoolean, boolean sh_twitterBoolean,
			boolean background_on_3gBoolean,boolean showDemo) {
		super();
		this.idString = idString;
		this.first_nameString = first_nameString;
		this.last_nameString = last_nameString;
		this.email_idString = email_idString;
		this.user_nameString = user_nameString;
		this.passwordString = passwordString;
		this.device_tokenString = device_tokenString;
		this.default_locationString = default_locationString;
		this.dob = dob;
		this.filtersString = filtersString;
		this.country_idInt = country_idInt;
		this.prioritizing_ratesInt = prioritizing_ratesInt;
		this.default_age_groupInt = default_age_groupInt;
		this.sexInt = sexInt;
		this.default_voting_durationInt = default_voting_durationInt;
		this.miles_kilometersInt = miles_kilometersInt;
		this.push_notificationsBoolean = push_notificationsBoolean;
		this.share_fbBoolean = share_fbBoolean;
		this.sh_twitterBoolean = sh_twitterBoolean;
		this.background_on_3gBoolean = background_on_3gBoolean;
		this.showDemo=showDemo;
		
	}

	public String getEncodedFieldsForRegistration() {
		String sexString = sexInt == 0 ? "Male" : "Female";
		String string = "device_token=" + URLEncoder.encode(device_tokenString) + "&first_name=" + URLEncoder.encode(first_nameString) + "&last_name="
				+ URLEncoder.encode(last_nameString) + "&user_name=" + URLEncoder.encode(user_nameString) + "&sex=" + sexString + "&dob=" + URLEncoder.encode(dob) + "&email_id="
				+ URLEncoder.encode(email_idString) + "&password=" + URLEncoder.encode(passwordString) + "&country_id=" + (country_idInt + 1);
		return string;
	}

	public String getEncodedFieldsForEditProfile() {
		String sexString = sexInt == 0 ? "Male" : "Female";
		String string = "first_name=" + URLEncoder.encode(first_nameString) + "&last_name=" + URLEncoder.encode(last_nameString) + "&sex=" + sexString + "&dob="
				+ URLEncoder.encode(dob) + "&email_id=" + URLEncoder.encode(email_idString) + "&password=" + URLEncoder.encode(passwordString) + "&country_id="
				+ (country_idInt + 1) + "&user_id=" + idString;
		return string;
	}

	public int getDefLocation1() {
		if (default_locationString.contains("veryone"))
			return 0;
		else if (default_locationString.contains("ithin"))
			return 1;
		else
			return 2;

	}

	// For settings screen
	public AppUser(String idString, String default_locationString, String filtersString, int prioritizing_ratesInt, int default_age_groupInt, int default_voting_durationInt,
			int miles_kilometersInt, boolean push_notificationsBoolean, boolean share_fbBoolean, boolean sh_twitterBoolean, boolean background_on_3gBoolean) {
		super();
		this.idString = idString;
		this.default_locationString = default_locationString;
		this.filtersString = filtersString;
		this.prioritizing_ratesInt = prioritizing_ratesInt;
		this.default_age_groupInt = default_age_groupInt;
		this.default_voting_durationInt = default_voting_durationInt;
		this.miles_kilometersInt = miles_kilometersInt;
		this.push_notificationsBoolean = push_notificationsBoolean;
		this.share_fbBoolean = share_fbBoolean;
		this.sh_twitterBoolean = sh_twitterBoolean;
		this.background_on_3gBoolean = background_on_3gBoolean;
	}

	public String getDefLocation2Text() {
		if (getDefLocation1() == 1) {
			// string will be like with in 10 mile radius
			String[] split = default_locationString.split(" ");
			return split[1];
		} else if (getDefLocation1() == 2) {
			// string will be country name
			return default_locationString;
		} else {
			return "";
		}
	}

	public String getDefLocation3Text() {
		if (getDefLocation1() == 1) {
			String[] split = default_locationString.split(" ");
			// gota find a proper fix for this
			try {
				String ssString = split[2];
				return ssString;
			} catch (Exception e) {
				e.printStackTrace();
				
				return "";
			}
		} else {
			return "";

		}
	}

	// used for edit profile
	public AppUser(String idString, String first_nameString, String last_nameString, String email_idString, String passwordString, String dob, int country_idInt, int sexInt) {
		super();
		this.idString = idString;
		this.first_nameString = first_nameString;
		this.last_nameString = last_nameString;
		this.email_idString = email_idString;
		this.passwordString = passwordString;
		this.dob = dob;
		this.country_idInt = country_idInt;
		this.sexInt = sexInt;
	}

	public AppUser(String first_nameString, String last_nameString, String email_idString, String user_nameString, String passwordString, String device_tokenString, String dob,
			int country_idInt, int sexInt) {
		super();
		this.first_nameString = first_nameString;
		this.last_nameString = last_nameString;
		this.email_idString = email_idString;
		this.user_nameString = user_nameString;
		this.passwordString = passwordString;
		this.device_tokenString = device_tokenString;
		this.dob = dob;
		this.country_idInt = country_idInt;
		this.sexInt = sexInt;
	}

	/**
	 * 
	 * @param u
	 *            user to compare
	 * @return true if settings are identical ,false other wise
	 */
	public boolean hasSameSettings(AppUser u) {
		boolean allEqual = (push_notificationsBoolean == u.push_notificationsBoolean) && (share_fbBoolean == u.share_fbBoolean) && (sh_twitterBoolean == u.sh_twitterBoolean)
				&& (background_on_3gBoolean == u.background_on_3gBoolean) && (prioritizing_ratesInt == u.prioritizing_ratesInt)
				&& (default_locationString.equals(u.default_locationString)) && (default_voting_durationInt == u.default_voting_durationInt)
				&& (default_age_groupInt == u.default_age_groupInt) && (filtersString.equals(u.filtersString));
		return allEqual;
	}

}
