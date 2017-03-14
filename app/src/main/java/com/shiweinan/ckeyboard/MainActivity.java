package com.shiweinan.ckeyboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditTextProcessor processor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText)findViewById(R.id.editText);
        editText.setShowSoftInputOnFocus(false);
        processor = new EditTextProcessor(editText, this);
        KeyboardView kbdView = (KeyboardView)findViewById(R.id.kbdView);
        kbdView.setProcessor(processor);

        //get sensor data
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(listener, acc, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_FASTEST);

    }
    float[] accV = new float[3];
    float[] magV = new float[3];
    float[] oriV = new float[3];
    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accV =  sensorEvent.values;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magV = sensorEvent.values;
                    break;
                default:
                    break;
            }
            float[] Rot = new float[16];
            SensorManager.getRotationMatrix(Rot, null, accV, magV);
            SensorManager.getOrientation(Rot, oriV);
            //System.out.println(oriV[1]);
            processor.tilt(oriV[1]);
        }



        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    public double getTiltAngle() {
        return oriV[1];
    }
}
