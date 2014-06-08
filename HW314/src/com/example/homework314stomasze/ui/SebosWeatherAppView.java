package com.example.homework314stomasze.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SebosWeatherAppView extends View {
    private PointF point = null;
    private Paint paint = null;

    public SebosWeatherAppView(Context context) {
        this(context, null);
    }

    public SebosWeatherAppView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:


            break;
        case MotionEvent.ACTION_UP:
            
            
            break;
        case MotionEvent.ACTION_MOVE:


            
            break;
        }
        invalidate();
        return true;
    }

}
