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
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    boolean inCorrecting = false;
    MainActivity mainActivity;
    KeyboardView kbdView;
    public EditTextProcessor(EditText editText, MainActivity mainActivity, KeyboardView kbdV) {
        this.editText = editText;
        this.mainActivity = mainActivity;
        this.kbdView = kbdV;
        words = new ArrayList<>();
        if (pm == null) {
            View pmView = mainActivity.getLayoutInflater().inflate(R.layout.sample_popup_menu, null);
            pm = new PopupMenu(mainActivity, pmView, 500, 500);
        }
    }
    public boolean canUndo = false;
    List<Word> undoWords = null;
    private void stashUndo() {
        undoWords = new ArrayList<>();
        for (int i=0; i<words.size(); i++) {
            undoWords.add(new Word(words.get(i)));
        }
        /*System.out.println("UUUUUUNNNNNNNNDDDDDDDDDDOOOOOOOOO");
        for (int i=0; i<undoWords.size(); i++) {
            System.out.println(undoWords.get(i).getString());
        }
        System.out.println("========================");*/
        canUndo = true;
    }
    private void undo() {
        if (!canUndo) return;
        words = undoWords;
        /*System.out.println("UUUUUUNNNNNNNNDDDDDDDDDDOOOOOOOOO");
        for (int i=0; i<undoWords.size(); i++) {
            System.out.println(undoWords.get(i).getString());
        }
        System.out.println("========================");*/
        showCorrectionHints(new ArrayList<Point>());
        canUndo = false;
    }
    private void matchCorrection(List<Point> pntList) {
        for (int i=0; i<words.size(); i++) {
            words.get(i).match(pntList);
        }
        updateView(editText.getSelectionStart());
    }
    PopupMenu pm = null;
    private void updateStartIndex() {
        int t = 0;
        for (int i=0; i<words.size(); i++) {
            Word w = words.get(i);
            w.startIndex = t;
            t += w.getString().length();
        }
    }
    public void addWord(List<Point> pntList) {
        String t = getWholeText();
        int st = editText.getSelectionStart();
        int en = editText.getSelectionEnd();
        if (st == t.length()) {
            words.add(new Word(pntList, getWholeText().length()));
            updateView();
        } else {
            if (st < en) { // if there is some text selected, delete first
                deleteWord();
            }//now add text to word
            int wordNo = -1;
            for (int i=0; i<words.size(); i++) {
                Word w = words.get(i);
                if (w.startIndex <= st && st <= w.startIndex + w.size()) {
                    wordNo = i;
                    break;
                }
            }
            if (wordNo >= 0) {
                Word w = words.get(wordNo);
                if (pntList.size() > 0) {
                    w.pointList.addAll(st - w.startIndex, pntList);
                    updateView(st + pntList.size());
                } else {
                    assert(pntList.size() == 0); // add a space
                    if (w.startIndex < st && st < w.startIndex + w.size()) {
                        System.out.println(String.format("st:%d, w:%d-%d", st, w.startIndex, w.startIndex+w.size()));
                        List<Point> p = new ArrayList<>();
                        for (int i=st-w.startIndex; i<w.size(); i++) {
                            p.add(w.pointList.get(i).clone());
                        }
                        for (int i=0; i<p.size(); i++) {
                            w.pointList.remove(st - w.startIndex);
                        }
                        Word newW = new Word(p, st+1);
                        words.add(wordNo+1, newW);
                    } else if (w.startIndex == st ){
                        Word newW = new Word(pntList, st+1);
                        words.add(wordNo, newW);
                    } else {
                        Word newW = new Word(pntList, st+1);
                        words.add(wordNo+1, newW);
                    }
                    updateView(st + 1);

                }
            }

        }
    }
    public void tilt(double angle) {
        if (!inCorrecting) return;
        //System.out.println("====================");
        //System.out.println("ori:" + tiltOri + "angle:" + angle + "delta:" + (tiltOri - angle));
        //System.out.println("====================");
        if (selectedWordId >=0 && selectedWordId < topCorrections.size() && topCorrections.get(selectedWordId).word.hasMenu()) {
            pm.setValue(topCorrections.get(selectedWordId).word, correctingStr);
            if (tiltOri - angle> 0.2) {
                System.out.println((new Date()).getTime());
                System.out.println("+++++++++++++++++++++++++++++111111111");
                pm.incSelect();
                tiltOri = angle;
                if (!pm.isShowing()) {
                    Pair<Float, Float> pp = getTextCoordinate(topCorrections.get(selectedWordId).getCenter());
                    pm.showAsDropDown(editText, (int)Math.floor(pp.first), (int)Math.floor(pp.second) - editText.getHeight() + editText.getLineHeight());
                }
            } else if (tiltOri - angle < -0.2) {
                System.out.println((new Date()).getTime());
                System.out.println("------------------------------111111111");
                pm.decSelect();
                tiltOri = angle;
                if (!pm.isShowing()) {
                    Pair<Float, Float> pp = getTextCoordinate(topCorrections.get(selectedWordId).getCenter());
                    pm.showAsDropDown(editText, (int)Math.floor(pp.first), (int)Math.floor(pp.second) - editText.getHeight() + editText.getLineHeight());
                }
            }

                //System.out.println("*********************************" + (int)((tiltOri - angle - 0.1) / 0.2));
                //pm.setSelect((int)((tiltOri - angle - 0.1) / 0.2));
            }

    }
    public void deleteWord() {
        String t = getWholeText();

        if (editText.getSelectionStart() == t.length()) {
            if (canUndo) {
                undo();
                updateView();
                return;
            }
            if (words.size() > 0) {
                Word last = words.remove(words.size() - 1);
                updateView();
            }
        } else {
            // TODO: add delete part of word
            System.out.println(String.format("%d --> %d",editText.getSelectionStart(), editText.getSelectionEnd()));
            int st = editText.getSelectionStart();
            if (editText.getSelectionStart() == editText.getSelectionEnd()) {
                deletePartOfText(editText.getSelectionStart());
            } else {
                for (int i = editText.getSelectionStart(); i < editText.getSelectionEnd(); i++) {
                    deletePartOfText(st+1);
                }
            }
            updateView(Math.max(st-1, 0));
        }
    }
    private void deletePartOfText(int index) {
        System.out.println("delete part of text: " + index);
        int wordNo = -1;
        boolean needConcat = false;
        for (int i=0; i<words.size(); i++) {
            Word w = words.get(i);
            System.out.println("w:"+w.startIndex +"size:" + w.size());
            if (index > w.startIndex && index<=w.startIndex+ w.size()+1) {
                wordNo = i;
                if (index == w.startIndex + w.size()+1) {
                    needConcat = true;
                }
                break;
            }
        }
        System.out.println(String.format("wordNo:%d,needCant:%b", wordNo, needConcat));
        if (wordNo >= 0) {
            if (needConcat && wordNo < words.size() - 1) {
                words.get(wordNo).pointList.addAll(words.get(wordNo+1).pointList);
                words.remove(wordNo + 1);
            } else if (!needConcat) {
                words.get(wordNo).pointList.remove(index - words.get(wordNo).startIndex - 1);
            }
            for (int i=wordNo + 1; i<words.size(); i++) {
                words.get(i).startIndex --;
            }
        }


    }
    private String getWholeText() {
        String str = "";
        for (int i=0; i<words.size(); i++) {
            str += words.get(i).getString();
        }
        str += kbdView.getScreenString();
        return str;
    }
    private SpannableStringBuilder text;
    public void updateView()
    {
        int l = getWholeText().length();
        updateView(l);
    }
    private void updateView(int cursorPos) {
        updateStartIndex();
        String str = getWholeText();
        String screenStr = kbdView.getScreenString();
        text = new SpannableStringBuilder(str);
        if (screenStr.length() > 0) {
            UnderlineSpan us = new UnderlineSpan();
            text.setSpan(us, text.length() - screenStr.length(), text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        for (int i = 0; i < words.size(); i++) {
            Word w = words.get(i);
            if (w.corrections != null && w.corrections.size() > 0) {
                ForegroundColorSpan fcs = new ForegroundColorSpan(Color.BLUE);
                if (w.corrections.size() > 1) {
                    fcs = new ForegroundColorSpan(Color.RED);
                }
                //text.setSpan(fcs, w.startIndex, w.startIndex + w.size(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                Correction c =  w.corrections.get(0);
                text.setSpan(fcs, w.startIndex + c.start, Math.min(w.startIndex + c.end, text.length()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        if (inCorrecting) {
            for (int i = 0; i < words.size(); i++) {
                Word w = words.get(i);
                /*if (w.corrections.size() > 0) {
                    ForegroundColorSpan fcs = new ForegroundColorSpan(Color.BLUE);
                    if (w.corrections.size() > 1) {
                        fcs = new ForegroundColorSpan(Color.RED);
                    }
                    text.setSpan(fcs, w.startIndex, w.startIndex + w.size(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }*/
                if (selectedWordId >= 0 && selectedWordId < topCorrections.size()) {
                    if (w.id == topCorrections.get(selectedWordId).word.id) { //preview change
                        BackgroundColorSpan bcs = new BackgroundColorSpan(Color.YELLOW);
                        text.setSpan(bcs, w.startIndex, w.startIndex + w.size(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        String newS = w.correctResult(w.corrections.get(0), correctingStr);
                        text.replace(w.startIndex, Math.min(w.startIndex + newS.length(), text.length()), newS);
                        if (newS.length() < w.size()) {//delete str case
                            String t = "";
                            for (int k = 0; k < w.size() - newS.length(); k++) t += " ";
                            text.replace(w.startIndex + newS.length(), w.startIndex + w.size(), t);
                        }
                        UnderlineSpan us = new UnderlineSpan();
                        text.setSpan(us, w.startIndex + w.corrections.get(0).start, w.startIndex + w.corrections.get(0).start + correctingStr.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }
            }
        }
        editText.setText(text);
        editText.setSelection(Math.min(cursorPos, text.length()));
    }
    List<Correction> topCorrections = new ArrayList<>();
    String correctingStr = "";
    List<Point> correctingPnt = new ArrayList<>();
    public void showCorrectionHints(List<Point> pntList) {
        matchCorrection(pntList);
        correctingStr = Word.getString(pntList);
        correctingPnt = pntList;
        topCorrections.clear();
        for (int i = 0; i < words.size(); i++) {
            Word w = words.get(i);
            if (w.corrections.size() > 0) {
                Correction c = w.corrections.get(0);
                topCorrections.add(c);
            }
        }
        updateView(editText.getSelectionStart());

    }
    public void beginCorrection(List<Point> pntList, float xpos, float ypos) {
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
            //use xpos and ypos(mainly xpos) to determine which correction should be select originally
            double tempV  = inf;
            selectedWordId = -1;
            for (int i=0; i<topCorrections.size(); i++) {
                Correction c = topCorrections.get(i);
                if (Math.abs(getTextCoordinate(c.getCenter()).first - xpos) < 108) {
                    if (c.value < tempV) {
                        selectedWordId = i;
                    }
                }
            }
            if (selectedWordId == -1) {
                for (int i=0; i<topCorrections.size(); i++) {
                    if (topCorrections.get(i).value < tempV) {
                        selectedWordId = i;
                    }
                }
            }
            Pair<Float, Float> p = getTextCoordinate(topCorrections.get(selectedWordId).getCenter());
            cursorX = p.first;
            cursorY = p.second;
            currentX = cursorX;
            currentY = cursorY;
            tiltOri = mainActivity.getTiltAngle();
            updateView();
        }
    }
    public double tiltOri = -1;
    public void doCorrection() {
        if (selectedWordId < 0) return;
        if (selectedWordId >= topCorrections.size()) return;
        Correction c = topCorrections.get(selectedWordId);
        Word w = c.word;
        if (w.hasMenu() && pm.isShowing()) {
            c = w.corrections.get(pm.getSelect());
        }
        w.doCorrect(c, correctingStr, correctingPnt);
        int endLen = w.size();
        if (endLen == 0) {
            words.remove(w);
        }
        updateView();
    }
    public void endCorrection() {
        inCorrecting = false;
        stashUndo();
        doCorrection();
        if (pm.isShowing()) {
            pm.dismiss();
        }
        text.clearSpans();
        editText.setText(text);
        editText.setSelection(text.length());
        showCorrectionHints(new ArrayList<Point>());
    }
    public float cursorX, cursorY;
    public float currentX, currentY;
    private double getLineDist(float x, float y, float dx, float dy, float cx, float cy) {
        //System.out.println(String.format("%f,%f->%f,%f --- %f, %f", cx, cy, x, y, dx, dy));
        if ((x - cx)*dx <0 || (y-cy)*dy<0) return inf;
        float a = dx * (x - cx) + dy * (y - cy);
        double d1 = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        double d2 = Math.sqrt(Math.pow(x - cx, 2)+ Math.pow(y - cy, 2));
        if (d1 == 0 || d2 == 0) return inf;
        double cos = Math.abs(Math.acos(a / d1 / d2));
        return cos;
        /*double c = dx * cy - dy * cx;
        double numerator = Math.abs(dy * x - dx * y + c);
        double denominator = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        return (numerator / denominator);*/

    }
    private int selectedWordId = 0;
    public void processTouchMove(float x, float y, float lastX, float lastY) {
        float deltaX = x - lastX;
        float deltaY = y - lastY;
        currentX += deltaX;
        currentY += deltaY;
        //System.out.println(String.format("x:%f,y:%f,dx:%f,dy:%f", x, y, deltaX, deltaY));
        //if (Math.abs(deltaX) < 1 && Math.abs(deltaY) < 1) return;
        deltaX = currentX - cursorX;
        deltaY = currentY - cursorY;
        if (Math.abs(deltaX) <35 && Math.abs(deltaY)<35) return;
        double minD = inf;
        int minI = -1;
        for (int i=0; i<topCorrections.size(); i++) {
            Correction c = topCorrections.get(i);
            Pair<Float, Float> coord = getTextCoordinate(c.getCenter());
            double dist = getLineDist(coord.first, coord.second, deltaX, deltaY, cursorX, cursorY);
            //System.out.println(c.word.id +"("+coord.first+","+coord.second+ ") :" + dist + "(" + selectedWordId + ")");
            topCorrections.get(i).dist = dist;
            if (dist < inf) {
                if (dist < minD && topCorrections.get(i).word.id != topCorrections.get(selectedWordId).word.id) {
                    minD = dist;
                    minI = i;
                }
            }
        }
        if (minI >= 0) {
            if (selectedWordId != minI) {
                if (pm.isShowing()) {
                    pm.dismiss();
                }
            }
            selectedWordId = minI;

        }
        tiltOri = mainActivity.getTiltAngle();
        if (selectedWordId>=0 && selectedWordId<topCorrections.size()) {
            Pair<Float, Float> cursor = getTextCoordinate(topCorrections.get(selectedWordId).getCenter());
            cursorX = cursor.first;
            cursorY = cursor.second;
            currentX = cursorX;
            currentY = cursorY;
        }

        //System.out.println(topCorrections.get(minI).word.id);
        updateView();

    }
    public void setInitWords(List<Word> w) {
        words = w;
        updateView();
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
