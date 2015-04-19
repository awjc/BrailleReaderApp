package com.asliced.braillereader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.view.SurfaceView;

public class DrawView extends SurfaceView {
	private Paint textPaint = new Paint();
	private Paint p = new Paint();

	private Bitmap bitmap;
	private String result;
	private int count = 0;
		
	public DrawView(Context context) {
		super(context);
		textPaint.setARGB(255, 200, 0, 0);
		textPaint.setTextSize(60);
		p.setARGB(255, 0, 0, 0);
		setWillNotDraw(false);
	}
	
	public void setString(String result){
		if(result != null){
			this.result = result;
			count = 3;
		} else {
			count--;
			if(count <= 0){
				this.result = null;
			}
		}
	}

	public void setBitmap(Bitmap bitmap){
		if(this.bitmap != null){
			this.bitmap.recycle();
		}
		
		this.bitmap = bitmap;
		dstRect = new Rect(0, 0, getWidth(), getHeight());
	
		roiPaint.setAlpha(150);
		roiPaint.setColor(Color.rgb(255, 0, 0));
		roiPaint.setStrokeWidth(5);
		roiPaint.setStyle(Style.STROKE);
		
		textPaint.setAlpha(255);
		textPaint.setColor(Color.rgb(255, 100, 10));
		
		pp.setAlpha(128);
		pp.setAntiAlias(true);
	}
	
	private Rect dstRect;
	Paint pp = new Paint();
	Paint roiPaint = new Paint();
	
	private float ROI_LEFT = 0.02f;
	private float ROI_RIGHT = 0.98f;
	private float ROI_TOP = 0.4f;
	private float ROI_BOTTOM = 0.55f;
	
	public float getROILeft(){
		return ROI_LEFT;
	}
	
	public float getROIRight(){
		return ROI_RIGHT;
	}
	
	public float getROITop(){
		return ROI_TOP;
	}
	
	public float getROIBottom(){
		return ROI_BOTTOM;
	}
		
	@Override
	protected void onDraw(Canvas canvas){
		canvas.drawRect(ROI_LEFT*canvas.getWidth(), ROI_TOP*canvas.getHeight(),
				ROI_RIGHT*canvas.getWidth(), ROI_BOTTOM*canvas.getHeight(), roiPaint);
		String str = result;
		if(result == null){
			str = "No Translation";
		}
		canvas.drawText("Result: " + str, 75, 250, textPaint);
		
		canvas.drawText("Threshold: " + PhotoHandler.threshold, 75, 150, textPaint);
		
		if(bitmap != null){
			dstRect.set((int)(ROI_LEFT*canvas.getWidth()), (int)(ROI_TOP*canvas.getHeight()), 
					(int)((ROI_RIGHT)*canvas.getWidth()), (int)((ROI_BOTTOM)*canvas.getHeight()));

			canvas.drawBitmap(bitmap, null, dstRect, pp);
			System.out.println("DRAWN");
		}
	}
}