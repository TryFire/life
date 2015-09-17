package com.sssta.memorygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by function on 2015/8/29.
 */
public class AutoDrawView extends View {

    int i = 0;
    private MyThread thread;

    public Point[][] points = new Point[3][3];

    private float bitmapR;

    private boolean inited = false;
    private boolean added = false;
    private boolean started = false;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint selectPaint = new Paint();
    private Paint errorPaint = new Paint();

    Bitmap normalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.normal);
    Bitmap selectBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.select);
    Bitmap errorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.error);

    private float x;
    private float y;
    private float slope = 0.03f;

    ArrayList<Point> pointsSelectList = new ArrayList<Point>();
    ArrayList<Point> pointsPrepared = new ArrayList<>();
    ArrayList<Integer> numberList = new ArrayList<>();
    ArrayList<Integer> passList = new ArrayList<>();

    private onAutoDrawFinishedListener listener;

    private Random random = new Random();

    public AutoDrawView(Context context) {
        super(context);
    }

    public AutoDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(!inited) {
            init();
        }

        drawPoints(canvas);

//Log.e("+++++", "++++++------------");

        if(pointsSelectList.size() > 0) {
            Point a = pointsSelectList.get(0);
            for (int i = 1; i < pointsSelectList.size(); i++) {
                Point next = pointsSelectList.get(i);
                drawLine(canvas, a, next);
                a = next;
            }
             drawLine(canvas, a, x, y);
        }
    }

    private void init() {
        selectPaint.setColor(Color.YELLOW);
        selectPaint.setStrokeWidth(10);
        errorPaint.setColor(Color.RED);
        errorPaint.setStrokeWidth(10);

        int offsetX, offsetY;
        int width = getWidth();
        int height = getHeight();
        int sideLength;
        bitmapR = normalBitmap.getHeight() / 2;

        if(width < height) {
            sideLength = width / 4;
            offsetX = 0;
            offsetY = (height - width) / 2;
        } else {
            sideLength = height / 4;
            offsetX = (width - height) / 2;
            offsetY = 0;
        }
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                points[i][j] = new Point(offsetX + sideLength * (j + 1), offsetY + sideLength * (i + 1));
            }
        }
        for (int i = 0; i < 9; i++) {
            numberList.add(i);
        }
        inited = true;

    }

    public void startAutoDrawPoints(int number) {
        if(!started) {
            started = true;
//Log.e("is Start", started + "");
            addPoints(number);

            if(added) {
                thread = new MyThread();
                thread.start();
//Log.e("Thread new ", "new yes yes");
            }
        }
    }

    private synchronized void addPoints(int number) {
        passList.clear();
        int x, y;
        int temple, result;
        int count;
        for (int i = 0; i < number; i++) {
            count = numberList.size();
           // Log.e("count", count + "");
            temple = random.nextInt(count);
            result = numberList.get(temple);
            /*Log.e("result===========", result+"");
            Log.e("temple===========", temple+"");
            Log.e("numberList size==", numberList.size()+"");*/
            x = result / 3;
            y = result % 3;
            pointsPrepared.add(points[x][y]);
            numberList.remove(temple);
            passList.add(x * 3 + y);
        }
        added = true;
    }

    public boolean isStarted() {
        return started;
    }

    private void drawPoints(Canvas canvas) {
        float x, y;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                x = points[i][j].getX();
                y = points[i][j].getY();
                switch (points[i][j].getState()) {
                    case Point.STATE_NORMAL:
                        canvas.drawBitmap(normalBitmap, x - bitmapR, y - bitmapR, paint);
                        break;
                    case Point.STATE_SELECT:
                        canvas.drawBitmap(selectBitmap, x - bitmapR, y - bitmapR, paint);
                        break;
                    case Point.STATE_ERROR:
                        canvas.drawBitmap(errorBitmap, x - bitmapR, y - bitmapR, paint);
                        break;
                }

            }
        }
    }

    private void drawLine(Canvas canvas, Point first, Point next) {
        if(first.getState() == Point.STATE_SELECT) {
            canvas.drawLine(first.getX(), first.getY(), next.getX(), next.getY(), selectPaint);
        } else if(first.getState() == Point.STATE_ERROR) {
            canvas.drawLine(first.getX(), first.getY(), next.getX(), next.getY(), errorPaint);
        }
    }

    private void drawLine(Canvas canvas, Point first, float x, float y) {
        if(first.getState() == Point.STATE_SELECT) {
            canvas.drawLine(first.getX(), first.getY(), x, y, selectPaint);
        } else if(first.getState() == Point.STATE_ERROR) {
            canvas.drawLine(first.getX(), first.getY(), x, y, errorPaint);
        }
    }

    private class MyThread extends Thread{
        @Override
        public void run() {
//Log.e("=========", "+++++++++++++");
            /*Iterator iterator = pointsPrepared.iterator();
            Point previous = (Point)iterator.next();*/
            int number = pointsPrepared.size();
            Point previous = pointsPrepared.get(0);
            previous.setState(Point.STATE_SELECT);
            pointsSelectList.add(previous);
            while(i < (number - 1)) {
//Log.e("i", "======" + i + "===========");
                i = i + 1;
                x = previous.getX();
                y = previous.getY();
             //   Point next = (Point)iterator.next();
                Point next = pointsPrepared.get(i);
                float[] resultXY = getSlope(previous, next);
                while(next.getDistance(x, y) > 20 ) {
                    x = resultXY[0] + x;
                    y = resultXY[1] + y;
                    postInvalidate();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                next.setState(Point.STATE_SELECT);
                pointsSelectList.add(next);
                postInvalidate();
                previous = next;
            }
            resetAutoDraw();
        }
    }

    private float[] getSlope(Point a, Point b) {
        float[] resultXY = new float[2];

        float aX = a.getX();
        float aY = a.getY();
        float bX = b.getX();
        float bY = b.getY();

//Log.e("i dont know", "aX:" + aX + "==" +"bX:" + bX + "==" +"aY:" + aY + "==" +"bY:" + bY + "==" );

        float resultX = (bX - aX) * slope;
        float resultY = (bY - aY) * slope;

        resultXY[0] = resultX ;
        resultXY[1] = resultY ;

        return resultXY;
    }

    private void resetAutoDraw() {
        started = false;

        if(listener != null) {
            listener.onAutoDrawFinished(passList);
        }
        i = 0;
        numberList.clear();
        pointsPrepared.clear();
        pointsSelectList.clear();
        for (int i = 0; i < 9; i++) {
            numberList.add(i);
        }
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                points[i][j].setState(Point.STATE_NORMAL);
            }
        }
        postInvalidate();
    }

    public interface onAutoDrawFinishedListener {
        void onAutoDrawFinished(List<Integer> passList);
    }

    public void setOnDrawFinishedListener(onAutoDrawFinishedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
