package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class TextView extends View {

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private String text = "I am tring to apply a very loooooooooooooooong string and draw it on the canvas.";
    private float bubbleX, bubbleY;
    private boolean showBubble = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.MONOSPACE);

        //34 for one line
        canvas.drawText("abcdefghijklmnopqrstuvwxyzabcdefgh\nijklmnopqrstuvwxyz", 0, 170, paint);
        canvas.drawText(text, 0, 50, paint);
        canvas.drawText(new StringBuffer(text).reverse().toString(), 0, 110, paint);
        canvas.drawText("ijklmnopqrstuvwxyzabcdefghijklmnop", 0, 230, paint);
        canvas.drawText("qrstuvwxyzabcdefghijklmnopqrstuvwxyz", 0, 290, paint);
        // TODO: consider storing these as member variables to reduce
        Paint paintCursor = new Paint();
        paintCursor.setColor(Color.argb(150, 0, 200, 200));
        paintCursor.setStrokeWidth(2);
        if (showBubble) {
            canvas.drawCircle(bubbleX, bubbleY, 80, paintCursor);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        showBubble = true;
        bubbleX = event.getX();
        bubbleY = event.getY();
        this.postInvalidate();
        return true;
    }


}
