package com.monke.mopermission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import java.util.UUID


open class MoPermission {

    companion object {
        private var sdkInt: Int = -1

        fun getSdkInt(): Int {
            if (sdkInt > 0) {
                return sdkInt
            }
            sdkInt = Build.VERSION.SDK_INT
            return sdkInt
        }

        /**
         * 申请普通权限
         */
        @JvmStatic
        fun requestPermission(
            context: Context,
            title: String,
            yesStr: String,
            noStr: String?,
            listener: OnRequestPermissionListener,
            vararg permission: String
        ) {
            requestPermission(context, title, null, yesStr, noStr, listener, null, *permission)
        }

        @JvmStatic
        fun requestPermission(
            context: Context,
            title: String?,
            desc: String?,
            yesStr: String?,
            noStr: String?,
            listener: OnRequestPermissionListener,
            uiClass: Class<out MoPermissionBaseDialog>?,
            vararg permission: String
        ) {
            if (getSdkInt() < Build.VERSION_CODES.M || permission.isEmpty() || checkPermissions(
                    context,
                    *permission
                )
            ) {
                listener.requestPermission(permission.asList())
            } else {
                requestPermissionActivity(
                    context,
                    false,
                    title,
                    desc,
                    yesStr,
                    noStr,
                    PermissionJustOnceObserver(context, listener),
                    uiClass,
                    *permission
                )
            }
        }

        /**
         * 申请必要权限
         */
        @JvmStatic
        fun requestNecessaryPermission(
            context: Context,
            title: String,
            yesStr: String,
            noStr: String?,
            listener: OnRequestNecessaryPermissionListener,
            vararg permission: String
        ) {
            requestNecessaryPermission(
                context,
                title,
                null,
                yesStr,
                noStr,
                listener,
                null,
                *permission
            )
        }

        @JvmStatic
        fun requestNecessaryPermission(
            context: Context,
            title: String,
            desc: String?,
            yesStr: String?,
            noStr: String?,
            listener: OnRequestNecessaryPermissionListener,
            uiClass: Class<out MoPermissionBaseDialog>?,
            vararg permission: String
        ) {
            if (getSdkInt() < Build.VERSION_CODES.M || permission.isEmpty() || checkPermissions(
                    context,
                    *permission
                )
            ) {
                listener.success(permission.asList())
            } else {
                requestPermissionActivity(
                    context,
                    true,
                    title,
                    desc,
                    yesStr,
                    noStr,
                    NecessaryPermissionJustOnceObserver(context, listener, permission.size),
                    uiClass,
                    *permission,
                )
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        private fun requestPermissionActivity(
            context: Context,
            necessary: Boolean,
            title: String?,
            desc: String?,
            yesStr: String?,
            noStr: String?,
            observer: Observer<List<String>>,
            uiClass: Class<out MoPermissionBaseDialog>?,
            vararg permission: String
        ) {
            //启动权限获取页面
            val key: String = UUID.randomUUID().toString() + System.currentTimeMillis()
            MoPermissionBus.getInstance()!!.registerJustReportOnce(key, observer)
            MoPermissionActivity.start(
                context,
                necessary,
                key,
                title,
                desc,
                yesStr,
                noStr,
                uiClass,
                *permission
            )
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        @JvmStatic
        @SuppressLint("NewApi")
        fun checkPermissions(context: Context, vararg permission: String): Boolean {
            if (permission.isEmpty()) {
                return true
            }
            if (getSdkInt() < Build.VERSION_CODES.M) {
                return true
            }

            for (item in permission) {
                if (!checkPermission(context, item)) {
                    return false
                }
            }
            return true
        }

        @JvmStatic
        @SuppressLint("NewApi")
        fun getNoPermissionLimitOne(context: Context, vararg permission: String): String? {
            if (permission.isEmpty()) {
                return null
            }
            if (getSdkInt() < Build.VERSION_CODES.M) {
                return null
            }

            for (item in permission) {
                if (!checkPermission(context, item)) {
                    return item
                }
            }
            return null
        }

        @JvmStatic
        fun checkPermission(context: Context, permission: String): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true
            } else {
                return when (permission) {
                    Manifest.permission.WRITE_SETTINGS -> {
                        Settings.System.canWrite(context)
                    }

                    Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                        Settings.canDrawOverlays(context)
                    }

                    Manifest.permission.BIND_ACCESSIBILITY_SERVICE -> {
                        (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?)?.let {
                            return it.isEnabled
                        }?:run {
                            return false
                        }
                    }

                    else -> {
                        val result = context.checkSelfPermission(permission)
                        result == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
        }
    }
}