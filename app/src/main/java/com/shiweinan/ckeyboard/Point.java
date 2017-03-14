package com.shiweinan.ckeyboard;

/**
 * Created by Weinan on 2017/3/13.
 */

public class Point {
    public float x;public float y;
    public Point(float xx, float yy) {
        this.x = xx;
        this.y = yy;
    }
    public double dist(Point p) {
        return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    }
}
