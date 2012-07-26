package com.riktamtech.android.ratethisstc.exceptions;

public class LoginException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5566544239555974242L;
	public static final int INVALID_USERNAME_OR_PASSWORD=1;
	public static final int UNREGISTERED_USERNAME=2;
	
	public int type;

	public LoginException(String m)
	{
		super(m);
		if(m.contains("Invalid"))
			type=INVALID_USERNAME_OR_PASSWORD;
		else if(m.contains("Unregistered"))
			type=UNREGISTERED_USERNAME;
		
	}
	
}
