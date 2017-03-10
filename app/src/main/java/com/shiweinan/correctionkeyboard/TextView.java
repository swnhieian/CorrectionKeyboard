package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class TextView extends View {

    public float bubbleXOri = 510;
    public float bubbleYOri = 200;
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
    public float bubbleX = bubbleXOri;
    public float bubbleY = bubbleYOri;
    private boolean showBubble = false;
    private KeyboardView kv;

    private int getTextX(int index) {
        return (this.getWidth() / Config.lineCharNum)*(index % Config.lineCharNum);
    }
    private int getTextY(int index) {
        return Config.lineStartY + (int)(Math.floor(index / Config.lineCharNum)) * Config.lineHeight;
    }
    public boolean showMenu() {
        return showMenu;
    }

    float menuPosX, menuPosY;
    float menuWidth, menuHeight;

    List<CorrectionPair> menuItem = new ArrayList<>();
    private int activeMenuIndex = -1;
    private double inf = 1000000000;


    private double getTextDistance(int t1, int t2) {
        int x1 = getTextX(t1);int x2 = getTextX(t2);
        int y1 = getTextY(t1);int y2 = getTextY(t2);
        return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2));
    }
    List<CorrectionPair> showPair = new ArrayList<>();
    private void drawBackground(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.YELLOW);
        for (int i=0; i<text.length(); i++) {
            double minDis = inf;
            int minIdx = -1;
            for (int j=0; j<showPair.size(); j++) {
                double d = getTextDistance(i, showPair.get(j).getCenter());
                if (d < minDis) {
                    minDis = d;
                    minIdx = j;
                }
            }
            if (minIdx == selectedShowPair && selectedShowPair>=0) {
                p.setAlpha((int)(Math.max(255 - 50.0*minDis/Config.lineHeight, 0)));
                canvas.drawRect(getTextX(i)-this.getWidth() / Config.lineCharNum, getTextY(i)-Config.lineHeight, getTextX(i), getTextY(i), p);
            }
        }
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
        /*for (int i=0; i<text.length(); i++) {
            Paint tempP = paint;
            if (showBubble && textStatus != null) {
                if (textStatus[i] == 1) tempP = corPaint;
                if (textStatus[i] == 2) tempP = doPaint;
            }
            canvas.drawText(String.valueOf(text.charAt(i)), getTextX(i), getTextY(i), tempP);
        }*/
        //draw text background first
        drawBackground(canvas);

        String newText = text;
        if (selectedShowPair >= 0) {
            CorrectionPair tempP = showPair.get(selectedShowPair);
            newText = text.substring(0, tempP.st) + kv.getRawInput() + text.substring(tempP.en+1);
            /*String newText = tempP.word.substring(0, tempP.st - tempP.wordSt+1) + kv.getRawInput() +
                    tempP.word.substring(tempP.en - tempP.wordSt + 1);
            canvas.drawText(newText, getTextX(tempP.wordSt), getTextY(tempP.wordSt), doPaint);*/

        }

        for (int i=0; i<text.length(); i++) {
            Paint tempP = paint;
            if (showBubble && textStatus != null) {
                if (textStatus[i] > 0) tempP = corPaint;
                if (textStatus[i] < 0) tempP = doPaint;
                if (Math.abs(textStatus[i]) == 2) {// draw a triangle
                    //tempP = doPaint;
                    Paint triPaint = new Paint(paint);
                    triPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                    triPaint.setStrokeWidth(2);
                    triPaint.setAntiAlias(true);
                    triPaint.setColor(tempP.getColor());
                    Path p = new Path();
                    p.setFillType(Path.FillType.EVEN_ODD);
                    p.moveTo(getTextX(i)+20, getTextY(i)-8);
                    p.lineTo(getTextX(i)+30, getTextY(i)-8);
                    //p.moveTo(getTextX(i)+25, getTextY(i)-8);
                    p.lineTo(getTextX(i)+25, getTextY(i));
                    //p.moveTo(getTextX(i)+20, getTextY(i));
                    p.lineTo(getTextX(i)+20, getTextY(i)-8);
                    p.close();
                    canvas.drawPath(p, triPaint);
                }
            }
            canvas.drawText(String.valueOf(text.charAt(i)), getTextX(i), getTextY(i), tempP);
        }

        if (selectedShowPair >= 0) {
            ///TODO: draw corrected text
        }


        if (showMenu()) {//draw menu on correctionSt and correctionEd
            menuItem.clear();
            int len = 0;
            for (int i=0; i<pairs.size(); i++) {
                if (pairs.get(i).value > Config.distThreshold && i>= Config.distNumThreshold) break;
                if (pairs.get(i).wordSt<= correctionSt && pairs.get(i).wordEn>=correctionEd) {
                    menuItem.add(pairs.get(i));
                    len = pairs.get(i).wordEn - pairs.get(i).wordSt + 1;
                }
            }
            Paint menuPaint = new Paint();
            menuPaint.setColor(Color.rgb(0, 200, 0));
            menuPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            float charWidth = (this.getWidth() / Config.lineCharNum);
            float menuItemHeight = Config.lineHeight;
            menuWidth = charWidth*len;
            menuHeight = menuItem.size() * menuItemHeight;
            if (menuItem.size() >1) {
                for (int i = 0; i < menuItem.size(); i++) {

                    canvas.drawRect(menuPosX - 20, menuPosY + i * menuItemHeight, menuPosX + 20 + charWidth * len, menuPosY + (i + 1) * menuItemHeight+15, menuPaint);
                }
                for (int i=0; i<menuItem.size(); i++) {
                    //draw firstPart of Text
                    String text = menuItem.get(i).word;
                    String p1 = text.substring(0, menuItem.get(i).st - menuItem.get(i).wordSt);
                    String p2 = text.substring(menuItem.get(i).st - menuItem.get(i).wordSt, menuItem.get(i).en - menuItem.get(i).wordSt + 1);
                    String p3 = text.substring(menuItem.get(i).en - menuItem.get(i).wordSt + 1);
                    canvas.drawText(p1, menuPosX, menuPosY + (i + 1) * menuItemHeight, paint);
                    if (bubbleY > (menuPosY + (i) * menuItemHeight) && bubbleY < (menuPosY + (i + 1) * menuItemHeight)) {
                        activeMenuIndex = i;
                        canvas.drawText(p2, menuPosX + p1.length() * charWidth, menuPosY + (i + 1) * menuItemHeight, doPaint);
                    } else
                        canvas.drawText(p2, menuPosX + p1.length() * charWidth, menuPosY + (i + 1) * menuItemHeight, corPaint);
                    canvas.drawText(p3, menuPosX + (p1.length() + p2.length()) * charWidth, menuPosY + (i + 1) * menuItemHeight, paint);

                }
            }
        }

        /*canvas.drawText("abcdefghijklmnopqrstuvwxyzabcdefgh\nijklmnopqrstuvwxyz", 0, 170, paint);
        canvas.drawText(text, 0, 50, paint);
        canvas.drawText(new StringBuffer(text).reverse().toString(), 0, 110, paint);
        canvas.drawText("ijklmnopqrstuvwxyzabcdefghijklmnop", 0, 230, paint);
        canvas.drawText("qrstuvwxyzabcdefghijklmnopqrstuvwxyz", 0, 290, paint);*/

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
                    bubbleX = bubbleXOri;
                    bubbleY = bubbleYOri;
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
        if (bubbleX < menuPosX || bubbleX > menuPosX + menuWidth || bubbleY < menuPosY || bubbleY>menuPosY + menuHeight) {
            showMenu = false;
        }
        matchCorrection();
        searchCorrection(bubbleX, bubbleY);
        postInvalidate();
    }
    private double pointDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }
    private double getDistance(List<Point> pattern, boolean isFirst, boolean isLast, List<Point> unk) {
        int lp = pattern.size();
        int lu = unk.size();
        if ((kv.getRawChar(pattern.get(0)) != kv.getRawChar(unk.get(0))) && !isFirst) {
            return Config.distThreshold + 150;
        }
        if (kv.getRawChar(pattern.get(lp-1)) != kv.getRawChar(unk.get(lu-1)) && !isLast) {
            return Config.distThreshold + 250;
        }
        if (lp == lu) {
            String ptn = "";
            String uuk = "";
            for (int i=0; i<lp; i++) {
                ptn += kv.getRawChar(pattern.get(i));
                uuk += kv.getRawChar(unk.get(i));
            }
            if (ptn.equals(uuk)) {
                return Config.distThreshold + 100;
            }
        }
        //System.out.println(lp + "x" + lu);
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
                    temp = Math.min(f[i-1][j] + 1.5*Config.lineHeight/*2*pointDistance(pattern.get(i-1), unk.get(j-1))*/, temp);
                if (j-1>=0  && f[i][j-1] < inf)
                    temp = Math.min(f[i][j-1] + 1.5*Config.lineHeight/*2*pointDistance(pattern.get(i-1), unk.get(j-1))*/, temp);
                if (temp < f[i][j]) f[i][j] = temp;
            }
        }
        /*for (int i=0; i<lp+1; i++) {
            for (int j = 0; j < lu + 1; j++) {
                System.out.print(f[i][j] + " ");
            }
            System.out.println();
        }*/
        return f[lp][lu] / ((unk.size()+pattern.size())>>1);
    }
    int[] textStatus;
    List<CorrectionPair> pairs = new ArrayList<>();
    public void matchCorrection() {
        ArrayList<Point> raw = kv.getRawInputPoints();
        String[] words = text.split(" ");
        textStatus = new int[text.length()];
        for (int i=0; i<text.length(); i++) {
            textStatus[i] = 0;
        }

        pairs.clear();
        int pos = 0;
        for (int i=0; i<words.length; i++) {
            int idx = 0;
            int mLen = 0;
            double min = inf;
            for (int len=1; len<words[i].length()+1; len++) {
                for (int j = 0; j < words[i].length() - len +1; j++) {
                    /*System.out.println("word:" + text.substring(pos + j, pos + j + len));*/
                    double d = getDistance(this.points.subList(pos + j, pos + j + len), j==0, j+len==words[i].length(), new ArrayList<Point>(raw));
                    CorrectionPair pair = new CorrectionPair(pos+j,pos+j+len-1, d, i, pos, pos+words[i].length()-1, words[i]);
                    pairs.add(pair);
                    /*if (d < min) {
                        idx = j;
                        mLen = len;
                        min = d;
                    }*/
                    /*System.out.println("correction:" + i + "," + j + "," + d);*/
                }
            }
            /*for (int t = pos + idx; t<pos+idx+mLen; t++) {
                textStatus[t] = 1;
            }*/
            pos += (words[i].length() + 1);
        }
        Collections.sort(pairs, new Comparator<CorrectionPair>() {
            @Override
            public int compare(CorrectionPair correctionPair, CorrectionPair t1) {
                if (correctionPair.value < t1.value) return -1;
                if (correctionPair.value > t1.value) return 1;
                if (correctionPair.en-correctionPair.st > t1.en-t1.st) return -1;
                if (correctionPair.en-correctionPair.st < t1.en-t1.st) return 1;
                return 0;
            }
        });
        System.out.println("====================");
        for (int i=0; i<pairs.size(); i++) {
            System.out.println(pairs.get(i).toString());
        }
        System.out.println("====================");
        //try to fill textStatus
        showPair.clear();
        for (int i=0; i<pairs.size(); i++) {
            CorrectionPair tempP = pairs.get(i);
            if (tempP.value > Config.distThreshold && i>=Config.distNumThreshold) break;
            boolean flag = false;
            for (int t=tempP.wordSt;t<=tempP.wordEn;t++) {
                if (textStatus[t] != 0) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                for (int t=tempP.st; t<=tempP.en; t++) {
                    textStatus[t] = 1;
                }
                pairs.get(i).isShown = true;
                showPair.add(pairs.get(i));
            } else {//flag = true;
                for (int t=tempP.wordEn; t>= tempP.wordSt; t --) {
                    if (textStatus[t] != 0) {
                        textStatus[t] = 2;
                        break;
                    }
                }
            }
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
    private long lastTime = 0;
    private float lastPosX, lastPosY;
    private boolean showMenu = false;
    private int selectedShowPair = -1;
    public void searchCorrection(float x, float y) {
        //find nearest corrected char
        int index = -1;
        double minD = inf;
        for (int i=0; i<showPair.size(); i++) {
            double tempD = getBubbleDistance(showPair.get(i).getCenter(), x, y);
            if (tempD < minD) {
                minD = tempD;
                index = i;
            }
        }
        selectedShowPair = index;/*
        int index = 0;
        double minD = 10000000;
        for (int i=0; i<text.length(); i++) {
            if (textStatus[i] != 0) {
                double tempD = getBubbleDistance(i, x, y);
                if (tempD < minD) {
                    minD = tempD;
                    index = i;
                }
                textStatus[i] = Math.abs(textStatus[i]);
            }
        }
        int st = index;
        int en = index;

        while (st > 0 && textStatus[st-1] != 0) {
            st --;
        }
        while (en < textStatus.length && textStatus[en+1] != 0) {
            en ++;
        }
        for (int i=st; i<=en; i++) {
            textStatus[i] = -Math.abs(textStatus[i]);
        }
        long time = (new Date()).getTime();
        if (!showMenu) {
            if (pointDistance(new Point(lastPosX, lastPosY), new Point(bubbleX, bubbleY)) > Config.showMenuDistThreshold) {
                lastTime = time;
                lastPosX = bubbleX;
                lastPosY = bubbleY;
            } else {
                if (time - lastTime > Config.showMenuTimeThreshold) {
                    showMenu = true;
                    menuPosX = bubbleX - 50;
                    menuPosY = bubbleY - Config.lineHeight / 2;
                }
            }
        }
        correctionSt = st;
        correctionEd = en;*/
        if (index >=0) {
            correctionSt = showPair.get(index).st;
            correctionEd = showPair.get(index).en;
        }
        postInvalidate();
    }
    private int correctionSt = 0;
    private int correctionEd = 0;
    ArrayList<Point> lastPoints;
    String lastText;
    public boolean canUndo = false;
    private void doCorrection() {
        if (showMenu) {
            doCorrection(menuItem.get(activeMenuIndex).st, menuItem.get(activeMenuIndex).en);
            showMenu = false;
        } else {
            doCorrection(correctionSt, correctionEd);
        }
    }
    private void doCorrection(int st, int ed) {
        lastPoints = this.points;
        lastText = this.text;
        canUndo = true;
        String raw = kv.getRawInput();
        ArrayList<Point> rawPoints = kv.getRawInputPoints();
        System.out.println("do correction"+st+"," + ed);
        this.text = this.text.substring(0, st) + raw + this.text.substring(ed+1);
        ArrayList<Point> newPoints = new ArrayList<>();
        for (int i=0; i<st; i++) {
            newPoints.add(this.points.get(i));
        }
        for (int i=0; i<rawPoints.size(); i++) {
            newPoints.add(rawPoints.get(i));
        }
        for (int i=ed+1; i<this.points.size(); i++) {
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
            selectedShowPair = -1;
            bubbleX = bubbleXOri;
            bubbleY = bubbleYOri;
        }
        postInvalidate();
    }


}
