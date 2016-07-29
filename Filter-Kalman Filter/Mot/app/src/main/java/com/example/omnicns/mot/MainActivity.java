package com.example.omnicns.mot;
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
    private Button btnStart, btnStop;
    private boolean started = false;
    private ArrayList<Pos> sensorData;
    private LinearLayout layout;
    private View mChart;
    static final float NS2S = 1.0f / 1000000000.0f;
    float[] last_values = null;
    float velocity, velocity1,velocity2 = 0;
    float position,position1,position2 = 0;
    float[] acceleration = null;
    float dt;
    long last_timestamp = 0;
    SensorManager mSensorManager;
    Sensor mAccelerometer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (LinearLayout) findViewById(R.id.chart_container);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorData = new ArrayList<Pos>();
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
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


    float a= 0.1f;
    float mLowPassX,mLowPassY,mLowPassZ;
    float lowPass(float current,float last) {
        return last * (1.0f - a) + current*a;
    }
    private float filterCoefficient = 0.5f;
    private float[] gravity = new float[] {0,0,0};


    private float timestamp;
    public void onSensorChanged(SensorEvent event) {

        if(started) {
            long last_timestamp = System.currentTimeMillis();
            //float dt = (event.timestamp - last_timestamp) * NS2S;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //apply low-pass filter
            float oneMinusCoeff = (1.0f - filterCoefficient);
            gravity[0] = filterCoefficient * gravity[0] + oneMinusCoeff * x;
            gravity[1] = filterCoefficient * gravity[1] + oneMinusCoeff * y;
            gravity[2] = filterCoefficient * gravity[2] + oneMinusCoeff * z;
            x = event.values[0] - gravity[0];
            y = event.values[1] - gravity[1];
            z = event.values[2] - gravity[2];
            if(timestamp!=0){
                dt = (event.timestamp - timestamp);
                //dt = (event.timestamp - timestamp);
                velocity+= x*dt;
                velocity1+= y*dt;
                velocity2+= z*dt;

                position += velocity*dt;
                position1 += velocity1*dt;
                position2 += velocity2*dt;
            }
            timestamp=event.timestamp;
            Pos data = new Pos(event.timestamp, x,y,z, velocity1,position1);
            sensorData.add(data);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
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
                started = false;
                velocity=0;
                velocity1=0;
                position=0;
                position1=0;
                sensorManager.unregisterListener(this);
                layout.removeAllViews();
                openChart();
                break;
        }
    }

    private void openChart() {

        if (sensorData != null || sensorData.size() > 0) {
            long t = sensorData.get(0).getTimestamp();
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

            /*
            XYSeries xSeries = new XYSeries("X");
            for (Pos data : sensorData) {
                xSeries.add(data.getZ(), data.getV());
            }
            dataset.addSeries(xSeries);
            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            //xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);

            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setXLabels(0);
            multiRenderer.setLabelsColor(Color.RED);
            multiRenderer.setChartTitle("t vs (x,y,z)");
            multiRenderer.setXTitle("Sensor Data");
            multiRenderer.setZoomButtonsVisible(true);

            multiRenderer.addSeriesRenderer(xRenderer);

            */

            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");
            for (Pos data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }
            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);
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
            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);

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
            for (int i = 0; i < 12; i++) {
                multiRenderer.addYTextLabel(i + 1, "" + i);
            }

            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);


            // Getting a reference to LinearLayout of the MainActivity Layout


            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset,
                    multiRenderer);

            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
        }

    }
}
