package com.monke.mopermissionsample;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityMonitorService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();
        int eventType = event.getEventType();
        Log.i("MONKE", "packageName = " + packageName + " eventType = " + eventType);
    }

    @Override
    public void onInterrupt() {
        Log.i("MONKE", "onInterrupt");
    }
}
