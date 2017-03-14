package com.shiweinan.ckeyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.widget.EditText;

/**
 * Created by Weinan on 2017/3/14.
 */

public class myEditText extends android.support.v7.widget.AppCompatEditText {
    public myEditText(Context context) {
        super(context);
    }
    public myEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public myEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu) {
    }

}
