package com.shiweinan.ckeyboard;

import android.graphics.Color;
import android.graphics.Typeface;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = (EditText)findViewById(R.id.editText);
        editText.setShowSoftInputOnFocus(false);
        EditTextProcessor processor = new EditTextProcessor(editText, this);
        KeyboardView kbdView = (KeyboardView)findViewById(R.id.kbdView);
        kbdView.setProcessor(processor);
    }
}
