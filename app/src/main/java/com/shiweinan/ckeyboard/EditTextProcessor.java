package com.shiweinan.ckeyboard;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Pair;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Weinan on 2017/3/13.
 */

public class EditTextProcessor {
    final int lineCharNum = 33;
    final double lineDistThreshold = 30;
    final double inf = 10000000;
    EditText editText;
    List<Word> words;
    int currentLen = 0;
    boolean inCorrecting = false;
    ActionMode.Callback2 aa;
    MainActivity mainActivity;
    public EditTextProcessor(EditText editText, MainActivity mainActivity) {
        this.editText = editText;
        this.mainActivity = mainActivity;
        words = new ArrayList<>();
        if (pm == null) {
            View pmView = mainActivity.getLayoutInflater().inflate(R.layout.sample_popup_menu, null);
            pm = new PopupMenu(mainActivity, pmView, 300, 500);
        }
    }
    private void matchCorrection(List<Point> pntList) {
        for (int i=0; i<words.size(); i++) {
            words.get(i).match(pntList);
        }
        updateView();
    }
    PopupMenu pm = null;
    public void addWord(List<Point> pntList) {
        words.add(new Word(pntList, currentLen));
        currentLen += (pntList.size() + 1);
        updateView();
    }
    public void tilt(double angle) {
        if (!inCorrecting) return;
        if (selectedWordId >=0 && selectedWordId < topCorrections.size() && topCorrections.get(selectedWordId).word.hasMenu()) {
            if (tiltOri - angle> 0.2) {
                pm.setValue(topCorrections.get(selectedWordId).word, correctingStr);
                pm.setSelect((int)((tiltOri - angle - 0.2) / 0.2));
                if (!pm.isShowing()) {
                    Pair<Float, Float> pp = getTextCoordinate(topCorrections.get(selectedWordId).getCenter());
                    pm.showAsDropDown(editText, (int)Math.floor(pp.first), (int)Math.floor(pp.second));
                }
            }
        }
    }
    public void deleteWord() {
        String t = getWholeText();

        if (editText.getSelectionStart() == t.length()) {
            if (words.size() > 0) {
                Word last = words.remove(words.size() - 1);
                currentLen -= (last.size() + 1);
                updateView();
            }
        } else {
            // TODO: add delete part of word
        }
    }
    private String getWholeText() {
        String str = "";
        for (int i=0; i<words.size(); i++) {
            str += words.get(i).getString();
        }
        return str;
    }
    private SpannableStringBuilder text;
    private void updateView() {
        String str = getWholeText();
        text = new SpannableStringBuilder(str);
        if (inCorrecting) {
            for (int i = 0; i < words.size(); i++) {
                Word w = words.get(i);
                if (w.corrections.size() > 0) {
                    ForegroundColorSpan fcs = new ForegroundColorSpan(Color.BLUE);
                    if (w.corrections.size() > 1) {
                        fcs = new ForegroundColorSpan(Color.RED);
                    }
                    text.setSpan(fcs, w.startIndex, w.startIndex + w.size(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
                if (w.id == topCorrections.get(selectedWordId).wordId) { //preview change
                    BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                    text.setSpan(bcs, w.startIndex, w.startIndex + w.size(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    String newS = w.correctResult(w.corrections.get(0), correctingStr);
                    text.replace(w.startIndex, Math.min(w.startIndex + newS.length(), text.length()), newS);
                    UnderlineSpan us = new UnderlineSpan();
                    text.setSpan(us, w.startIndex + w.corrections.get(0).start, w.startIndex + w.corrections.get(0).start+correctingStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
        editText.setText(text);
        editText.setSelection(text.length());
    }
    List<Correction> topCorrections = new ArrayList<>();
    String correctingStr = "";
    List<Point> correctingPnt = new ArrayList<>();
    public void beginCorrection(List<Point> pntList) {
        if (inCorrecting) return;

        matchCorrection(pntList);
        inCorrecting = true;
        correctingStr = Word.getString(pntList);
        correctingPnt = pntList;
        topCorrections.clear();
        for (int i=0; i<words.size(); i++) {
            Word w = words.get(i);
            if (w.corrections.size() > 0) {
                Correction c = w.corrections.get(0);
                topCorrections.add(c);
            }
        }
        if (topCorrections.size() > 0) {
            Pair<Float, Float> p = getTextCoordinate(topCorrections.get(0).getCenter());
            cursorX = p.first;
            cursorY = p.second;
            currentX = cursorX;
            currentY = cursorY;
            selectedWordId = 0;
            tiltOri = mainActivity.getTiltAngle();
            updateView();
        }
    }
    public double tiltOri = -1;
    public void doCorrection() {
        Correction c = topCorrections.get(selectedWordId);
        Word w = c.word;
        if (w.hasMenu() && pm.isShowing()) {
            c = w.corrections.get(pm.getSelect());
        }
        w.doCorrect(c, correctingStr, correctingPnt);
        if (correctingStr.length() - c.end + c.start > 0) {
            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).startIndex > w.startIndex) {
                    words.get(i).startIndex += (correctingStr.length() - (c.end - c.start));
                }
            }
        }
    }
    public void endCorrection() {
        inCorrecting = false;
        doCorrection();
        if (pm.isShowing()) {
            pm.dismiss();
        }
        text.clearSpans();
        editText.setText(text);
        editText.setSelection(text.length());
        updateView();
    }
    public float cursorX, cursorY;
    public float currentX, currentY;
    private double getLineDist(float x, float y, float dx, float dy, float cx, float cy) {
        if ((x - cx)*dx <0 || (y-cy)*dy<0) return inf;
        double c = dx * cy - dy * cx;
        double numerator = Math.abs(dy * x - dx * y + c);
        double denominator = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        return (numerator / denominator);
    }
    private int selectedWordId = -1;
    public void processTouchMove(float x, float y, float lastX, float lastY) {
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        currentX += deltaX;
        currentY += deltaY;
        //System.out.println(String.format("x:%f,y:%f,dx:%f,dy:%f", x, y, deltaX, deltaY));
        //if (Math.abs(deltaX) < 1 && Math.abs(deltaY) < 1) return;
        if (Math.abs(currentX - cursorX) < 20 && Math.abs(currentY - cursorY) < 20) return;
        deltaX = currentX - cursorX;
        deltaY = currentY - cursorY;
        if (Math.abs(deltaX) <0.1 && Math.abs(deltaY)<0.1) return;
        double minD = inf;
        int minI = -1;
        for (int i=0; i<topCorrections.size(); i++) {
            Correction c = topCorrections.get(i);
            Pair<Float, Float> coord = getTextCoordinate(c.getCenter());
            double dist = getLineDist(coord.first, coord.second, deltaX, deltaY, cursorX, cursorY);
            //System.out.println(c.wordId +"("+coord.first+","+coord.second+ ") :" + dist + "(" + selectedWordId + ")");
            topCorrections.get(i).dist = dist;
            if (dist < inf) {
                if (dist < minD && topCorrections.get(i).wordId != topCorrections.get(selectedWordId).wordId) {
                    minD = dist;
                    minI = i;
                }
            }
        }
        if (minI >= 0) {
            selectedWordId = minI;
            tiltOri = mainActivity.getTiltAngle();
            Pair<Float, Float> cursor = getTextCoordinate(topCorrections.get(minI).getCenter());
            cursorX = cursor.first;
            cursorY = cursor.second;
            currentX = cursorX;
            currentY = cursorY;
            System.out.println(topCorrections.get(minI).wordId);
            updateView();
        }

    }
    public Pair<Float, Float> getTextCoordinate(int index) {
        int currentCol = 0;
        int currentRow = 0;
        int totLen = 0;
        for (int i=0; i<words.size(); i++) {
            String str = words.get(i).getString();
            str = str.substring(0, str.length() - 1);
            if (str.length() > lineCharNum - currentCol) {
                currentCol = 0;
                currentRow += 1;
            }
            int spaceAppend = 1;
            if (str.length() == lineCharNum - currentCol) {
                 spaceAppend = 0;
           }
            if (index >= totLen && index <= totLen + str.length()) {
                currentCol += (index - totLen);
                while (currentCol >= lineCharNum && spaceAppend > 0) {
                    currentRow += 1;
                    currentCol -= lineCharNum;
                }
                if (currentCol == lineCharNum && spaceAppend == 0) {
                    currentCol -= 1;
                }
                float py = currentRow * editText.getLineHeight();
                float px = currentCol * (1080 / lineCharNum);
                return new Pair<>(px, py);
            }
            totLen += (str.length() + 1);
            currentCol += (str.length() + spaceAppend);
            while (currentCol > lineCharNum) {
                currentRow += 1;
                currentCol -= lineCharNum;
            }
        }
        return new Pair<>(0f, 0f);
    }
}
