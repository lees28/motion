package com.example.sooyeonlee.test;

public class Pos{
    private long timestamp;
    private double x;
    private double y;

    float[] filteredValues = new float[3];
    float alpha= 0.1f;

    public Pos(long timestamp, double x, double y) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
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
    public String toString() {
        return "t="+timestamp+", x="+x+", y="+y;
    }
}