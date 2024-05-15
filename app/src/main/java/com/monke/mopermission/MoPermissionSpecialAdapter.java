package com.monke.mopermission;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

public class MoPermissionSpecialAdapter extends MoPermissionBaseAdapter {
    private MoPermissionBaseAdapter specialPermissionAdapter;

    public void setSpecialPermissionAdapter(MoPermissionBaseAdapter specialPermissionAdapter) {
        this.specialPermissionAdapter = specialPermissionAdapter;
    }

    @Override
    public int isSpecialPermission(String permission) {
        int result = 0;
        if (specialPermissionAdapter != null) {
            result = specialPermissionAdapter.isSpecialPermission(permission);
        }
        if (result != 0) {
            return result;
        }
        if (TextUtils.equals(permission, Manifest.permission.WRITE_SETTINGS)) {
            return 1;
        } else if (TextUtils.equals(permission, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            return 1;
        } else if (TextUtils.equals(permission, Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int checkPermission(Context context, String permission) {
        int result = 0;
        if (specialPermissionAdapter != null) {
            result = specialPermissionAdapter.checkPermission(context, permission);
        }
        if (result != 0) {
            return result;
        }
        if (TextUtils.equals(permission, Manifest.permission.WRITE_SETTINGS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.System.canWrite(context) ? 1 : -1;
            } else {
                return 1;
            }
        } else if (TextUtils.equals(permission, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context) ? 1 : -1;
            } else {
                return 1;
            }
        } else if (TextUtils.equals(permission, Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
            AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (accessibilityManager == null) {
                return -1;
            }
            if (!accessibilityManager.isEnabled()) {
                return -1;
            }
            String temp = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (TextUtils.isEmpty(temp)) {
                return -1;
            }
            String[] splits = temp.split(":");
            if (splits == null || splits.length == 0) {
                return -1;
            }
            for (int i = 0; i < splits.length; i++) {
                String t = splits[i];
                if (TextUtils.isEmpty(t)) {
                    continue;
                }
                int lasIndex = t.indexOf("/");
                if (lasIndex < 0) {
                    lasIndex = t.length();
                }
                t = t.substring(0, lasIndex);
                if (TextUtils.equals(t, context.getPackageName())) {
                    return 1;
                }
            }
            return -1;
        }
        return 0;
    }

    @Override
    public boolean requestPermission(Context context, String permission) {
        if (specialPermissionAdapter != null && specialPermissionAdapter.requestPermission(context, permission)) {
            return true;
        }
        if (TextUtils.equals(permission, Manifest.permission.WRITE_SETTINGS)) {
            context.startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName())));
            return true;
        } else if (TextUtils.equals(permission, Manifest.permission.SYSTEM_ALERT_WINDOW)) {
            //悬浮窗权限 单独处理
            context.startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName())));
            return true;
        } else if (TextUtils.equals(permission, Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
            context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            return true;
        }
        return false;
    }
}
