package com.shiweinan.correctionkeyboard;

/**
 * Created by Weinan on 2017/3/3.
 */
enum CorrectionOrder {
    SelectFirst, CorrectFirst
};
enum CorrectionMethod {
    Touch, Slide, Tilt
}
public class Config {
    //screen width: 1080 height: 1812
    public static CorrectionOrder correctionOrder = CorrectionOrder.CorrectFirst;
    public static CorrectionMethod correctionMethod = CorrectionMethod.Slide;

    public static long longPresThreshold = 150;
    public static int lineCharNum = 34;
    public static int lineStartY = 50;
    public static int lineHeight = 60;

    public static double distThreshold = 300;
    public static int distNumThreshold = 5;
    public static double showMenuDistThreshold = 20;
    public static long showMenuTimeThreshold = 500;
}
