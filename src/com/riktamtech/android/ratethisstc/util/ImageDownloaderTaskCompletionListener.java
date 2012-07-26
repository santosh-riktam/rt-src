package com.riktamtech.android.ratethisstc.util;

import java.io.File;

/**
 * interface to be impplemented for classes wishing to be notified when an
 * download task completes
 * 
 * @author Santosh Kumar D
 * 
 */
public interface ImageDownloaderTaskCompletionListener {
	public void onImageDownloadComplete(File f);
}
