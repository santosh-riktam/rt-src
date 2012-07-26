
package com.riktamtech.android.ratethisstc.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomFontizer {
	/**
	 * finds textviews in root and applies custom font to them
	 * 
	 * @param root
	 * @param is
	 */
	public void fontize(ViewGroup root, int... is) {
		Typeface typeface = Typeface.createFromAsset(root.getContext()
				
				.getAssets(), "fonts/MyriadPro-Regular.otf");

		TextView tv;
		
		
		
		
		
		for (int i : is) {
			tv = (TextView) root.findViewById(i);
			tv.setTypeface(typeface);
		}

	}

	/**
	 * applies custom font to single textview
	 * 
	 * @param ctx
	 * @param tv
	 */
	public void fontize(Context ctx, TextView tv) {
		Typeface typeface = Typeface.createFromAsset(ctx.getAssets(),
				"fonts/MyriadPro-Regular.otf");
		tv.setTypeface(typeface);
	}

}
