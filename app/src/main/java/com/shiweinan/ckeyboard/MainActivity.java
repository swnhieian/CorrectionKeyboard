package com.shiweinan.ckeyboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.*;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditTextProcessor processor;
    EditText editText;
    KeyboardView kbdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText)findViewById(R.id.editText);
        editText.setShowSoftInputOnFocus(false);
        kbdView = (KeyboardView)findViewById(R.id.kbdView);
        processor = new EditTextProcessor(editText, this, kbdView);
        kbdView.setProcessor(processor);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0 && editable.charAt(editable.length() - 1) == '\n') {
                    Logger.submit();
                }

            }
        });



        //get sensor data
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(listener, acc, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_FASTEST);
        //get userName
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        Logger.userName = userName;
        Logger.mainActivity = this;
        Logger.processor = processor;
        android.widget.EditText taskV = (android.widget.EditText)findViewById(R.id.taskText);
        taskV.setFocusable(false);
        Logger.tv = taskV;
        InputStream is = getResources().openRawResource(R.raw.phrases);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        List<String> phrases = new ArrayList<>();
        try {
            while ((line =br.readLine()) != null) {
                phrases.add(line);
            }
            assert(phrases.size() == 500);
            Logger.setAllTask(phrases);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.setPhase(Phase.Practice);
        //write permission
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }
    public void switchKeyboard() {
        editText.setShowSoftInputOnFocus(Logger.currentKbdType == KeyboardType.Google);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        System.out.println("v^v^v^v^v^v^v^v^v^v^");
        if (Logger.currentKbdType == KeyboardType.Google) {
            kbdView.setVisibility(View.INVISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            kbdView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "cannot write file!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.practice:
                Logger.setPhase(Phase.Practice);
                break;
            case R.id.session1:
                Logger.setPhase(Phase.SessionOne);
                break;
            case R.id.session2:
                Logger.setPhase(Phase.SessionTwo);
                break;
            case R.id.session3:
                Logger.setPhase(Phase.SessionThree);
                break;
            case R.id.session4:
                Logger.setPhase(Phase.SessionFour);
                break;
            case R.id.changeKbd:
                Logger.swtichKeyboard();
                break;
            default:
                break;
        }
        return true;
    }
}
