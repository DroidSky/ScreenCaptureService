package com.rosie.accessibilityservice;

/**
 * Created by ryuji on 2017-04-10.
 */

import android.accessibilityservice.AccessibilityServiceInfo;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.ArrayList;
import java.util.List;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    final static String TAG = "AccessibilityService";

    int lines;

    List<String> latestTexts = new ArrayList<>();
    List<String> nextTexts = new ArrayList<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.d(TAG, "on AccessibilityEvent");

        nextTexts.clear();
        getNextTexts(getRootInActiveWindow());

        if (isTextChanged(latestTexts, nextTexts))
        {
            latestTexts.clear();
            lines = 0;
            getNodeInfoes(getRootInActiveWindow(), 0);
        }
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "OnInterrupt");
    }

    // 접근성 권한 설정 시
    @Override
    public void onServiceConnected() {
        Log.d(TAG, "on Service Connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; // 전체 이벤트 가져오기
        info.feedbackType = AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FEEDBACK_HAPTIC;
        info.notificationTimeout = 1000; // millisecond

        setServiceInfo(info);
    }

    void getNextTexts(AccessibilityNodeInfo node){

        if(node.getText() != null && node.getText().length() > 0 )
            nextTexts.add(node.getText().toString());

        for ( int i = 0 ; i < node.getChildCount() ; i ++){

            AccessibilityNodeInfo child = node.getChild(i);
            if(child == null)
                continue;
            getNextTexts(child);
        }
    }

    boolean isTextChanged(List<String> latestTexts, List<String> nextTexts){

        if(nextTexts.size() <= 0 || latestTexts.size() <= 0 )
            return true;
        for(int i = 0 ; i < latestTexts.size() ; i ++){

            if(!latestTexts.get(i).equals(nextTexts.get(i)))
                return true;
        }
        return false;
    }

    void getNodeInfoes(AccessibilityNodeInfo node, int tab) {

        if (node == null || lines > 100)
            return;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= tab; i++) {
            sb.append("+++");
        }

        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        sb.append(node.getPackageName() + "==" + node.getClassName() + rect);

        if (node.isScrollable() || node.getClassName().toString().contains("ScrollView")) {
            sb.append(" <Scrollable> ");
        }

        if (node.isClickable()) {
            sb.append(" [Clickable] ");
        }

        if (node.getText() != null && node.getText().length() > 0){
            sb.append(" [Text: " + node.getText() + " ]");

            latestTexts.add(node.getText().toString());
        }

        if (node.getContentDescription() != null && node.getContentDescription().length() > 0) {
            sb.append(" [Description: " + node.getContentDescription() + " ]");
        }
        Rect icon = new Rect();
        node.getBoundsInScreen(icon);
        if (icon.width() == icon.height())
            sb.append(" [Maybe icon: " + icon.width() + ", " + icon.height());

        Log.d(TAG, sb.toString());
        lines ++;

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if(lines>100)
                return;
            getNodeInfoes(child, tab + 1);
        }
    }
}