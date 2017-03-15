package com.shiweinan.ckeyboard;

/**
 * Created by Weinan on 2017/3/14.
 */

public class Correction {
    public int start;
    public int end;
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
    }
    public Correction(Correction c) {
        this.start = c.start;
        this.end = c.end;
        this.word = new Word(c.word);
        this.value = c.value;
        this.dist = c.dist;
    }
}
