package com.shiweinan.ckeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

/**
 * TODO: document your custom view class.
 */
public class PopupMenu extends PopupWindow {
    public PopupMenu(Context context) {
        super(context);
    }

    public PopupMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopupMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public PopupMenu(View contentView) {
        super(contentView);
    }




}
