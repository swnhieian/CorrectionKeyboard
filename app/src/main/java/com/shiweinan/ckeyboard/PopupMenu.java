package com.shiweinan.ckeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

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
    View contentView;
    MainActivity mainActivity;
    public PopupMenu(MainActivity mainActivity, View contentView, int width, int height) {
        super(contentView, width, height);
        this.contentView = contentView;
        this.mainActivity = mainActivity;

    }

    int maxNum = 0;
    int[] ids = new int[] {R.id.text0, R.id.text1, R.id.text2};
    public void setValue(Word word, String replaceStr) {
        for (int i=0; i<3; i++) {
            if (word.corrections.size() > i) {
                TextView tv = (TextView) contentView.findViewById(ids[i]);
                String res = word.correctResult(word.corrections.get(i), replaceStr);
                tv.setText(res);
            }
        }
        for (int i=word.corrections.size(); i<3; i++) {
            TextView tv = (TextView) contentView.findViewById(ids[i]);
            tv.setVisibility(View.INVISIBLE);
        }
        maxNum = word.corrections.size();
        //selected = 0;
    }
    int selected = 0;
    public int getSelect() {
        return selected;
    }
    public void incSelect() {
        setSelect(selected + 1);
    }
    public void decSelect() {
        setSelect(selected - 1);
    }
    public void setSelect(int i) {
        if (i >= maxNum) i = maxNum - 1;
        if (i < 0) i = 0;
        selected = i;

        for (int idx=0; idx<3; idx++) {
            TextView tv = (TextView)contentView.findViewById(ids[idx]);
            if (idx == i) {
                tv.setBackgroundColor(Color.GREEN);
            } else {
                tv.setBackgroundColor(Color.WHITE);
            }
        }

    }




}


