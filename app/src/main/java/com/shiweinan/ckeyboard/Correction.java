package com.shiweinan.ckeyboard;

/**
 * Created by Weinan on 2017/3/14.
 */

public class Correction {
    public int start;
    public int end;
    public int wordStart;
    public int wordId;
    public Word word;
    public double value;
    public double dist;
    public int getCenter() {
        //return (wordStart + ((start + end) >> 1));
        return (word.startIndex + (word.size() >> 1));
    }
    public Correction(int start, int end, double value,Word word) {
        this.start = start;
        this.end = end;
        this.value = value;
        this.word = word;
        this.wordStart = word.startIndex;
        this.wordId = word.id;
    }
}
