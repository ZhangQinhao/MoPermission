package com.monke.mopermission;

public class RequestPermissionData {
    private final String permission;
    private boolean enable;
    private final boolean isSpecial;
    private int requestCount = 0;

    public RequestPermissionData(String permission, boolean isSpecial) {
        this.permission = permission;
        this.isSpecial = isSpecial;
    }

    public boolean isSpecial() {
        return isSpecial;
    }

    public String getPermission() {
        return permission;
    }

    public void accumulateRequestCount() {
        this.requestCount++;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
