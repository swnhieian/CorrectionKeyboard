package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        KeyboardView kbdView = (KeyboardView)findViewById(R.id.kbdView);
        TextView textView = (TextView)findViewById(R.id.textView);
        kbdView.setTextView(textView);

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Log.i("aaaaa", "屏幕尺寸1: 宽度 = "+display.getWidth()+"高度 = :"+display.getHeight()
        );
    }
}
