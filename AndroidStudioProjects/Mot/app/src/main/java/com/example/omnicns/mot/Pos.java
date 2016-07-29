package com.example.omnicns.mot;

public class Pos{
    private long timestamp;
    private double x;
    private double y;
    private double z;
    private double w;
    private double v;
    private double a;
    private double b;




    float[] filteredValues = new float[3];
    float alpha= 0.1f;

    public Pos(long timestamp, float x, double y, double z,double w,double v,double a,double b) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.v = v;
        this.a = a;
        this.b = b;


    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getX() {
        return x;
    }
    public void setX(double x) {
        this.x = x;
    }
    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public double getZ() {
        return z;
    }
    public void setZ(double z) {
        this.z = z;
    }
    public double getW() {
        return w;
    }
    public void setW(double w) {
        this.w = w;
    }
    public double getV() {
        return v;
    }
    public void setV(double v) {
        this.v = v;
    }
    public double getA() {
        return a;
    }
    public void setA(double a) {
        this.a = a;
    }
    public double getB() {
        return b;
    }
    public void setB(double b) {
        this.b = b;
    }

    public String toString() {
        return "t="+timestamp+", x="+x+", y="+y;
    }
}