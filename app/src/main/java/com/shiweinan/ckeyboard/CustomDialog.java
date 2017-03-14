package com.shiweinan.ckeyboard;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

/**
 * Created by Weinan on 2017/3/14.
 */

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    android.widget.TextView tv;

    public CustomDialog(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        tv = (android.widget.TextView)findViewById(R.id.txt_dia);

    }

    public void setValue(String[] vals) {
        String str = "";
        for (int i=0; i< vals.length; i++) {
            str += (vals[i] + "\n");
        }
        System.out.println(str);
        tv.setText(str);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                c.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
