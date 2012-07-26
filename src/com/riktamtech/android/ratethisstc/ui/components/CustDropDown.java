package com.riktamtech.android.ratethisstc.ui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.riktamtech.android.ratethisstc.R;

/**
 * 
 * @author santu
 * 
 *         custom spinner
 */

public class CustDropDown extends TextView implements OnClickListener
{

	Context ctx;
	public String dialogTitle;
	public String[] items;
	public OnClickListener onClickListener;

	public CustDropDown(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initt();
	}

	public void initt()
	{
		setTextColor(Color.WHITE);
		setGravity(Gravity.LEFT);
		setText("      ");
		setBackgroundColor(getResources().getColor(R.color.tvBg));
		setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down_arow, 0);
		setOnClickListener(this);
		setPadding(2, 2, 6, 2);
	}

	/**
	 * 
	 * @param title - title of popup dialog shown onclick the view
	 * @param i     - string array reference
	 * @param index - index of the array currenty selected 
	 */
	public void setParams(String title, int i, int index)
	{
		if (index == -1)
			{
				items = getResources().getStringArray(i);
				dialogTitle = title;
				setText("  ");
				currentIndex = index;
			}
		else
			{
				items = getResources().getStringArray(i);
				dialogTitle = title;
				setText(items[index]);
				currentIndex = index;
			}

	}
	
	/**
	 * 
	 * @param title				- title of popup dialog shown onclick the view
	 * @param stringArrayRef	- string array reference
	 * @param text 				- text to be displayed by the view 
	 */
	public void setParams(String title,int stringArrayRef,String text)
	{
		items = getResources().getStringArray(stringArrayRef);
		dialogTitle = title;
		setText(text);
		currentIndex = -1;
		for(int i=0;i<items.length;i++)
			{
				if(text.equals(items[i]))
					{
						currentIndex=i;
						break;
					}
			}
		
	}

	@Override
 	public void onClick(View v)
	{
		if (dialogTitle == null)
			return;
		final View vv = v;
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(dialogTitle);

		builder.setItems(items, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				setText(items[which] + " ");
				currentIndex = which;
				mPopup.dismiss();
				if (onClickListener != null)
					onClickListener.onClick(vv);
			}
		});
		mPopup = builder.show();

	}

	AlertDialog mPopup;
	public int currentIndex;

	public int getWSIndex()
	{
		if(currentIndex!=-1)
		return (currentIndex + 1);
		else
			{
				Log.d("CustDropDown", "index is -1 "+items);
				return 1;
			}
	}
	
	
	public void addItem(String str)
	{
		String items1[]=new String[items.length+1];
		System.arraycopy(items, 0, items1, 0, items.length);
		items1[items.length]=str;
		items=items1;
	}
	
	public void  removeLastItem() {
		String items1[]=new String[items.length-1];
		System.arraycopy(items, 0, items1, 0, items.length-1);
		items=items1;
	}
	
	public String getDisplayedText()
	{
		return items[currentIndex];
	}
}
