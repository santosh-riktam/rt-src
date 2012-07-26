package com.riktamtech.android.ratethisstc.exceptions;


public class WebServiceException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3774394145129679978L;
	Exception ex;
	public WebServiceException(Exception e)
	{
		super(e.getMessage());
		ex=e;
	}
	
	
}
