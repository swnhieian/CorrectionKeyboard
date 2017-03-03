package com.shiweinan.correctionkeyboard;

/**
 * Created by Weinan on 2017/3/3.
 */
enum CorrectionOrder {
    SelectFirst, CorrectFirst
};
enum CorrectionMethod {
    Touch, Select, Move
}
public class Config {
    //screen width: 1080 height: 1812
    public static CorrectionOrder correctionOrder = CorrectionOrder.CorrectFirst;
    public static CorrectionMethod correctionMethod = CorrectionMethod.Touch;
}
