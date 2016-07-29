package com.example.omnicns.mot_dead_reckoning;

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
    float x,y,z=0;
    float[] acceleration = null;
    float dt;
    long last_timestamp = 0;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    private float[] gravity = new float[]{0, 0, 0};


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

/*
    float prev_xAcceleration = 0;
    float prev_xVelocity = 0;
    float prev_xPosition = 0;
    float prev_yAcceleration = 0;
    float prev_yVelocity = 0;
    float prev_yPosition = 0;
    float prev_zAcceleration = 0;
    float prev_zVelocity = 0;
    float prev_zPosition = 0;
*/
    //original

    private float filterCoefficient = 0.9f;
    private float timestamp;
    //private int  noAccelerationCount,noAccelerationCount1;
    private int sense = 0;
    private float last_valuesX,last_valuesY,last_valuesZ=0;

    float a= 0.1f;
    float mLowPassX,mLowPassY,mLowPassZ;
    float lowPass(float current,float last) {
        return last * (1.0f - a) + current * a;
    }
    public void onSensorChanged(SensorEvent event) {
        if(started) {
            float dt = (event.timestamp - last_timestamp) * NS2S;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long timestamp = System.currentTimeMillis();
            /*
            mLowPassX = lowPass(x,mLowPassX);
            mLowPassY = lowPass(y,mLowPassY);
            mLowPassZ = lowPass(z,mLowPassZ);
            x=mLowPassX - (float) 0.0188;
            y=mLowPassY - (float) 0.00217;
            z=mLowPassZ + (float) 0.01857;

        }
        else{
            last_valuesX=0;
            last_valuesY=0;
            last_valuesZ=0;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            velocity=(long)0f;
            velocity1=(long)0f;
            velocity2=(long)0f;
            position=(long)0f;
            position1=(long)0f;
            position2=(long)0f;
        }
        velocity+= (x+ last_valuesX)/2 * dt;
        velocity1+= (y + last_valuesY)/2 * dt;
        velocity2+= (z+ last_valuesZ)/2 * dt;

        position+= velocity* dt;
        position1+= velocity1* dt;
        position2+= velocity2* dt;
        last_valuesX=x;
        last_valuesY=y;
        last_valuesZ=z;
        */
            last_timestamp = event.timestamp;
            Pos data = new Pos(timestamp, x, z);
            sensorData.add(data);
        }
    }




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
                /*
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                */
                        last_timestamp=0;
                sense=0;
                /*
                float Rot[]=null; //for gravity rotational data
                //don't use R because android uses that for other stuff
                float I[]=null; //for magnetic rotational data


                azimuth=0;
                pitch=0;
                roll=0;
                break;
                */
            case R.id.btnUpload:
                layout.removeAllViews();
                btnStart.setEnabled(true);
                btnStop.setEnabled(true);
                btnUpload.setEnabled(false);
                btnVelocity.setEnabled(true);
                x=0;
                y=0;
                z=0;
                /*
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                */
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
                /*
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                */
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
                /*
                prev_xAcceleration = 0;
                prev_xVelocity     = 0;
                prev_xPosition     = 0;
                prev_yAcceleration = 0;
                prev_yVelocity     = 0;
                prev_yPosition     = 0;
                prev_zAcceleration = 0;
                prev_zVelocity     = 0;
                prev_zPosition     = 0;
                */
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
                xSeries.add(data.getA(), data.getB());
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
                xSeries.add(data.getTimestamp() - t, data.getW());
                ySeries.add(data.getTimestamp() - t, data.getV());
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

