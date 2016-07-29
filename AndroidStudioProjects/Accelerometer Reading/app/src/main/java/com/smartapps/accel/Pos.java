package com.smartapps.accel;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapFactory.Options;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

public class Pos {

    public void integrate() {


        long curtime = System.currentTimeMillis();
        long timediff = curtime - prevtime;
        if (prevtime == 0)

        {
            timediff = 0;
            prevtime = curtime;
            return;
        }

        prevtime = curtime;

        float t = timediff / 1000.0f;

        // diferentiate
        float xd = (mAccel[0] - mAccelPrev[0]);
        float yd = (mAccel[1] - mAccelPrev[1]);
        float zd = (mAccel[2] - mAccelPrev[2]);

        // round
        xd = Math.round(xd * 50) / 50.0f;
        yd = Math.round(yd * 50) / 50.0f;
        zd = Math.round(zd * 50) / 50.0f;

        // enforce threshold
        if ((Math.abs(xd) < 0.06) || (Math.abs(xd) > 3.0))

        {
            xd = 0;
            mSpeed[0] /= 2;
        }

        if ((Math.abs(yd) < 0.06) || (Math.abs(yd) > 3.0))

        {
            yd = 0;
            mSpeed[1] /= 2;
        }

        if ((Math.abs(zd) < 0.06) || (Math.abs(zd) > 3.0))

        {
            zd = 0;
            mSpeed[2] /= 2;
        }

        // and reintegrate to get rid of gravity constant
        float ax = xd * t;
        float ay = yd * t;
        float az = zd * t;

        // integrate speed and distance
        mSpeed[0] += ax * t;
        mDist[0] += mSpeed[0] * t;

        mSpeed[1] += ay * t;
        mDist[1] += mSpeed[1] * t;

        mSpeed[2] += az * t;
        mDist[2] += mSpeed[2] * t;

        SensorManager.getRotationMatrix(R, I, mAccel, mGeoM);

        // store delta distance in vector
        float localC[] = {mDist[0] - mDistLast[0], mDist[1] - mDistLast[1], mDist[2] - mDistLast[2], 1};
        mDistLast[0] = mDist[0];
        mDistLast[1] = mDist[1];
        mDistLast[2] = mDist[2];

        // transform to world coordinates
        float worldC[] = new float[4];

        MatrixMulVector(R, localC, worldC);

        glview.updateRotationMatrix(R);
        glview.move(worldC);
    }
}
