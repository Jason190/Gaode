package com.example.jason.gaode;

/**
 * Created by jason on 2016/9/14.
 */
public class dian {
    double x;
    double y;

    public double getX() {
        return this.x;
    }

    public void setX(double var1) {
        this.x = var1;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double var1) {
        this.y = var1;
    }

    public dian(double var1, double var3) {
        this.x = var1;
        this.y = var3;}
    public dian(){}
    public dian(dian point){
        this.x=point.x;
        this.y=point.y;
    }
}
