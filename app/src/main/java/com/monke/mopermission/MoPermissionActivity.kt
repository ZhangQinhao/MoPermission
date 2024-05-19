package com.monke.mopermission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale

@RequiresApi(Build.VERSION_CODES.M)
class MoPermissionActivity : AppCompatActivity() {

    companion object {
        private const val INTENT_KEY_NECESSARY = "intent_key_necessary"
        private const val INTENT_KEY_RESPONSEKEY = "intent_key_responsekey"
        private const val INTENT_KEY_PERMISSION = "intent_key_permission"

        private const val INTENT_KEY_TITLE = "intent_key_title"  //当必要权限申请，未弹出系统权限框的dialog描述
        private const val INTENT_KEY_DESC = "intent_key_desc"
        private const val INTENT_KEY_YES = "intent_key_yes"
        private const val INTENT_KEY_NO = "intent_key_no"
        private const val INTENT_KEY_UI = "intent_key_ui"

        internal fun start(
            context: Context,
            necessary: Boolean,
            responseKey: String,
            title: String?,
            warnDesc: String?,
            yesStr: String?,
            noStr: String?,
            uiClass: Class<out MoPermissionBaseDialog>?,
            vararg permission: String
        ) {
            val intent = Intent(context, MoPermissionActivity::class.java)
            intent.putExtra(INTENT_KEY_NECESSARY, necessary)
            intent.putExtra(INTENT_KEY_RESPONSEKEY, responseKey)
            intent.putExtra(INTENT_KEY_PERMISSION, permission)
            intent.putExtra(INTENT_KEY_TITLE, title)
            intent.putExtra(INTENT_KEY_DESC, warnDesc)
            intent.putExtra(INTENT_KEY_YES, yesStr)
            intent.putExtra(INTENT_KEY_NO, noStr)
            intent.putExtra(INTENT_KEY_UI, uiClass)
            context.startActivity(intent)
        }
    }

    private var necessary: Boolean = false
    private var responseKey: String? = null
    private var title: String? = null
    private var warnDesc: String? = null
    private var yesStr: String? = null
    private var noStr: String? = null
    private var uiClass: Class<out MoPermissionBaseDialog>? = null
    private val normalPermission = arrayListOf<RequestPermissionData>()
    private val specialPermission = arrayListOf<RequestPermissionData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }
        necessary = intent.getBooleanExtra(INTENT_KEY_NECESSARY, false)
        responseKey = intent.getStringExtra(INTENT_KEY_RESPONSEKEY)
        if (TextUtils.isEmpty(responseKey)) {
            finish()
            return
        }
        var permission = intent.getStringArrayExtra(INTENT_KEY_PERMISSION)
        if (permission == null || permission!!.isEmpty()) {
            finish()
            return
        }
        permission!!.forEach {
            if (MoPermission.moPermissionAdapter.isSpecialPermission(it) > 0) {
                specialPermission.add(RequestPermissionData(it, true))
            } else {
                normalPermission.add(RequestPermissionData(it, false))
            }
        }
        title = intent.getStringExtra(INTENT_KEY_TITLE)
        if (title.isNullOrEmpty()) {
            title = getString(R.string.moperission_warn_default)
        }
        warnDesc = intent.getStringExtra(INTENT_KEY_DESC)
        yesStr = intent.getStringExtra(INTENT_KEY_YES)
        if (yesStr.isNullOrEmpty()) {
            yesStr = getString(R.string.moperission_request)
        }
        noStr = intent.getStringExtra(INTENT_KEY_NO)
        if (noStr.isNullOrEmpty()) {
            noStr = if (necessary) {
                getString(R.string.moperission_exit)
            } else {
                getString(R.string.moperission_cancel)
            }
        }
        uiClass = intent.getSerializableExtra(INTENT_KEY_UI) as Class<out MoPermissionBaseDialog>?
        //检查权限
        checkPermissions()
        //申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //弹出对话框
            showRequestPermissionDialog()
        } else {
            //不用申请权限
            MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
            finish()
            return
        }
    }

    private fun showRequestPermissionDialog() {
        showDialog(title, warnDesc, yesStr, noStr, View.OnClickListener {
            requestPermissions()
            dismissDialog()
        }, View.OnClickListener {
            MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
            dismissDialog()
            finish()
        })
    }

    private fun requestPermissions() {
        if (!needRequestNormalPermission() && !needRequestSpecialPermission()) {
            MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
            dismissDialog()
            finish()
        } else {
            if (requestNormalPermissions()) {
                return
            }
            if (requestSpecialPermissions()) {
                return
            }
            MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
            dismissDialog()
            finish()
        }
    }

    private fun requestNormalPermissions(): Boolean {
        val waitNormalRequest = arrayListOf<String>()
        val waitSystemRequest = arrayListOf<String>()
        normalPermission.forEach {
            if (!it.isEnable && (necessary || it.requestCount == 0)) {
                if (!shouldShowRequestPermissionRationale(
                        this@MoPermissionActivity,
                        it.permission
                    )
                ) {  //正常申请
                    it.accumulateRequestCount()
                    waitNormalRequest.add(it.permission)
                } else {
                    //用户勾选不再提示，需要跳转系统设置页开启
                    waitSystemRequest.add(it.permission)
                }
            }
        }
        if (waitNormalRequest.isNotEmpty()) {
            requestPermissions(waitNormalRequest.toTypedArray(), 0x101)
            return true
        } else if (waitSystemRequest.isNotEmpty()) {
            waitSystemRequest.forEach { i ->
                normalPermission.forEach { j ->
                    if (TextUtils.equals(i, j.permission)) {
                        j.accumulateRequestCount()
                    }
                }
            }
            requestPermissionSetting(this)
            jumpToSystem = true
            return true
        } else {
            return false
        }
    }

    private fun requestSpecialPermissions(): Boolean {
        for (i in 0 until specialPermission.size) {
            var item = specialPermission[i]
            if (!item.isEnable && (necessary || item.requestCount == 0)) {
                item.accumulateRequestCount()
                if (!MoPermission.moPermissionAdapter.requestPermission(this, item.permission)) {
                    requestPermissionSetting(this)
                }
                jumpToSystem = true
                return true
            }
        }
        return false
    }

    private fun checkPermissions() {
        if (normalPermission.isNotEmpty()) {
            normalPermission.forEach {
                it.isEnable = MoPermission.checkPermission(this, it.permission)
            }
        }
        if (specialPermission.isNotEmpty()) {
            specialPermission.forEach {
                it.isEnable = MoPermission.checkPermission(this, it.permission)
            }
        }
    }

    private fun getPermissionEnableList(): ArrayList<String> {
        val result = arrayListOf<String>()
        if (normalPermission.isNotEmpty()) {
            normalPermission.forEach {
                if (it.isEnable) {
                    result.add(it.permission)
                }
            }
        }
        if (specialPermission.isNotEmpty()) {
            specialPermission.forEach {
                if (it.isEnable) {
                    result.add(it.permission)
                }
            }
        }
        return result
    }

    private fun needRequestNormalPermission(): Boolean {
        if (normalPermission.isNotEmpty()) {
            normalPermission.forEach {
                if (!it.isEnable && (necessary || it.requestCount == 0)) {
                    return true
                }
            }
        }
        return false
    }

    private fun needRequestSpecialPermission(): Boolean {
        if (specialPermission.isNotEmpty()) {
            specialPermission.forEach {
                if (!it.isEnable && (necessary || it.requestCount == 0)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 跳转到系统应用权限设置页
     */
    private fun requestPermissionSetting(from: Context) {
        try {
            val localIntent = Intent()
            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 9) {
                localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                localIntent.data = Uri.fromParts("package", from.packageName, null)
            } else if (Build.VERSION.SDK_INT <= 8) {
                localIntent.action = Intent.ACTION_VIEW
                localIntent.setClassName(
                    "com.android.settings",
                    "com.android.settings.InstalledAppDetails"
                )
                localIntent.putExtra("com.android.settings.ApplicationPkgName", from.packageName)
            }
            from.startActivity(localIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var moPermissionDialog: MoPermissionBaseDialog? = null
    private fun showDialog(
        title: String?,
        desc: String?,
        yesStr: String?,
        noStr: String?,
        yesClickListener: View.OnClickListener,
        noClickListener: View.OnClickListener
    ) {
        if (moPermissionDialog == null) {
            moPermissionDialog = if (uiClass == null) {
                MoPermissionDialog(this)
            } else {
                var constructor = uiClass!!.getConstructor(Context::class.java)
                constructor.newInstance(this)
            }
        }
        moPermissionDialog?.show(title, desc, yesStr, noStr, yesClickListener, noClickListener)
    }

    private fun dismissDialog() {
        moPermissionDialog?.dismiss()
    }

    private fun dialogIsShow(): Boolean {
        return moPermissionDialog?.isShowing ?: false
    }

    private var jumpToSystem: Boolean = false
    override fun onResume() {
        super.onResume()
        if (jumpToSystem) {
            jumpToSystem = false
            checkPermissions()
            if (!needRequestNormalPermission() && !needRequestSpecialPermission()) {
                dismissDialog()
                MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
                finish()
                return
            }
            if (!dialogIsShow()) {
                showRequestPermissionDialog()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MoPermissionBus.getInstance()?.removeLiveData(responseKey)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0x101 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for ((index, item) in grantResults.withIndex()) {
                normalPermission.forEach {
                    if (TextUtils.equals(it.permission, permissions[index])) {
                        it.isEnable = item == PackageManager.PERMISSION_GRANTED
                    }
                }
                specialPermission.forEach {
                    if (TextUtils.equals(it.permission, permissions[item])) {
                        it.isEnable = item == PackageManager.PERMISSION_GRANTED
                    }
                }
            }
            if (needRequestNormalPermission() || needRequestSpecialPermission()) {
                if (!dialogIsShow()) {
                    showRequestPermissionDialog()
                }
            } else {
                dismissDialog()
                MoPermissionBus.getInstance()?.sendData(responseKey!!, getPermissionEnableList())
                finish()
            }
        }
    }
}