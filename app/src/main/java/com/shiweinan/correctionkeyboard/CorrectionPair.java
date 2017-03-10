package com.shiweinan.correctionkeyboard;

/**
 * Created by Weinan on 2017/3/7.
 */

public class CorrectionPair {
    public int st;
    public int en;
    public double value;
    public int wordIndex;
    public int wordSt;
    public int wordEn;
    public String word;
    public boolean isShown = false;
    public int getCenter() {
        return ((st + en) >> 1);
    }
    public CorrectionPair(int start, int end, double value, int wordIndex, int wordSt, int wordEn, String word) {
        this.st = start;
        this.en = end;
        this.value = value;
        this.wordIndex = wordIndex;
        this.wordSt = wordSt;
        this.wordEn = wordEn;
        this.word = word;
    }
    @Override
    public String toString() {
        return String.format("%d - %d, %s", st, en, value);
    }
}
