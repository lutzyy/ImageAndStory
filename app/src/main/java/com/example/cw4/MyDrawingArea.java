package com.example.cw4;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

public class MyDrawingArea extends View {
    Path path = new Path();
    /*This bmp is declared outside globally in the custom view class*/
    Bitmap bmp;

    private int curr=Color.BLACK;


    private Paint paint= new Paint();
    private float currStroke=5.0f;
    public MyDrawingArea(Context context) {
        super(context);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyDrawingArea(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
//use color
    //stroke (size).


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Stroke();

        //canvas.drawCircle(cx,cy,radius,paint);
        Paint p = new Paint();
        p.setColor(curr);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(currStroke);
        /*
        Note: Declare path somewhere outside onDraw
        Path path = new Path();
        */
        //path.moveTo(100, 100);
        /**path.lineTo(150, 50);
         path.lineTo(200, 150);
         path.lineTo(250, 100);
         path.lineTo(300, 120);
         path.moveTo(120, 400);
         path.lineTo(170, 350);
         path.lineTo(220, 450);
         path.lineTo(270, 400);
         path.lineTo(320, 420);
         **/
        canvas.drawPath(path, p);
        invalidate();
    }
    public void SetSize(float SWidth)
    {
        currStroke= SWidth;

    }

    public void SetColor(int color)
    {
        curr=color;

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            path.moveTo(x, y);
        }
        else if(action == MotionEvent.ACTION_MOVE){
            path.lineTo(x, y);
        }
        return true;
    }


    public Bitmap getBitmap()
    {
        bmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint();
        p.setColor(curr);
        p.setStyle(Paint.Style.STROKE);
        p.setAntiAlias(true);
        p.setStrokeWidth(currStroke);
        c.drawPath(path, p); //path is global. The very same thing that onDraw uses.
        return bmp;
    }

    public void clear()
    {

        path.reset();
    }


    /** public void setScrollingEnabled(ScrollView scrollView, boolean enabled) {
     if (enabled) {
     scrollView.requestDisallowInterceptTouchEvent(false);
     } else {
     scrollView.requestDisallowInterceptTouchEvent(true);
     }
     }
     **/





}
