package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.*;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private String text = "";
    private ArrayList<Point> points = new ArrayList<>();
    private float bubbleX = 510;
    private float bubbleY = 50;
    private boolean showBubble = false;
    private KeyboardView kv;

    private int getTextX(int index) {
        return (this.getWidth() / Config.lineCharNum)*(index % Config.lineCharNum);
    }
    private int getTextY(int index) {
        return Config.lineStartY + (int)(Math.floor(index / Config.lineCharNum)) * Config.lineHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paint.setTextSize(50);
        paint.setTypeface(Typeface.MONOSPACE);
        Paint corPaint = new Paint(paint);
        corPaint.setColor(Color.BLUE);
        Paint doPaint = new Paint(paint);
        doPaint.setColor(Color.RED);

        if (showBubble && textStatus != null) {
            for (int i=0; i<textStatus.length; i++) {
                System.out.print(textStatus[i] + " ");
            }
        }
        //34 for one line
        /*int totLine = (text.length() / Config.lineCharNum) + (text.length() % Config.lineCharNum > 0?1:0);
        for (int i=0; i<totLine; i++) {
            String t = text.substring(i*Config.lineCharNum, Math.min((i+1)*Config.lineCharNum, text.length()));
            canvas.drawText(t, 0, Config.lineStartY + i*Config.lineHeight, paint);
        }*/
        for (int i=0; i<text.length(); i++) {
            Paint tempP = paint;
            if (showBubble && textStatus != null) {
                if (textStatus[i] == 1) tempP = corPaint;
                if (textStatus[i] == 2) tempP = doPaint;
            }
            canvas.drawText(String.valueOf(text.charAt(i)), getTextX(i), getTextY(i), tempP);
        }


        /*canvas.drawText("abcdefghijklmnopqrstuvwxyzabcdefgh\nijklmnopqrstuvwxyz", 0, 170, paint);
        canvas.drawText(text, 0, 50, paint);
        canvas.drawText(new StringBuffer(text).reverse().toString(), 0, 110, paint);
        canvas.drawText("ijklmnopqrstuvwxyzabcdefghijklmnop", 0, 230, paint);
        canvas.drawText("qrstuvwxyzabcdefghijklmnopqrstuvwxyz", 0, 290, paint);*/
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (Config.correctionMethod == CorrectionMethod.Touch) {
                    showBubble = true;
                    bubbleX = event.getX();
                    bubbleY = event.getY();
                    matchCorrection();
                    searchCorrection(bubbleX, bubbleY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Config.correctionMethod == CorrectionMethod.Touch) {
                    showBubble = false;
                    bubbleX = 510;
                    bubbleY = 50;
                    matchCorrection();
                    searchCorrection(bubbleX, bubbleY);
                    doCorrection();
                    kv.endCorrectionMode();
                }
                break;
            default:
                break;
        }
        this.postInvalidate();
        return true;
    }

    public void setBubbleDelta(float dx, float dy) {
        bubbleX += dx;
        bubbleY += dy;
        matchCorrection();
        searchCorrection(bubbleX, bubbleY);
        postInvalidate();
    }
    private double getDistance(String pattern, String unk) {
        return 0.2;
    }
    private double pointDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    private double getDistance(List<Point> pattern, List<Point> unk) {
        int lp = pattern.size();
        int lu = unk.size();
        //System.out.println(lp + "x" + lu);
        double inf = 1000000000;
        double[][] f = new double[lp+1][lu+1];
        for (int i=0; i<lp+1; i++) {
            for (int j=0; j<lu+1; j++) {
                f[i][j] = inf;
            }
        }
        f[0][0] = 0;
        for (int i=1; i<lp+1; i++) {
            f[i][0] = inf;
        }
        for (int i=1; i<lu+1; i++) {
            f[0][i] = inf;
        }
        for (int i=1; i<lp+1; i++) {
            for (int j=1; j<lu+1; j++) {
                double temp = inf;
                if (i-1>=0 && j-1>=0 && f[i-1][j-1] < inf)
                    temp = Math.min(f[i-1][j-1] + pointDistance(pattern.get(i-1), unk.get(j-1)), temp);
                if (i-1>=0  && f[i-1][j] < inf)
                    temp = Math.min(f[i-1][j] + 2*pointDistance(pattern.get(i-1), unk.get(j-1)), temp);
                if (j-1>=0  && f[i][j-1] < inf)
                    temp = Math.min(f[i][j-1] + 2*pointDistance(pattern.get(i-1), unk.get(j-1)), temp);
                if (temp < f[i][j]) f[i][j] = temp;
            }
        }
        /*for (int i=0; i<lp+1; i++) {
            for (int j = 0; j < lu + 1; j++) {
                System.out.print(f[i][j] + " ");
            }
            System.out.println();
        }*/
        return f[lp][lu];
    }
    int[] textStatus;
    public void matchCorrection() {
        ArrayList<Point> raw = kv.getRawInputPoints();
        String[] words = text.split(" ");
        textStatus = new int[text.length()];
        for (int i=0; i<text.length(); i++) {
            textStatus[i] = 0;
        }

        int pos = 0;
        for (int i=0; i<words.length; i++) {
            int idx = 0;
            int mLen = 0;
            double min = 1000000000;
            for (int len=1; len<words[i].length()+1; len++) {
                for (int j = 0; j < words[i].length() - len +1; j++) {
                    System.out.println("word:" + text.substring(pos + j, pos + j + len));
                    double d = getDistance(this.points.subList(pos + j, pos + j + len), new ArrayList<Point>(raw));
                    if (d < min) {
                        idx = j;
                        mLen = len;
                        min = d;
                    }
                    System.out.println("correction:" + i + "," + j + "," + d);
                }
            }
            for (int t = pos + idx; t<pos+idx+mLen; t++) {
                textStatus[t] = 1;
            }
            pos += (words[i].length() + 1);
        }
        postInvalidate();

    }

    public void appendText(String text, ArrayList<Point> pointList) {
        this.text += text;
        this.points.addAll(pointList);
        this.points.add(new Point(-1, -1));
        postInvalidate();
    }
    private void unDoCorrection() {
        this.points = lastPoints;
        this.text = lastText;
        this.canUndo = false;
        postInvalidate();
    }
    public void delete() {
        if (canUndo) {
            unDoCorrection();
            return;
        }
        if (this.text.length()<1) return;
        int idx = this.text.substring(0, this.text.length()-1).lastIndexOf(' ');
        if (idx < 0) {this.text = ""; this.points.clear();}
        if (idx > 0) {
            this.text = this.text.substring(0, idx+1);
            this.points = new ArrayList<Point>(this.points.subList(0, idx+1));
        }
        postInvalidate();
    }
    public void setKeyboardView(KeyboardView kbdv) {
        this.kv = kbdv;
    }
    public double getBubbleDistance(int index, float x, float y) {
        int idxx = getTextX(index);
        int idxy = getTextY(index);
        return Math.sqrt(Math.pow(x - idxx, 2) + Math.pow(y - idxy, 2));
    }
    public void searchCorrection(float x, float y) {
        //find nearest corrected char
        int index = 0;
        double minD = 10000000;
        for (int i=0; i<text.length(); i++) {
            if (textStatus[i] > 0) {
                double tempD = getBubbleDistance(i, x, y);
                if (tempD < minD) {
                    minD = tempD;
                    index = i;
                }
                textStatus[i] = 1;
            }
        }
        int st = index;
        int en = index;

        while (st > 0 && textStatus[st-1] == 1) {
            st --;
        }
        while (en < textStatus.length && textStatus[en+1] == 1) {
            en ++;
        }
        for (int i=st; i<=en; i++) {
            textStatus[i] = 2;
        }
        correctionSt = st;
        correctionEd = en;
        postInvalidate();
    }
    private int correctionSt = 0;
    private int correctionEd = 0;
    ArrayList<Point> lastPoints;
    String lastText;
    public boolean canUndo = false;
    private void doCorrection() {
        lastPoints = this.points;
        lastText = this.text;
        canUndo = true;
        String raw = kv.getRawInput();
        ArrayList<Point> rawPoints = kv.getRawInputPoints();
        System.out.println("do correction"+correctionSt+"," + correctionEd);
        this.text = this.text.substring(0, correctionSt) + raw + this.text.substring(correctionEd+1);
        ArrayList<Point> newPoints = new ArrayList<>();
        for (int i=0; i<correctionSt; i++) {
            newPoints.add(this.points.get(i));
        }
        for (int i=0; i<rawPoints.size(); i++) {
            newPoints.add(rawPoints.get(i));
        }
        for (int i=correctionEd+1; i<this.points.size(); i++) {
            newPoints.add(this.points.get(i));
        }
        this.points = newPoints;
    }
    public void setShowBubble(boolean bool) {
        showBubble = bool;
        if (!bool) {
            if (Config.correctionMethod == CorrectionMethod.Slide) {
                searchCorrection(bubbleX, bubbleY);
                doCorrection();
            }
            bubbleX = 510;
            bubbleY = 50;
        }
        postInvalidate();
    }


}
