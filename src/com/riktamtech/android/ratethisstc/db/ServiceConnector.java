package com.riktamtech.android.ratethisstc.db;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.dao.AchievementDAO;
import com.riktamtech.android.ratethisstc.dao.AppUser;
import com.riktamtech.android.ratethisstc.dao.MyRatesDAO;
import com.riktamtech.android.ratethisstc.dao.RateItRate;
import com.riktamtech.android.ratethisstc.exceptions.EditProfileException;
import com.riktamtech.android.ratethisstc.exceptions.LoginException;
import com.riktamtech.android.ratethisstc.exceptions.RateItException;
import com.riktamtech.android.ratethisstc.exceptions.RatesException;
import com.riktamtech.android.ratethisstc.exceptions.RegistrationException;
import com.riktamtech.android.ratethisstc.exceptions.WebServiceException;
import com.riktamtech.android.ratethisstc.util.AppUtils;

public class ServiceConnector {

	private static JSONObject callWebService(Context ctx, String str) throws WebServiceException {
		String responseString = "";
		JSONObject jsonObject;
		try {
			URL url = new URL(str);
			log(str);
			Scanner scanner = new Scanner(url.openStream());
			while (scanner.hasNext())
				responseString += scanner.nextLine();
			jsonObject = new JSONObject(responseString);
			scanner.close();
		} catch (Exception e) {
			isInConnectedStatus=false;
			ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI), mobileNetworkInfo = connectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (!(wifiNetworkInfo.isConnectedOrConnecting() || mobileNetworkInfo.isConnectedOrConnecting())) {
				throw new WebServiceException(new Exception(ctx.getResources().getString(R.string.ALRT_NOT_CONNECTED)));
			}
			throw new WebServiceException(e);
		}
		return jsonObject;
	}

	private static boolean isInConnectedStatus = true;

	public static boolean testConnection(Context ctx) {
		if (isInConnectedStatus)
			return true;

		ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI), mobileNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (!(wifiNetworkInfo.isConnectedOrConnecting() || mobileNetworkInfo.isConnectedOrConnecting())) {
			isInConnectedStatus = false;
			return false;
		} else {
			URL url;
			try {
				url = new URL(ctx.getResources().getString(R.string.WSRoot) + "Login?user_name=" + URLEncoder.encode("S") + "&password=" + URLEncoder.encode("123456"));
				log(url);
				url.openConnection().getInputStream();
			} catch (Exception e) {
				e.printStackTrace();
				isInConnectedStatus = false;
				return false;
			}
			isInConnectedStatus = true;
			return true;
		}

	}

	public static boolean isConnected(Context ctx) {
		if (isInConnectedStatus)
			return true;
		ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI), mobileNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (!(wifiNetworkInfo.isConnectedOrConnecting() || mobileNetworkInfo.isConnectedOrConnecting())) {
			isInConnectedStatus = false;
			return false;
		} else {
			isInConnectedStatus = true;
			return true;
		}
	}

	public static AppUser login(Context ctx, String username, String password, String deviceToken) throws LoginException, WebServiceException {

		String serviceUrl = ctx.getResources().getString(R.string.WSRoot) + "Login?user_name=" + URLEncoder.encode(username) + "&password=" + URLEncoder.encode(password)
				+ "&device_token=" + URLEncoder.encode(deviceToken);
		JSONObject resultJSON = callWebService(ctx, serviceUrl);
		String dialogMessaage = "";

		String responseCode;
		try {
			responseCode = resultJSON.getString("ResponseCode");

			if (responseCode.equals("SUCCESS")) {

				JSONObject userInfo = resultJSON.getJSONObject("ResponseData");

				// initialising variables , conversion from WS
				boolean background_on_3gBoolean = userInfo.getString("background_on_3g").equalsIgnoreCase("On") ? true : false;
				String idString = userInfo.getString("id");
				String first_nameString = userInfo.getString("first_name");
				String last_nameString = userInfo.getString("last_name");
				String email_idString = userInfo.getString("email_id");
				String user_nameString = username;
				String passwordString = password;
				String device_tokenString = deviceToken;
				String default_locationString = userInfo.getString("default_location");
				String dob = userInfo.getString("dob");
				String filtersString = userInfo.getString("filters");
				int country_idInt = Integer.parseInt(userInfo.getString("country_id")) - 1;
				int prioritizing_ratesInt = Integer.parseInt(userInfo.getString("prioritizing_rates"));
				int default_age_groupInt = AppSession.ageGroupArrayList.indexOf(userInfo.getString("default_age_group"));
				int sexInt = userInfo.getString("sex").equals("Male") ? 0 : 1;
				int default_voting_durationInt = AppSession.votingDurationArrayList.indexOf(userInfo.getString("default_voting_duration"));
				int miles_kilometersInt = Integer.parseInt(userInfo.getString("miles_kilometers"));
				boolean push_notificationsBoolean = userInfo.getString("push_notifications").equals("1") ? true : false;
				int facebookTwStatus = Integer.parseInt(userInfo.getString("share_fb_or_twitter"));
				boolean share_fbBoolean = facebookTwStatus < 2 ? false : true;
				boolean sh_twitterBoolean = (facebookTwStatus == 1 || facebookTwStatus == 3) ? true : false;
				boolean showDemo = userInfo.getString("demo").equals("0") ? true : false;
				AppUser appUser = new AppUser(idString, first_nameString, last_nameString, email_idString, user_nameString, passwordString, device_tokenString,
						default_locationString, dob, filtersString, country_idInt, prioritizing_ratesInt, default_age_groupInt, sexInt, default_voting_durationInt,
						miles_kilometersInt, push_notificationsBoolean, share_fbBoolean, sh_twitterBoolean, background_on_3gBoolean, showDemo);
				return appUser;
			} else if (responseCode.equals("FAILURE")) {
				resultJSON = resultJSON.getJSONObject("ResponseData");
				dialogMessaage = resultJSON.getString("Message");
				throw new LoginException(dialogMessaage);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}
		return null;
	}

	public static String forgotPassword(Context ctx, String username) throws WebServiceException {
		String responseMessage = null;
		try {
			JSONObject jsonObject = callWebService(ctx, ctx.getResources().getString(R.string.WSRoot) + "ForgotPassword?userEmail=" + username);
			responseMessage = jsonObject.getJSONObject("ResponseData").getString("Message");
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return responseMessage;
	}

	public static String register(Context ctx, AppUser u) throws WebServiceException, RegistrationException {
		String service = ctx.getResources().getString(R.string.WSRoot) + "Register?" + u.getEncodedFieldsForRegistration();
		JSONObject resultJSON = callWebService(ctx, service);
		try {
			if (resultJSON.getString("ResponseCode").equals("FAILURE")) {

				String message = resultJSON.getJSONObject("ResponseData").getString("Message");
				throw new RegistrationException(message);
			} else if (resultJSON.getString("ResponseCode").equals("SUCCESS")) {
				return resultJSON.getJSONObject("ResponseData").getString("id");
			}
		} catch (JSONException e) {
			throw new WebServiceException(e);
		}
		return null;
	}

	public static ArrayList<RateItRate> rateitRequest(Context ctx, String qidList) throws WebServiceException, RateItException {
		String lat = "37.0625", lon = "-95.677068";
		Location currentUserLocation = AppSession.currentUserLocation;
		if (currentUserLocation != null) {
			lat = AppSession.currentUserLocation.getLatitude() + "";
			lon = AppSession.currentUserLocation.getLongitude() + "";
		}

		try {
			AppUser user = AppSession.signedInUser;
			int settings = user.prioritizing_ratesInt;

			String filters = AppUtils.getFiltersString();

			String WSString = ctx.getResources().getString(R.string.WSRoot) + "RateIt2?user_id=" + AppSession.signedInUser.idString + "&latitude=" + lat + "&longitude=" + lon
					+ "&tag_list=" + filters + "&settings=" + settings;
			if (qidList != null && !qidList.equals(" ")) {
				WSString = WSString + "&qid_list=" + qidList;
			}
			JSONObject resultJsonObject = callWebService(ctx, WSString);

			ArrayList<RateItRate> items = new ArrayList<RateItRate>();

			String responseCode = resultJsonObject.getString("ResponseCode");
			if (responseCode.equals("SUCCESS")) {
				JSONArray array = resultJsonObject.getJSONArray("ResponseData");
				for (int i = 0; i < array.length(); i++) {
					JSONObject jsonObject = array.getJSONObject(i);
					RateItRate rateItDAO = new RateItRate(ctx, jsonObject.getString("qid"), jsonObject.getString("duration"), jsonObject.getString("primarytag_id"));
					items.add(rateItDAO);
				}
				return items;
			} else {
				throw new RateItException(resultJsonObject.getString("Message"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}

	}

	public static ArrayList<MyRatesDAO> myRatesRequest(Context ctx, int qid, boolean isMyRates) throws WebServiceException, RatesException {
		String link = "";
		if (isMyRates) {

			link = ctx.getResources().getString(R.string.WSRoot) + "MyRates?user_id=" + AppSession.signedInUser.idString + "&qid=" + qid;
		} else {

			link = ctx.getResources().getString(R.string.WSRoot) + "Rated?user_id=" + AppSession.signedInUser.idString + "&id=" + qid;
		}
		log(link);
		JSONObject responseJsonObject = callWebService(ctx, link);
		try {
			if (responseJsonObject.getString("ResponseCode").equals("SUCCESS")) {
				JSONArray jsonArray = responseJsonObject.getJSONArray("ResponseData");
				ArrayList<MyRatesDAO> listItems = new ArrayList<MyRatesDAO>();
				for (int i = 0; i < jsonArray.length(); i++) {
					MyRatesDAO myRatesRowDAO = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), MyRatesDAO.class);
					if (myRatesRowDAO.secondarytag_A == null)
						myRatesRowDAO.secondarytag_A = " ";
					if (myRatesRowDAO.secondarytag_B == null)
						myRatesRowDAO.secondarytag_B = " ";
					listItems.add(myRatesRowDAO);

				}
				log(listItems);
				return listItems;
			} else {
				throw new RatesException("no more rates");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}

	}

	public static ArrayList<MyRatesDAO> getHelpfulRates(boolean isMyRates) {
		ArrayList<MyRatesDAO> rates = new ArrayList<MyRatesDAO>();
		MyRatesDAO r1 = null;
		if (!isMyRates) {
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 127, 73, "Rated_Helpful1", 200, false, false, false, "0 hours,0 minutes,10 seconds");
			rates.add(r1);
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 256, 768, "Rated_Helpful3", 1024, false, true, false, "10 hours,10 minutes,10 seconds");
			rates.add(r1);
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 150, 150, "Rated_Helpful2", 300, true, false, false, "10 hours,10 minutes,10 seconds");
			rates.add(r1);

		} else {
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 127, 73, "Rates_Helpful_Rate1", 200, true, false, false, "10 hours,10 minutes,10 seconds");
			rates.add(r1);
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 0, 0, "Rates_Helpful_Rate2", 0, false, false, false, "48 hours,10 minutes,10 seconds");
			rates.add(r1);
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 150, 150, "Rates_Helpful_Rate3", 300, true, false, false, "-10 hours,-10 minutes,-10 seconds");
			rates.add(r1);
			r1 = new MyRatesDAO("04 jun 2011", "Education", "A", "B", 0, 0, "Rates_Helpful_Rate4", 0, false, true, false, "10 hours,10 minutes,10 seconds");
			rates.add(r1);
		}
		return rates;
	}

	public static ArrayList<RateItRate> getRateItHelpfulRates(Context ctx) {
		ArrayList<RateItRate> arrayList = new ArrayList<RateItRate>();
		RateItRate r1 = null;
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate1", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate2", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate3", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate4", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate5", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate6", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		r1 = new RateItRate(ctx, "RateIt_Helpful_Rate7", "10 hours,10 minutes,10 seconds", "7");
		arrayList.add(r1);
		return arrayList;
	}

	public static AchievementDAO getAchievements(Context ctx, String userId) throws WebServiceException {
		String WSLocation = ctx.getResources().getString(R.string.WSRoot) + "Achievements?user_id=" + userId;
		JSONObject resultJsonObject = callWebService(ctx, WSLocation);
		try {
			if (resultJsonObject.getString("ResponseCode").equals("SUCCESS")) {
				AchievementDAO achievementDAO = new AchievementDAO();
				resultJsonObject = resultJsonObject.getJSONObject("ResponseData");
				if (resultJsonObject.has("Badge")) {
					achievementDAO.badge = resultJsonObject.getString("Badge");
				}
				achievementDAO.winningRate = resultJsonObject.getInt("winningRate");
				achievementDAO.drawRate = resultJsonObject.getInt("drawRate");
				achievementDAO.totalRated = resultJsonObject.getInt("totalRated");
				achievementDAO.totalUploaded = resultJsonObject.getInt("totalUploaded");
				return achievementDAO;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}
		return null;

	}

	public static String editProfile(Context ctx, AppUser user) throws WebServiceException, EditProfileException {

		String webserviceString = ctx.getResources().getString(R.string.WSRoot) + "EditProfile?" + user.getEncodedFieldsForEditProfile();
		JSONObject resultJSON = callWebService(ctx, webserviceString);
		try {
			if (resultJSON.getString("ResponseCode").equals("FAILURE")) {

				String message = resultJSON.getJSONObject("ResponseData").getString("Message");
				throw new EditProfileException(message);
			} else if (resultJSON.getString("ResponseCode").equals("SUCCESS")) {
				return resultJSON.getJSONObject("ResponseData").getString("Message");
			}
		} catch (JSONException e) {
			throw new WebServiceException(e);
		}
		return null;
	}

	public static Hashtable<String, String> getFbDetails(Context ctx, String userId) throws WebServiceException {
		Hashtable<String, String> hashtable = new Hashtable<String, String>();
		String ws = ctx.getResources().getString(R.string.WSRoot) + "GetFbDetails?user_id=" + userId;
		JSONObject responseObject = callWebService(ctx, ws);
		try {
			String responseCode = responseObject.getString("ResponseCode");
			if (responseCode.equals("SUCCESS")) {
				responseObject = responseObject.getJSONObject("ResponseData");
				hashtable.put("token", responseObject.getString("access_token"));
				hashtable.put("expiry", responseObject.getString("expiry"));
				return hashtable;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}
		return hashtable;
	}

	public static String setFbDetails(Context ctx, String userId, String token, String expiry) throws WebServiceException {
		String ws = ctx.getResources().getString(R.string.WSRoot) + "SetFbDetails?user_id=" + userId + "&access_token=" + URLEncoder.encode(token) + "&expiry=" + expiry;
		JSONObject responseObject = callWebService(ctx, ws);
		try {
			String responseCode = responseObject.getString("ResponseCode");
			if (responseCode.equals("SUCCESS")) {
				responseObject = responseObject.getJSONObject("ResponseData");
				return responseObject.getString("Message");
			}

		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);
		}
		return null;
	}

	public static String saveSettings(Context ctx, AppUser u) throws WebServiceException {
		int pushInt = u.push_notificationsBoolean ? 1 : 0;
		int shareFbTwInt = 3;
		{
			String binaryStringfb = u.share_fbBoolean ? "1" : "0";
			String binaryStringTw = u.sh_twitterBoolean ? "1" : "0";
			shareFbTwInt = Integer.parseInt(binaryStringfb + binaryStringTw, 2);
		}
		String background3gStr = u.background_on_3gBoolean ? "On" : "Off";
		int prioInt = u.prioritizing_ratesInt;
		String lDefLocationString = URLEncoder.encode(u.default_locationString);
		String defVotDurString = URLEncoder.encode(AppSession.votingDurationArrayList.get(u.default_voting_durationInt));
		int mkmInt = u.miles_kilometersInt;
		String defAgeStr = URLEncoder.encode(AppSession.ageGroupArrayList.get(u.default_age_groupInt));
		String service = ctx.getResources().getString(R.string.WSRoot) + "Settings?user_id=" + u.idString + "&push_notifications=" + pushInt + "&share_fb_or_twitter="
				+ shareFbTwInt + "&background_on_3g=" + background3gStr + "&prioritizing_rates=" + prioInt + "&default_location=" + lDefLocationString
				+ "&default_voting_duration=" + defVotDurString + "&miles_kilometers=" + mkmInt + "&filters=" + u.filtersString.replaceAll(" ", "") + "&default_age_group="
				+ defAgeStr;
		JSONObject responseJsonObject = callWebService(ctx, service);
		try {
			if (responseJsonObject.getString("ResponseCode").equals("SUCCESS")) {
				responseJsonObject = responseJsonObject.getJSONObject("ResponseData");
				return responseJsonObject.getString("Message");
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);

		}
		return null;
	}

	public static RateItRate answerRate(Context ctx, String qid, int answer, String qidList) throws WebServiceException, RateItException {
		String tagList = AppUtils.getFiltersString();
		if (qid.equals("RateIt_Helpful_Rate7")) {
			answer = 5;
			AppSession.signedInUser.showDemo = false;
		}
		if (qid.equals("RateIt_Helpful_Rate1") || qid.equals("RateIt_Helpful_Rate2") || qid.equals("RateIt_Helpful_Rate3") || qid.equals("RateIt_Helpful_Rate4")
				|| qid.equals("RateIt_Helpful_Rate5") || qid.equals("RateIt_Helpful_Rate6")) {
			return null;
		}
		String lat, lon;
		try {
			lat = AppSession.currentUserLocation.getLatitude() + "";
			lon = AppSession.currentUserLocation.getLongitude() + "";
		} catch (Exception e) {
			lat = "37.0625";
			lon = "-95.677068";
		}
		int settings = AppSession.signedInUser.prioritizing_ratesInt + 1;
		// TODO change this
		int quit = 1;// AppUtils.canLoadRatesFromBackground(ctx) ? 0 : 1;
		String WSString = ctx.getResources().getString(R.string.WSRoot) + "Answers1?user_id=" + AppSession.signedInUser.idString + "&latitude=" + lat + "&longitude=" + lon
				+ "&tag_list=" + tagList.trim() + "&qid_list=" + qidList.trim() + "&answer=" + answer + "&q1=" + qid + "&quit=" + quit + "&settings=" + settings;
		//long currentTimeMillis1 = System.currentTimeMillis();
		JSONObject resulJsonObject = callWebService(ctx, WSString);
		//long currentTimeMillis2 = System.currentTimeMillis();
		//log("time is "+(currentTimeMillis2-currentTimeMillis1));
		try {
			if (AppUtils.canLoadRatesFromBackground(ctx) && resulJsonObject != null) {

				if (resulJsonObject.getString("ResponseCode").equals("SUCCESS")) {
					resulJsonObject = resulJsonObject.getJSONObject("ResponseData");
					RateItRate rateItRate = new RateItRate(ctx, resulJsonObject.getString("qid"), resulJsonObject.getString("duration"), resulJsonObject.getString("primarytag_id"));
					return rateItRate;
				} else {
					throw new RateItException("no more rates to show");
				}
			} else {
				// return null
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new WebServiceException(e);

		}
		return null;
	}

	public static void answerRateNew(Context ctx, String qid, int answer) throws WebServiceException {
		if (qid.equals("RateIt_Helpful_Rate7")) {
			answer = 5;
			AppSession.signedInUser.showDemo = false;
		}
		String WSString = ctx.getResources().getString(R.string.WSRoot) + "Answers1?user_id=" + AppSession.signedInUser.idString + "&answer=" + answer + "&q1=" + qid;
		try {
			JSONObject resulJsonObject =callWebService(ctx, WSString);
			if (resulJsonObject != null && !resulJsonObject.getString("ResponseCode").equals("SUCCESS"))
				throw new WebServiceException(new Exception("no more rates to show"));
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
	}

	private static void log(Object o) {
		Log.d("ServiceConnector", o.toString());
	}
}// END OF CLASS