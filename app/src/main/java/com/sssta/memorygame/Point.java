package com.sssta.memorygame;

/**
 * Created by function on 2015/8/8.
 */
public class Point {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SELECT = 1;
    public static final int STATE_ERROR = 2;

    private int state = Point.STATE_NORMAL;

    private float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getDistance(Point p) {
        float distance  = (float)Math.sqrt((p.getX() - this.x) * (p.getX() - this.x) + (p.getY() - this.y) * (p.getY() - this.y));
        return distance;
    }

    public float getDistance(float x, float y) {
        float distance = (float)Math.sqrt((x - this.x) * (x - this.x) + (y - this.y) * (y - this.y));
        return distance;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
