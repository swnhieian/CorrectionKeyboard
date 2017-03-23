package com.shiweinan.ckeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Weinan on 2017/3/13.
 */

public class Word {
    final double matchThreshold = 100;
    final double inf = 100000000;
    final double delta =  1e-8;
    final double lineHeight = 167;
    static int idCount = 0;
    List<Point> pointList;
    List<Correction> corrections;
    int id;
    int startIndex;
    public int alpha = 255;
    public Word(Word w) {
        this.id = w.id;
        this.startIndex = w.startIndex;
        this.alpha = w.alpha;
        this.pointList = new ArrayList<>();
        for (int i=0; i<w.pointList.size(); i++) {
            this.pointList.add(w.pointList.get(i).clone());
        }
        this.corrections = new ArrayList<>();
        for (int i=0; i<w.corrections.size(); i++) {
            Correction cc = w.corrections.get(i);
            this.corrections.add(new Correction(cc.start, cc.end, cc.value, this));
        }
    }
    public Word(List<Point> pntList, int startIndex) {
        pointList = new ArrayList<>();
        for (int i=0; i<pntList.size(); i++) {
            pointList.add(pntList.get(i).clone());
        }
        this.startIndex = startIndex;
        this.id = Word.idCount;
        Word.idCount ++;
    }
    public int size() { return pointList.size(); }
    public String getString() { return getString(pointList) + " "; }
    public static String getString(List<Point> pntList) {
        String ret = "";
        for (int i=0; i<pntList.size(); i++) {
            ret += KeyboardView.getRawChar(pntList.get(i));
        }
        return ret;
    }
    public boolean hasMenu() {
        return (corrections.size() > 1);
    }

    private double getDistance(List<Point> pattern, boolean isFirst, boolean isLast, List<Point> unk) {
            int lp = pattern.size();
            int lu = unk.size();
            /*if ((KeyboardView.getRawChar(pattern.get(0)) != KeyboardView.getRawChar(unk.get(0))) && !isFirst) {
                return matchThreshold + 150;
            }
            if (KeyboardView.getRawChar(pattern.get(lp-1)) != KeyboardView.getRawChar(unk.get(lu-1)) && !isLast) {
                return matchThreshold + 250;
            }*/
            if (lp == lu) {
                String ptn = getString(pattern);
                String uuk = getString(unk);
                if (ptn.equals(uuk)) {
                    return 0;
                    //return matchThreshold + 100;
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
                    double dist = pattern.get(i-1).dist(unk.get(j-1));
                    if (i-1>=0 && j-1>=0 && f[i-1][j-1] < inf)
                        temp = Math.min(f[i-1][j-1] + dist, temp);
                    if (i-1>=0  && f[i-1][j] < inf)
                        temp = Math.min(f[i-1][j] + /*1.5*lineHeight*/2*dist, temp);
                    if (j-1>=0  && f[i][j-1] < inf)
                        temp = Math.min(f[i][j-1] + /*1.5*lineHeight*/2*dist, temp);
                    if (temp < f[i][j]) f[i][j] = temp;
                }
            }
        /*for (int i=0; i<lp+1; i++) {
            for (int j = 0; j < lu + 1; j++) {
                System.out.print(f[i][j] + " ");
            }
            System.out.println();
        }*/
            return f[lp][lu] / ((unk.size()+pattern.size())/2);
    }
    public String correctResult(Correction c, String replace) {
        assert(c.word.id == this.id);
        String ret = this.getString();
        if (Math.abs(c.value) < delta) return (ret.substring(0, c.start) + ret.substring(c.end));
        return (ret.substring(0, c.start) + replace + ret.substring(c.end));
    }
    public void doCorrect(Correction c, String replace, List<Point> pntList) {
        //System.out.println("do correct"+ pointList.size() + "," + c.start +"," + c.end + "," +pntList.size());

        List<Point> newP = new ArrayList<>(pointList);
        newP = newP.subList(0, c.start);
        if (Math.abs(c.value) > delta) {
            newP.addAll(pntList);
        }
        List<Point> lastP = pointList.subList(c.end, pointList.size());
        newP.addAll(lastP);
        pointList = newP;
        //System.out.println(getString());
        //System.out.println("==========");
    }
    public void match(List<Point> user) {
        corrections = new ArrayList<>();
        if (user.size() == 0) return;
        int len = pointList.size();
        for (int l=1; l<pointList.size()+1; l++) {
            for (int start=0; start < len-l+1; start++) {
                double d = getDistance(pointList.subList(start, start + l), start == 0, start + l == len, user);
                if (d < matchThreshold) {
                    corrections.add(new Correction(start, start + l, d, this));
                }
            }
        }
        Collections.sort(corrections, new Comparator<Correction>() {
            @Override
            public int compare(Correction correction, Correction t1) {
                if (correction.value < t1.value) return -1;
                if (correction.value > t1.value) return 1;
                return 0;
            }
        });
    }



}
