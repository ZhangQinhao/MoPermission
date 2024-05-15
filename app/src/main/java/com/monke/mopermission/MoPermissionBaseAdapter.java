package com.monke.mopermission;

import android.content.Context;

public abstract class MoPermissionBaseAdapter {
    public abstract int isSpecialPermission(String permission);

    public abstract int checkPermission(Context context, String permission);

    public abstract boolean requestPermission(Context context, String permission);
}
