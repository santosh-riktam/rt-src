package com.riktamtech.android.ratethisstc.exceptions;

public class RegistrationException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -586061405629832236L;
	public static final int INVALID_USERNAME=1;
	public static final int INVALID_EMAIL=2;
	

	public int type;

	public RegistrationException(String m)
	{
		super(m);
		if(m.contains("sername"))
			type=INVALID_USERNAME;
		else if(m.contains("emailId"))
			type=INVALID_EMAIL;
		
	}

	

}
