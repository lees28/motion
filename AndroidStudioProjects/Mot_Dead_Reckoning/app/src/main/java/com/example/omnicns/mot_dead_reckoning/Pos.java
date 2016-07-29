package com.example.omnicns.mot_dead_reckoning;

public class Pos {
    private long timestamp;
    private float x;
    private float y;
    private float z;
    private float w;
    private float v;
    private float a;
    private float b;




    float[] filteredValues = new float[3];
    float alpha= 0.1f;

    public Pos(long timestamp, float x, float y) {
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
    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }
    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }
    public float getZ() {
        return z;
    }
    public void setZ(float z) {
        this.z = z;
    }
    public float getW() {
        return w;
    }
    public void setW(float w) {
        this.w = w;
    }
    public float getV() {
        return v;
    }
    public void setV(float v) {
        this.v = v;
    }
    public float getA() {
        return a;
    }
    public void setA(float a) {
        this.a = a;
    }
    public float getB() {
        return b;
    }
    public void setB(float b) {
        this.b = b;
    }

    public String toString() {
        return "t="+timestamp+", x="+x+", y="+y;
    }
}