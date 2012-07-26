package com.riktamtech.android.ratethisstc.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.riktamtech.android.ratethisstc.R;
import com.riktamtech.android.ratethisstc.db.AppSession;

public class RatingView extends View {
	private float percent;
	private boolean useGrayColor;
	private boolean isCompleted;
	private boolean isFlagged;

	public RatingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustProgressBar);
		percent = array.getInt(R.styleable.CustProgressBar_percent, 50) / 100f;
		setPadding(2, 2, 2, 2);
		useGrayColor = false;
	}

	private int greenColor = Color.parseColor("#58D117"), redColor = Color.parseColor("#AE001E"), blueColor = Color.parseColor("#0E79FF"), orangeColor = Color
			.parseColor("#FF8000");

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		if (useGrayColor) {
			paint.setColor(greenColor);
			canvas.drawRect(0f, 0f, (getWidth() * percent) - 1, (float) getHeight(), paint);
			paint.setColor(Color.GRAY);
			canvas.drawRect((getWidth() * percent) + 1, 0, (float) getWidth(), (float) getHeight(), paint);
			return;
		}
		int primaryColor = orangeColor, secondaryColor = orangeColor;
		if (!isCompleted) {
			primaryColor = orangeColor;
			secondaryColor = orangeColor;
		}
		if (isFlagged) {
			primaryColor = redColor;
			secondaryColor = redColor;
		}
		if (!isFlagged && isCompleted) {

			if (percent < 0.5f) {
				primaryColor = redColor;
				secondaryColor = greenColor;
			} else if (percent > .5f) {
				primaryColor = greenColor;
				secondaryColor = redColor;
			} else {
				primaryColor = blueColor;
				secondaryColor = blueColor;
			}

		}
		paint.setColor(primaryColor);
		canvas.drawRect(0f, 0f, (getWidth() * percent) - 1, (float) getHeight(), paint);
		paint.setColor(secondaryColor);
		canvas.drawRect((getWidth() * percent), 0, (float) getWidth(), (float) getHeight(), paint);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

	}

	int width;

	/**
	 * Determines the width of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			result = AppSession.DEVICE_DENSITY > 160 ? 150 : 120;
		}

		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else {
			result = AppSession.DEVICE_DENSITY > 160 ? 5 : 3;
		}
		return result;
	}

	public void initt(float percent, boolean useGray, boolean isCompleted, boolean isFlagged) {
		this.percent = percent;
		useGrayColor = useGray;
		this.isCompleted = isCompleted;
		this.isFlagged = isFlagged;
		requestLayout();
		invalidate();
	}
}
