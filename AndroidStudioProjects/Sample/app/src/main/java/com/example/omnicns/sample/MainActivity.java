package com.example.omnicns.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.hardware.SensorEventListener;
import android.view.View;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
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


public class MainActivity extends Activity implements SensorEventListener,
        OnClickListener {
    private SensorManager sensorManager;
    private Button btnStart, btnStop, btnUpload, btnVelocity;
    private boolean started = false;
    private ArrayList<Pos> sensorData;
    private LinearLayout layout;
    private View mChart;
    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    long velocity, velocity1, velocity2 = 0;
    long position, position1, position2 = 0;
    float[] acceleration = null;
    float dt;
    long last_timestamp = 0;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    private float[] gravity = new float[]{0, 0, 0};

}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.chart_container);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorData = new ArrayList<Pos>();
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btnVelocity = (Button) findViewById(R.id.btnVelocity);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnVelocity.setOnClickListener(this);

        btnStart.setEnabled(true);
        btnStop.setEnabled(false);

        btnUpload.setEnabled(false);
        btnVelocity.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (started == true) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    float prev_xAcceleration = 0;
    float prev_xVelocity = 0;
    float prev_xPosition = 0;
    float prev_yAcceleration = 0;
    float prev_yVelocity = 0;
    float prev_yPosition = 0;
    float prev_zAcceleration = 0;
    float prev_zVelocity = 0;
    float prev_zPosition = 0;

    //original

    private float filterCoefficient = 0.9f;
    private float timestamp;
    //private int  noAccelerationCount,noAccelerationCount1;
    private int sense = 0;


    float a = 0.2f;

    //float mLowPassX, mLowPassY, mLowPassZ;
    float lowPass(float current, float last) {
        return last * (1.0f - a) + current * a;
    }

    //
    float Rot[] = null; //for gravity rotational data
    //don't use R because android uses that for other stuff
    float I[] = null; //for magnetic rotational data

    float azimuth;
    float pitch;
    float roll;
    //
    float oldTime = 0;
    long vx, vy, vz = 0;
    long x, y, z = 0;

    public void onSensorChanged(SensorEvent event) {

            /*
        if (started&&event.sensor.getType() == Sensor.TYPE_ORIENTATION) {

        }
            */
//
        if (started) {
            float accels[] = new float[3];
            float mags[] = new float[3];
            float[] values = new float[3];

            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mags = event.values.clone();
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    accels = event.values.clone();
                    break;
            }
            if (mags != null && accels != null) {
                Rot = new float[9];
                I = new float[9];
                SensorManager.getRotationMatrix(Rot, I, accels, mags);
                // Correct if screen is in Landscape

                float[] outR = new float[9];
                SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, values);


                azimuth = (float)Math.toDegrees(values[0]); //looks like we don't need this one
                pitch =(float)Math.toDegrees(values[1]);
                roll = (float)Math.toDegrees(values[2]);
                x = (long) accels[0];
                y = (long) accels[1];
                z = (long) accels[2];
            } else {
                azimuth = 0;
                pitch = 0;
                roll = 0;

            }
        /*
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            */
            //apply low-pass filter
            float oneMinusCoeff = (1.0f - filterCoefficient);

            //x=lowPass(x,prev_xAcceleration);
            //y=lowPass(y,prev_yAcceleration);
            //z=lowPass(z,prev_zAcceleration);

            float lowPassX, lowPassY, lowPassZ;

            //low pass filter
            gravity[0] = filterCoefficient * gravity[0] + oneMinusCoeff * x;
            gravity[1] = filterCoefficient * gravity[1] + oneMinusCoeff * y;
            gravity[2] = filterCoefficient * gravity[2] + oneMinusCoeff * z;
            lowPassX = x - gravity[0];
            lowPassY = y - gravity[1];
            lowPassZ = z - gravity[2];

            //high pass filter - 순간적인 가속도의 증가를 구함
            x = (long) (x - lowPassX);
            y = (long) (y - lowPassY);
            z = (long) (z - lowPassZ);


            double nPitchRad = Math.toRadians(-pitch); // n means negative
            double sinNPitch = Math.sin(nPitchRad);
            double cosNPitch = Math.cos(nPitchRad);
            double nRollRad = Math.toRadians(-roll);
            double sinNRoll = Math.sin(nRollRad);
            double cosNRoll = Math.cos(nRollRad);

            double bx, by;
            bx = x * cosNRoll + z * sinNRoll;
            by = x * sinNPitch * sinNRoll + y * cosNPitch - z * sinNPitch * cosNRoll;
            z = (long) (-x * cosNPitch * sinNRoll + y * sinNPitch * cosNRoll + z * cosNPitch * cosNRoll);

            double nAzimuthRad = Math.toRadians(-azimuth);
            double sinNAzimuth = Math.sin(nAzimuthRad);
            double cosNAzimuth = Math.cos(nAzimuthRad);

            x = (long) (bx * cosNAzimuth - by * sinNAzimuth);
            y = (long) (bx * sinNAzimuth + by * cosNAzimuth);
            //z=0-z;
            //y=0-y;
            //x=0-x;'

            if (x >= -0.2 && x <= 0.2) {
                x = 0;
            }

            if (y >= -0.2 && y <= 0.2) {
                y = 0;
            }
            if (z >= -0.2 && z <= 0.2) {
                z = 0;
            }
            if (z >= 2.0 || z <= -2.0) {
                z = 0;
            }


            long nowTime = System.currentTimeMillis();
            float dt = (nowTime - oldTime) * NS2S;
            if (nowTime != 0) {
                long interval = (long) (nowTime - oldTime);
                velocity += x * dt / 10;  // [cm / s]로
                velocity1 += y * dt / 10;
                velocity2 += z * dt / 10;
                position += velocity * dt / 100;  // [cm] 하는
                position1 += velocity1 * dt / 100;
                position2 += velocity2 * dt / 100;
            }
            oldTime = nowTime;
            Pos data = new Pos(nowTime, x, y, z, position, position1, velocity, velocity1);
            sensorData.add(data);
/*
                if (timestamp != 0) {
                    //dt = (event.timestamp - timestamp);
                    //dt = (event.timestamp - timestamp);
                    //first integration
                /*
                velocity = prev_xVelocity +
                        (prev_xAcceleration +
                                ((x - prev_xAcceleration)/(float)2.0))*dt;
                velocity1 = prev_yVelocity +
                        (prev_yAcceleration +
                                ((y - prev_yAcceleration)/(float)2.0))*dt;

                //second integration
                position = prev_xPosition +
                        (prev_xVelocity+
                                ((velocity - prev_xVelocity)/(float)2.0))*dt;
                position1 = prev_yPosition +
                        (prev_yVelocity+
                                ((velocity1 - prev_yVelocity)/(float)2.0))*dt;
              //
                    velocity += x * dt;
                    velocity1 += y * dt;
                    velocity2 += z * dt;

                    position += velocity * dt;
                    position1 += velocity1 * dt;
                    position2 += velocity2 * dt;

                }
                timestamp = event.timestamp;
*/
            /*
            //remove 0g offset
            x-= xBias;
            y-= yBias;
            */
            //remove noise!
/*
            if(x == 0){
                noAccelerationCount++;
            }
            else{
                noAccelerationCount = 0;
            }

            if(noAccelerationCount > 10) {
                velocity = (float)0.0;
                prev_xVelocity = (float)0.0;
                noAccelerationCount = 0;
            }

            if(y == 0){
                noAccelerationCount1++;
            }
            else{
                noAccelerationCount1 = 0;
            }

            if(noAccelerationCount1 > 10) {
                velocity1 = (float)0.0;
                prev_yVelocity = (float)0.0;
                noAccelerationCount1 = 0;
            }
*/

            //update
                /*
                prev_xAcceleration = x;
                prev_xVelocity     = velocity;
                prev_xPosition     = position;
                prev_yAcceleration = y;
                prev_yVelocity     = velocity1;
                prev_yPosition     = position1;
                prev_zAcceleration = z;
                prev_zVelocity     = velocity2;
                prev_zPosition     = position2;
                */


            mags = null; //retrigger the loop when things are repopulated
            accels = null; ////retrigger the loop when things are repopulated
        }

        //ong last_timestamp = System.currentTimeMillis();
        //float dt = (event.timestamp - last_timestamp) * NS2S;


    }





    //calibration variables
    private float xAccumulator     = 0;
    private float yAccumulator     = 0;
    private float xBias, yBias;
    private int CALIBRATION_COUNT = 5;
    private int calibrationCount = 0;
    private int sampleCount  = 0;

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                btnVelocity.setEnabled(false);

                sensorData = new ArrayList<Pos>();
                // save prev data if available
                started = true;
                Sensor accel = sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, accel,
                        SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);

                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
                btnUpload.setEnabled(true);
                btnVelocity.setEnabled(true);

                started = false;
                sensorManager.unregisterListener(this);
                layout.removeAllViews();
                openChart();
                gravity = new float[]{0, 0, 0};

                //calibrate
                /*
                if(calibrationCount < CALIBRATION_COUNT) {
                    //Make sure the accelerometer has had enough time
                    //to take a new sample.
                    //wait(ACC_SAMPLE_PERIOD);
                    for (Pos data : sensorData) {
                        xAccumulator += data.getX();
                        yAccumulator += data.getY();
                    }
                    calibrationCount++;
                }else if (calibrationCount==5) {
                    xBias = xAccumulator / CALIBRATION_COUNT;
                    yBias = yAccumulator / CALIBRATION_COUNT;
                }else{
                    //take a certain number of samples and then average them to try and reduce some of the noise.
                    int xAccumulator = 0;
                    int yAccumulator = 0;
                    if (sampleCount<=5) {
                        for (Pos data : sensorData) {
                            xAccumulator += data.getX();
                            yAccumulator += data.getY();
                        }
                        sampleCount++;
                    }
*/
                velocity = 0;
                velocity1 = 0;
                velocity2 = 0;
                position = 0;
                position1 = 0;
                position2 = 0;
                x=0;
                y=0;
                z=0;
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                last_timestamp=0;
                sense=0;
                float Rot[]=null; //for gravity rotational data
                //don't use R because android uses that for other stuff
                float I[]=null; //for magnetic rotational data


                azimuth=0;
                pitch=0;
                roll=0;
                break;
            case R.id.btnUpload:
                layout.removeAllViews();
                btnStart.setEnabled(true);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                btnVelocity.setEnabled(true);
                x=0;
                y=0;
                z=0;
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                last_timestamp=0;

                started = false;
                layout.removeAllViews();
                open();
                velocity = 0;
                velocity1 = 0;
                velocity2 = 0;
                position = 0;
                position1 = 0;
                position2 = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                sense=0;
                gravity = new float[]{0, 0, 0};

                break;
            case R.id.btnVelocity:
                layout.removeAllViews();
                btnStart.setEnabled(true);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(true);
                btnVelocity.setEnabled(false);
                openVel();
                velocity = 0;
                velocity1 = 0;
                velocity2 = 0;
                position = 0;
                position1 = 0;
                position2 = 0;

                last_timestamp=0;

                x=0;
                y=0;
                z=0;
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                gravity = new float[]{0, 0, 0};
                sense=0;
                break;
            default:
                break;
        }
    }


    private void openChart() {

        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            //XYSeries zSeries = new XYSeries("Z");
            for (Pos data : sensorData) {
                //if(data.getTimestamp() - t>)
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                //zSeries.add(data.getTimestamp() - t, data.getZ());
            }
            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            //dataset.addSeries(zSeries);
            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

/*
            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);
*/
            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setYTitle("Values of Acceleration");
            multiRenderer.setZoomButtonsVisible(true);

            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            //multiRenderer.addSeriesRenderer(zRenderer);


            // Getting a reference to LinearLayout of the MainActivity Layout


            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }

    }

    private void open() {

        if (sensorData != null || sensorData.size() > 0) {
            XYMultipleSeriesDataset dataset1 = new XYMultipleSeriesDataset();
            XYSeries xSeries = new XYSeries("X");
            for (Pos data : sensorData) {
                xSeries.add(data.getW(), data.getV());
            }
            dataset1.addSeries(xSeries);
            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setZoomButtonsVisible(true);

            multiRenderer.addSeriesRenderer(xRenderer);
            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset1,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }
    }

    private void openVel() {

        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset2 = new XYMultipleSeriesDataset();

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            for (Pos data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getA());
                ySeries.add(data.getTimestamp() - t, data.getB());
            }
            dataset2.addSeries(xSeries);
            dataset2.addSeries(ySeries);
            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            //xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);
            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            //yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            //transparent margins
            //mRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));;

            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            //multiRenderer.setZoomButtonsVisible(true);
            for (int i = 0; i < sensorData.size(); i++) {

                multiRenderer.addXTextLabel(i + 1, ""
                        + (sensorData.get(i).getTimestamp() - t));
            }
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);

            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset2,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }
    }
}



 /*
        public void onSensorChanged(SensorEvent event) {

            if (started) {

                float accels[] = new float[3];
                float mags[] = new float[3];
                float[] values = new float[3];

                switch (event.sensor.getType()) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        mags = event.values.clone();
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        accels = event.values.clone();
                        break;
                }
                if (mags != null && accels != null) {
                    Rot = new float[9];
                    I = new float[9];
                    SensorManager.getRotationMatrix(Rot, I, accels, mags);
                    // Correct if screen is in Landscape

                    float[] outR = new float[9];
                    SensorManager.remapCoordinateSystem(Rot, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                    SensorManager.getOrientation(outR, values);


                    azimuth = (float)Math.toDegrees(values[0]); //looks like we don't need this one
                    pitch =(float)Math.toDegrees(values[1]);
                    roll = (float)Math.toDegrees(values[2]);
                    x = (long) accels[0];
                    y = (long) accels[1];
                    z = (long) accels[2];
                } else {
                    azimuth = 0;
                    pitch = 0;
                    roll = 0;

                }

            long x = (long) event.values[0];
            long y = (long) event.values[1];
            long z = (long) event.values[2];

            //apply low-pass filter
            float oneMinusCoeff = (1.0f - filterCoefficient);

            //x=lowPass(x,prev_xAcceleration);
            //y=lowPass(y,prev_yAcceleration);
            //z=lowPass(z,prev_zAcceleration);

            float lowPassX, lowPassY, lowPassZ;

            //low pass filter
            gravity[0] = filterCoefficient * gravity[0] + oneMinusCoeff * x;
            gravity[1] = filterCoefficient * gravity[1] + oneMinusCoeff * y;
            gravity[2] = filterCoefficient * gravity[2] + oneMinusCoeff * z;
            x = (long)(x - gravity[0]);
            y = (long)(y - gravity[1]);
            z = (long)(z - gravity[2]);

            //high pass filter - 순간적인 가속도의 증가를 구함
            /*
            x = (long) (x - lowPassX);
            y = (long) (y - lowPassY);
            z = (long) (z - lowPassZ);
*/
/*
            double nPitchRad = Math.toRadians(-pitch); // n means negative
            double sinNPitch = Math.sin(nPitchRad);
            double cosNPitch = Math.cos(nPitchRad);
            double nRollRad = Math.toRadians(-roll);
            double sinNRoll = Math.sin(nRollRad);
            double cosNRoll = Math.cos(nRollRad);

            double bx, by;
            bx = x * cosNRoll + z * sinNRoll;
            by = x * sinNPitch * sinNRoll + y * cosNPitch - z * sinNPitch * cosNRoll;
            z = (long) (-x * cosNPitch * sinNRoll + y * sinNPitch * cosNRoll + z * cosNPitch * cosNRoll);

            double nAzimuthRad = Math.toRadians(-azimuth);
            double sinNAzimuth = Math.sin(nAzimuthRad);
            double cosNAzimuth = Math.cos(nAzimuthRad);

            x = (long) (bx * cosNAzimuth - by * sinNAzimuth);
            y = (long) (bx * sinNAzimuth + by * cosNAzimuth);
            //z=0-z;
            //y=0-y;
            //x=0-x;'

            if (x >= -0.2 && x <= 0.2) {
                x = 0;
            }

            if (y >= -0.2 && y <= 0.2) {
                y = 0;
            }
            if (z >= -0.2 && z <= 0.2) {
                z = 0;
            }
            if (z >= 2.0 || z <= -2.0) {
                z = 0;
            }


            long nowTime = System.currentTimeMillis();
            float dt = (nowTime - oldTime) * NS2S;
            if (nowTime != 0) {
                long interval = (long) (nowTime - oldTime);
                velocity += x * dt / 10;  // [cm / s]로
                velocity1 += y * dt / 10;
                velocity2 += z * dt / 10;
                position += velocity * dt;   // [cm] 하는
                position1 += velocity1 * dt ;
                position2 += velocity2 * dt ;
            }
            oldTime = nowTime;
            Pos data = new Pos(nowTime, x, y, z, position, position1, velocity, velocity1);
            sensorData.add(data);
/*
                if (timestamp != 0) {
                    //dt = (event.timestamp - timestamp);
                    //dt = (event.timestamp - timestamp);
                    //first integration
                /*
                velocity = prev_xVelocity +
                        (prev_xAcceleration +
                                ((x - prev_xAcceleration)/(float)2.0))*dt;
                velocity1 = prev_yVelocity +
                        (prev_yAcceleration +
                                ((y - prev_yAcceleration)/(float)2.0))*dt;

                //second integration
                position = prev_xPosition +
                        (prev_xVelocity+
                                ((velocity - prev_xVelocity)/(float)2.0))*dt;
                position1 = prev_yPosition +
                        (prev_yVelocity+
                                ((velocity1 - prev_yVelocity)/(float)2.0))*dt;
              //
                    velocity += x * dt;
                    velocity1 += y * dt;
                    velocity2 += z * dt;

                    position += velocity * dt;
                    position1 += velocity1 * dt;
                    position2 += velocity2 * dt;

                }
                timestamp = event.timestamp;
*/
            /*
            //remove 0g offset
            x-= xBias;
            y-= yBias;
            */
//remove noise!
/*
            if(x == 0){
                noAccelerationCount++;
            }
            else{
                noAccelerationCount = 0;
            }

            if(noAccelerationCount > 10) {
                velocity = (float)0.0;
                prev_xVelocity = (float)0.0;
                noAccelerationCount = 0;
            }

            if(y == 0){
                noAccelerationCount1++;
            }
            else{
                noAccelerationCount1 = 0;
            }

            if(noAccelerationCount1 > 10) {
                velocity1 = (float)0.0;
                prev_yVelocity = (float)0.0;
                noAccelerationCount1 = 0;
            }
*/

//update
                /*
                prev_xAcceleration = x;
                prev_xVelocity     = velocity;
                prev_xPosition     = position;
                prev_yAcceleration = y;
                prev_yVelocity     = velocity1;
                prev_yPosition     = position1;
                prev_zAcceleration = z;
                prev_zVelocity     = velocity2;
                prev_zPosition     = position2;
                */


//mags = null; //retrigger the loop when things are repopulated
//accels = null; ////retrigger the loop when things are repopulated
}

        //ong last_timestamp = System.currentTimeMillis();
        //float dt = (event.timestamp - last_timestamp) * NS2S;


        }


        //
        float Rot[] = null; //for gravity rotational data
        //don't use R because android uses that for other stuff
        float I[] = null; //for magnetic rotational data

        float azimuth;
        float pitch;
        float roll;
        //
        float oldTime = 0;
        long vx, vy, vz = 0;
        long x, y, z = 0;