package com.example.omnicns.sample;

public class Pos {
    private long timestamp;
    private long x;
    private long y;
    private long z;
    private long w;
    private long v;
    private long a;
    private long b;




    float[] filteredValues = new float[3];
    float alpha= 0.1f;

    public Pos(long timestamp, long x, long y, long z, long w, long v, long a, long b) {
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
    public long getX() {
        return x;
    }
    public void setX(long x) {
        this.x = x;
    }
    public long getY() {
        return y;
    }
    public void setY(long y) {
        this.y = y;
    }
    public long getZ() {
        return z;
    }
    public void setZ(long z) {
        this.z = z;
    }
    public long getW() {
        return w;
    }
    public void setW(long w) {
        this.w = w;
    }
    public long getV() {
        return v;
    }
    public void setV(long v) {
        this.v = v;
    }
    public long getA() {
        return a;
    }
    public void setA(long a) {
        this.a = a;
    }
    public long getB() {
        return b;
    }
    public void setB(long b) {
        this.b = b;
    }

    public String toString() {
        return "t="+timestamp+", x="+x+", y="+y;
    }
}