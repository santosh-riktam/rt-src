package com.riktamtech.android.ratethisstc.exceptions;

/**
 * 
 * @author santu
 * 
 *         thrown when the rates are over and ws returns false
 * 
 */
public class RateItException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3810436501482982144L;

	public RateItException(String message) {
		super(message);
	}
}
