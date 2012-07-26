package com.riktamtech.android.ratethisstc.util;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.Facebook;
import com.harrison.lee.twitpic4j.TwitPic;
import com.harrison.lee.twitpic4j.TwitPicResponse;
import com.harrison.lee.twitpic4j.exception.InvalidUsernameOrPasswordException;
import com.riktamtech.android.ratethisstc.R;

public class FbTwConnector
{
	Context ctx;
			
	public FbTwConnector(Context ctx)
	{
		super();
		this.ctx = ctx;
	}

	public String postToFacebookWall(Facebook facebook, String picture, String message, String link)
	{
		Bundle params = new Bundle();
		params.putString("message", message);
		if(link!=null ) 
			params.putString("link", link);
		params.putString("picture", picture);
		params.putString("caption", "RateThis!");
		log(picture);
		
		try 
			{
				String resultString = facebook.request("me/feed", params, "POST");
				if (resultString.contains("{\"id\""))
					{
						String sr=new JSONObject(resultString).getString("id");
						return "success. id = " + sr;
					}
				else
					return "facebook error : result : "+resultString  ;

			} catch (Exception e)
			{
				e.printStackTrace();
				return "facebook error : Exception : "+e.getMessage();
			}
	}

	public String tweet(String userName, String password, String imageLocation, String post)
	{
		try
			{
				URL url = new URL(imageLocation);
				Bitmap bitmap ;
				try{
					bitmap= BitmapFactory.decodeStream(url.openStream());
				}catch (Exception e) {
					e.printStackTrace();
					url=new URL(ctx.getResources().getString(R.string.WSImages)+"icon.png");
				}
				bitmap= BitmapFactory.decodeStream(url.openStream());
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 100, outputStream);
				TwitPic twitPic = new TwitPic(userName, password);

				TwitPicResponse twitPicResponse = twitPic.uploadAndPost(outputStream.toByteArray(), post);
				if (twitPicResponse.getErrorMessage() != null)
					throw new Exception("error");

				return twitPicResponse.getStatus();

			} catch (InvalidUsernameOrPasswordException e)
			{
				e.printStackTrace();
				return "invalid details";
			} catch (Exception e)
			{
				e.printStackTrace();
				return "failed";
			}

	}

	
	public String tweet(Context ctx,String userName, String password, int imageResource, String post)
	{
		try
			{
				log(imageResource+"");
				Bitmap bitmap = null ;
				try{
					bitmap= BitmapFactory.decodeResource(ctx.getResources(), imageResource);
				}catch (Exception e) {
				}
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 100, outputStream);
				TwitPic twitPic = new TwitPic(userName, password);

				TwitPicResponse twitPicResponse = twitPic.uploadAndPost(outputStream.toByteArray(), post);
				if (twitPicResponse.getErrorMessage() != null)
					throw new Exception("error");

				return twitPicResponse.getStatus();

			} catch (InvalidUsernameOrPasswordException e)
			{
				e.printStackTrace();
				return "invalid details";
			} catch (Exception e)
			{
				e.printStackTrace();
				return "failed";
			}

	}
	
	public void log(String s)
	{
		Log.d("FBTWCONNECTOR", s);
	}
}
