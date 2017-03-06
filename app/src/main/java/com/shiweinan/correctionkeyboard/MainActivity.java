package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
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
        textView.setKeyboardView(kbdView);


        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Log.i("aaaaa", "屏幕尺寸1: 宽度 = "+display.getWidth()+"高度 = :"+display.getHeight()
        );
        this.setTitle(Config.correctionMethod.toString());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.config, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.touch:
                Config.correctionMethod = CorrectionMethod.Touch;
                break;
            case R.id.slide:
                Config.correctionMethod = CorrectionMethod.Slide;
                break;
            case R.id.tilt:
                Config.correctionMethod = CorrectionMethod.Tilt;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        this.setTitle(Config.correctionMethod.toString());
        return true;
    }
}
