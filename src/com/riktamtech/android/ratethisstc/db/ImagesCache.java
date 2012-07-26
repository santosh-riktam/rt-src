package com.riktamtech.android.ratethisstc.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * holding bitmaps in memory speeds up loading but uses lot of memory
 * @author Santosh Kumar D
 *
 */
@Deprecated
public class ImagesCache {
	private Hashtable<String, SoftReference<Bitmap>> cache;
	Context ctx;
	
	public ImagesCache(Context ctx) {
		super();
		this.ctx = ctx;
		cache=new Hashtable<String, SoftReference<Bitmap>>();
		
		File file=new File(ctx.getExternalCacheDir().getAbsolutePath()+"/thumbs/");
		if(!file.exists()) file.mkdirs();
		
	}

	public Bitmap getBitmap(String str)
	{
		Bitmap bmp = null;
		String file=str.substring(str.lastIndexOf('/')+1);
		SoftReference<Bitmap> bitmapSoftReference =cache.get(file);
		if(bitmapSoftReference!=null) 
			bmp=bitmapSoftReference.get();
		if(bmp==null)
			{
				bmp=readFile(file);
				if(bmp==null)
				{
					try {
						URL url=new URL(str);
						bmp=BitmapFactory.decodeStream(url.openStream());
						if(bmp!=null){
						if(!writeFile(file,bmp)) {
							Log.e(">>>>>>>>>><<<<<<<<", "write unsuccessful");
						}
						cache.put(file, new SoftReference<Bitmap>(bmp));
						return bmp;
						}
						else
						{
							return null;
						}
						
					} catch (MalformedURLException e) {
						System.out.println(e.getMessage());
					} catch (IOException e) {
						System.out.println(e.getMessage());
					}
				}
				else{
					cache.put(file	, new SoftReference<Bitmap>(bmp));
					return bmp;
				}
			}
		else{
			return bmp;
		}
				
		return bmp;
		
	}

	private boolean writeFile(String str, Bitmap bmp) {
		//Log.d("IIIIIIIII", "writing "+str);
		File rootDir = ctx.getExternalCacheDir();
		File file = new File(rootDir.getAbsolutePath()+"/thumbs/" + str);
		
		try {
			
			return bmp.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		
	}

	private Bitmap readFile(String str) {
		//Log.d("IIIIIIIII", "reading "+str);
		
		File rootDir = ctx.getExternalCacheDir();
		File file = new File(rootDir.getAbsolutePath()+"/thumbs/" + str);
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

}
