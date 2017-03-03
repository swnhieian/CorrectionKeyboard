package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class KeyboardView extends View {

    public KeyboardView(Context context) {
        super(context);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawKeyboard(canvas);


    }

    private void drawKeyboard(Canvas canvas) {
        int backGroundColor = Color.rgb(236,239,241);
        int charColor = Color.rgb(55, 71, 79);
        String line1 = "qwertyuiop";
        for (int i=0; i<line1.length(); i++) {
            //canvas.drawText()
        }
        String line2 = "asdfghjkl";
        String line3 = "zxcvbnm";



    }
}
