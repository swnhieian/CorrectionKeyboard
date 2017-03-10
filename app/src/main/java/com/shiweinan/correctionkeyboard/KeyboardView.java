package com.shiweinan.correctionkeyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;

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

    int charWidth = 108;
    int charHeight = 167;
    int suggestionHeight = (int)(charHeight*0.8);
    String[] suggestions = new String[] {"", "", ""};

    private void drawKeyboard(Canvas canvas) {
        int backGroundColor = Color.rgb(236,239,241);
        int charColor = Color.rgb(55, 71, 79);
        int suggestionColor = Color.rgb(228, 231, 233);
        Paint p = new Paint();
        p.setColor(suggestionColor);
        canvas.drawRect(0, 0, 1080, suggestionHeight, p);
        Drawable d = getResources().getDrawable(R.drawable.googlekeyboard);

        double scale = 1080.0 / d.getIntrinsicWidth();
        d.setBounds(0, suggestionHeight, 1080, (int)(d.getIntrinsicHeight()*scale) + suggestionHeight);
        d.draw(canvas);

        if (suggestions.length > 0) {//draw suggestions
            p.setColor(Color.rgb(202,208,210));
            int sepPadding = 25;
            p.setStrokeWidth(5);
            canvas.drawLine(360, sepPadding, 360, suggestionHeight-sepPadding, p);
            canvas.drawLine(720, sepPadding, 720, suggestionHeight-sepPadding, p);
            p.setColor(Color.BLACK);
            p.setTextSize(50);
            p.setTypeface(Typeface.MONOSPACE);
            //12 chars for one suggestion
            canvas.drawText(suggestions[1], 540-30*suggestions[1].length()/2, suggestionHeight/2+20, p);
            if (suggestions.length >1) {
                canvas.drawText(suggestions[0],180-30*suggestions[0].length()/2, suggestionHeight/2+20, p);
            }
            if (suggestions.length >2) {
                canvas.drawText(suggestions[2],900-30*suggestions[2].length()/2, suggestionHeight/2+20, p);
            }
        }



        if (false) {
            p = new Paint();
            p.setColor(Color.BLUE);
            p.setStrokeWidth(2);
            canvas.drawLine(0, 0, 1080, 0, p);
            canvas.drawLine(0, 167, 1080, 167, p);
            canvas.drawLine(0, 334, 1080, 334, p);
            canvas.drawLine(0, 501, 1080, 501, p);
            canvas.drawLine(162, 0, 162, 668, p);
            canvas.drawLine(918, 0, 918, 668, p);
        }
    }
    public void setTextView(TextView tv) {
        this.tv = tv;
    }

    private TextView tv;
    private long downTime = 0;
    private float lastX = 0;
    private float lastY = 0;
    private float lastRotX = 0;
    private boolean inCorrectionStatus = false;
    private ArrayList<Point> screenPoints = new ArrayList<>();


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        long time = (new Date()).getTime();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = time;
                break;
            case MotionEvent.ACTION_MOVE:
                //if time > threshold is long touch or gesture
                //then try to manipulate bubble cursor to correct
                //else consider it as a simple touch, ignore it
                if (time - downTime > Config.longPresThreshold && screenPoints.size() > 0) {
                    switch (Config.correctionMethod) {
                        case Tilt:
                            break;
                        case Slide:
                            inCorrectionStatus = true;
                            tv.setShowBubble(true);
                            float deltaX = event.getX() - lastX;
                            float deltaY = event.getY() - lastY;
                            float scaleX = Math.max(tv.bubbleX / event.getX(), (1080 - tv.bubbleX) / (1080-event.getX()));
                            float scaleY = Math.max(tv.bubbleY / event.getY(), (1000 - tv.bubbleY) / (800-event.getY()));
                            tv.setBubbleDelta(deltaX * scaleX, deltaY * scaleY);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (inCorrectionStatus) {
                    endCorrectionMode();
                } else {
                    processType(event.getX(), event.getY());
                    if (tv.canUndo) tv.canUndo = false;
                }
                break;
            default:
                break;
        }
        lastX = event.getX();
        lastY = event.getY();
        return true;
    }
    public void endCorrectionMode() {
        inCorrectionStatus = false;
        tv.setShowBubble(false);
        screenPoints.clear();
        updateSuggestion();
    }
    private boolean isCharArea(float x, float y) {
        if (y > suggestionHeight && y < suggestionHeight + charHeight*2) {
            return true;
        }
        if (y>suggestionHeight+charHeight*2 && y<suggestionHeight+charHeight*3) {
            if (x > 162 && x < 918) return true;
        }
        return false;
    }
    private boolean isDeleteArea(float x, float y) {
        return (y>suggestionHeight+charHeight*2 && y<suggestionHeight+charHeight*3 && x > 918);
    }
    private boolean isSpaceArea(float x, float y) {
        return (y > suggestionHeight +charHeight*3 && x > 270 && x < 1350);
    }
    private boolean isSuggestionArea(float x, float y) {
        return (y < suggestionHeight);
    }
    private void processType(float x, float y) {
        if (isSuggestionArea(x, y)) {
            select((int)(x / 360));
        }
        if (isSpaceArea(x, y)) {
            select(1);
        }
        if (isDeleteArea(x, y)) {
            if (screenPoints.size() > 0) {
                screenPoints.remove(screenPoints.size() - 1);
            } else {
                tv.delete();
            }
        }
        if (isCharArea(x, y)) {
            screenPoints.add(new Point(x, y));
        }
        updateSuggestion();
    }
    public char getRawChar(Point pos) {
        assert(isCharArea(pos.x, pos.y));
        String[] lines = new String[] {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
        int lineNo = (int)((pos.y - suggestionHeight) / charHeight);
        int lineIdx = (int)((pos.x - (lineNo == 2?1.5*108:lineNo*0.5*108)) / 108);
        return lines[lineNo].charAt(lineIdx);
    }
    public String getRawInput() {
        String ret = "";
        for (int i=0; i<screenPoints.size(); i++) {
            ret += getRawChar(screenPoints.get(i));
        }
        return ret;
    }
    public ArrayList<Point> getRawInputPoints() {
        return screenPoints;
    }
    private void updateSuggestion() {
        String raw = getRawInput();
        suggestions[0] = "";
        suggestions[1] = raw;
        suggestions[2] = "";
        postInvalidate();
    }
    private void select(int index) {
        assert(index < 3);
        if (suggestions[index] == "") return;
        tv.appendText(suggestions[index] + " ", screenPoints);
        screenPoints.clear();
        updateSuggestion();
    }
}
