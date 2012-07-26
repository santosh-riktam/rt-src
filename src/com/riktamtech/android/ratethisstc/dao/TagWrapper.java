package com.riktamtech.android.ratethisstc.dao;

import com.riktamtech.android.ratethisstc.db.AppSession;



public class TagWrapper {

	public int initialPosition;
	public boolean enabled;
	public String text;
	public TagWrapper(int initialPosition, boolean enabled) {
		super();
		this.initialPosition = initialPosition;
		this.enabled = enabled;
		text=AppSession.primaryTagsArrayList.get(initialPosition);
	}
	
	
}
