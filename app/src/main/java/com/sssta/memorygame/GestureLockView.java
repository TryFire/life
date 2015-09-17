package com.sssta.memorygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by function on 2015/8/8.
 */
public class GestureLockView extends View {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint selectPaint = new Paint();
    Paint errorPaint = new Paint();

    Bitmap normalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.normal);
    Bitmap selectBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.select);
    Bitmap errorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.error);

    public Point[][] points = new Point[3][3];
    ArrayList<Point> pointsSelectList = new ArrayList<Point>();
    ArrayList<Integer> passList = new ArrayList<Integer>();

    public onDrawFinishedListener listener;

    private boolean inited = false;
    private boolean isDraw = false;
    private boolean isFinished = false;

    float bitmapR;
    float mouseX, mouseY;

    private Point nextPoint;

    public GestureLockView(Context context) {
        super(context);
    }

    public GestureLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        /*points[0][0] = new Point(offsetX + sideLength, offsetY);
        points[0][1] = new Point(offsetX + sideLength * 2, offsetY);
        points[0][2] = new Point(offsetX + sideLength * 3, offsetY);

        points[1][0] = new Point(offsetX + sideLength, offsetY + sideLength);
        points[1][1] = new Point(offsetX + sideLength * 2, offsetY + sideLength);
        points[1][2] = new Point(offsetX + sideLength * 3, offsetY + sideLength);

        points[2][0] = new Point(offsetX + sideLength, offsetY + sideLength * 2);
        points[2][1] = new Point(offsetX + sideLength * 2, offsetY + sideLength * 2);
        points[2][2] = new Point(offsetX + sideLength * 3, offsetY + sideLength * 2);*/


        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                points[i][j] = new Point(offsetX + sideLength * (j + 1), offsetY + sideLength * (i + 1));
            }
        }

        inited = true;

    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(!inited) {
            init();
        }

        drawPoints(canvas);

        if(pointsSelectList.size() > 0) {
            Point a = pointsSelectList.get(0);
            for (int i = 1; i < pointsSelectList.size(); i++) {
                Point next = pointsSelectList.get(i);
                drawLine(canvas, a, next);
                a = next;
            }
            if (isDraw) {
                drawLine(canvas, a, new Point(mouseX, mouseY));
            }
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        int[] result;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                result = getSelectedPoint();
                if(result != null) {
                    isFinished = false;
                    resetPoints();
                    isDraw = true;
                    int i = result[0];
                    int j = result[1];
                    points[i][j].setState(Point.STATE_SELECT);
                    pointsSelectList.add(points[i][j]);
                    passList.add(i * 3 + j);
                }
                break;
            case MotionEvent.ACTION_MOVE :
                result = getSelectedPoint();
                if(isDraw) {
                    if(result != null) {
                        int i = result[0];
                        int j = result[1];
                        points[i][j].setState(Point.STATE_SELECT);
                        if(!pointsSelectList.contains(points[i][j])) {
                            pointsSelectList.add(points[i][j]);
                            passList.add(i * 3 + j);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isDraw = false;
                boolean value = false;
                if(listener != null) {
                    value = listener.onDrawFinished(passList);
                }
                if(!value) {
                    for(Point p : pointsSelectList) {
                        p.setState(Point.STATE_ERROR);
                    }
                } else {
                    isFinished = true;
                    resetPoints();
                }
                break;
        }
        postInvalidate();
        return true;
    }

    public boolean isDraw() {
        return isDraw;
    }

    public boolean isFinished() {
        return isFinished;
    }

    private int[] getSelectedPoint() {
        Point mousePoint = new Point(mouseX, mouseY);
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                if(points[i][j].getDistance(mousePoint) < bitmapR) {
                    int[] result = new int[2];
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return null;
    }

    public void resetPoints() {
        pointsSelectList.clear();
        passList.clear();
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                points[i][j].setState(Point.STATE_NORMAL);
            }
        }
        this.postInvalidate();
    }

    public interface onDrawFinishedListener {
        boolean onDrawFinished(List<Integer> passList);
    }

    public void setOnDrawFinishedListener(onDrawFinishedListener listener) {
        this.listener = listener;
    }
}
