package com.shiweinan.ckeyboard;

import android.widget.*;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Weinan on 2017/3/21.
 */
enum Phase { Practice, SessionOne, SessionTwo, SessionThree, SessionFour};
enum KeyboardType { Google, CKeyboard };
public class Logger {
    final static int sessionSize = 20;
    public static String userName;
    public static android.widget.EditText tv;
    public static MainActivity mainActivity;
    public static EditTextProcessor processor;
    private static Phase currentPhase;
    public static KeyboardType currentKbdType = KeyboardType.CKeyboard;
    private static String[] allTexts;
    private static List<String> tasks = new ArrayList<>();
    private static int currentTaskNo = -1;
    private static String title;
    public Logger() {
        userName = "";
        tasks = new ArrayList<>();
        currentPhase = Phase.Practice;
    }
    public static void setAllTask(List<String> texts) {
        assert(texts.size() == 500);
        allTexts = new String[500];
        for (int i=0; i<texts.size(); i++) {
            allTexts[i] = texts.get(i).toLowerCase();
        }
        loadTask();
    }
    private static void loadTask() {
        List<Integer> index = new ArrayList<>();
        Random rand = new Random();
        int repeat = 1;
        switch (currentPhase) {
            case Practice:
                repeat = 1;
                break;
            case SessionOne:
                repeat = 1;
                break;
            case SessionTwo:
                repeat = 2;
                break;
            case SessionThree:
                repeat = 4;
                break;
            case SessionFour:
                repeat = 8;
                break;
            default:
                break;
        }
        while (index.size() < sessionSize*repeat) {
            int id = rand.nextInt(allTexts.length);
            if (!index.contains(id)) {
                index.add(id);
            }
        }
        tasks.clear();
        for (int i=0; i<sessionSize; i++) {
            String str = "";
            for (int j=0; j<repeat-1; j++) {
                str += (allTexts[index.get(i*repeat+j)] + " ");
            }
            str += allTexts[index.get(i*repeat+repeat-1)];
            tasks.add(str);
        }
        assert(tasks.size() == sessionSize);
        currentTaskNo = 0;
        update();
    }
    public static String getTaskLine() {
        return tasks.get(currentTaskNo);
    }
    private static List<Word> generateError() {
        String[] correctWords = getTaskLine().split(" ");
        List<Word> ret = new ArrayList<>();
        int start = 0;
        for (int i=0; i<correctWords.length; i++) {
            if (correctWords[i].length() == 0) continue;
            List<Point> pntList = new ArrayList<>();
            for (int j=0; j<correctWords[i].length(); j++) {
                Point p = KeyboardView.getCharPoint(correctWords[i].charAt(j));
                pntList.add(p);
            }
            Word w = new Word(pntList, start);
            ret.add(w);
            start += (w.size() + 1);
        }
        return ret;
    }
    public static void update() {
        tv.setText(getTaskLine());
        processor.setInitWords(generateError());
        mainActivity.setTitle(String.format("%s %s : %d / %d", currentKbdType.toString(), title, currentTaskNo + 1, sessionSize));
    }
    public static void submit() {
        //TODO ： deal with result
        currentTaskNo ++;
        if (currentTaskNo == sessionSize) {
            Toast.makeText(mainActivity, "Finish!", Toast.LENGTH_SHORT).show();
            setPhase(Phase.Practice);
        }
        update();
    }
    public static void setPhase(Phase curPhase) {
        Logger.currentPhase = curPhase;
        title = "";
        switch (curPhase) {
            case Practice:
                title = "Practice";
                break;
            case SessionOne:
                title = "Session 1";
                break;
            case SessionTwo:
                title = "Session 2";
                break;
            case SessionThree:
                title = "Session 3";
                break;
            default:
                title = "Error!";
        }
        loadTask();
    }
    public static void swtichKeyboard() {
        if (currentPhase != Phase.Practice && currentTaskNo != 0) return;
        if (currentKbdType == KeyboardType.CKeyboard) {
            currentKbdType = KeyboardType.Google;
        } else {
            currentKbdType = KeyboardType.CKeyboard;
        }
        mainActivity.switchKeyboard();
        update();
    }

}
