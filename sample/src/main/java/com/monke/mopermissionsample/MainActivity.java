package com.monke.mopermissionsample;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.monke.mopermission.MoPermission;
import com.monke.mopermission.OnRequestNecessaryPermissionListener;
import com.monke.mopermission.OnRequestPermissionListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //网络权限测试
        initNet();
        //手机信息权限测试
        initPhone();
        //手机信息和SD卡读写权限测试
        initSDandPhone();
        //sd卡麦克风悬浮窗权限测试
        initSDMICWINDOW();
        //摄像头与发送短信权限测试(自定义二次确认弹窗UI)
        initCameraMSG();
    }

    private void initCameraMSG() {
        findViewById(R.id.tv_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestPermission(MainActivity.this, "权限申请", "获取摄像头与短信发送权限", "申请", "取消", new OnRequestPermissionListener() {
                    @Override
                    public void requestPermission(List<String> permissions) {
                        if (permissions.size() >= 2) {
                            showToast("已获取摄像头,短信发送权限");
                        } else {
                            if (permissions.size() == 0) {
                                showToast("未获取摄像头,短信发送权限");
                            } else {
                                String temp = "已获取";
                                if (permissions.contains(Manifest.permission.CAMERA)) {
                                    temp += "摄像头";
                                }
                                if (permissions.contains(Manifest.permission.SEND_SMS)) {
                                    temp += "短信发送";
                                }
                                temp += "权限";
                                showToast(temp);
                            }
                        }
                    }
                }, CustomPermissionDialog.class, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS);
            }
        });
        findViewById(R.id.tv_5_necessary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "权限申请", "获取摄像头与短信发送权限", "申请", "退出", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        showToast("已获取读取SD卡,麦克风,悬浮窗权限");
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("未获取读取SD卡,麦克风,悬浮窗权限！可以关闭相关功能");
                    }
                }, CustomPermissionDialog.class, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS);
            }
        });
    }

    private void initSDMICWINDOW() {
        findViewById(R.id.tv_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestPermission(MainActivity.this, "权限申请", "获取读取SD卡,麦克风,悬浮窗权限", "申请", "取消", new OnRequestPermissionListener() {
                    @Override
                    public void requestPermission(List<String> permissions) {
                        if (permissions.size() >= 3) {
                            showToast("已获取读取SD卡,麦克风,悬浮窗权限");
                        } else {
                            if (permissions.size() == 0) {
                                showToast("未获取读取SD卡,麦克风,悬浮窗权限");
                            } else {
                                String temp = "已获取";
                                if (permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    temp += "SD卡";
                                }
                                if (permissions.contains(Manifest.permission.RECORD_AUDIO)) {
                                    temp += "麦克风";
                                }
                                if (permissions.contains(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                                    temp += "悬浮窗";
                                }
                                temp += "权限";
                                showToast(temp);
                            }
                        }
                    }
                }, null, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW);
            }
        });
        findViewById(R.id.tv_4_necessary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "权限申请", "获取读取SD卡,麦克风,悬浮窗权限", "申请", "退出", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        showToast("已获取读取SD卡,麦克风,悬浮窗权限");
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("未获取读取SD卡,麦克风,悬浮窗权限！可以关闭相关功能");
                    }
                }, null, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW);
            }
        });
    }

    private void initSDandPhone() {
        findViewById(R.id.tv_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestPermission(MainActivity.this, "权限申请", "获取读取SD卡,麦克风,悬浮窗权限", "申请", "取消", new OnRequestPermissionListener() {
                    @Override
                    public void requestPermission(List<String> permissions) {
                        if (permissions.size() >= 2) {
                            showToast("已获取读取手机信息权限和SD卡权限");
                        } else {
                            if (permissions.size() == 0) {
                                showToast("未获取手机信息权限和SD卡权限");
                            } else {
                                if (permissions.contains(Manifest.permission.READ_PHONE_STATE)) {
                                    showToast("未获取SD卡权限");
                                } else {
                                    showToast("未获取手机信息权限");
                                }
                            }
                        }
                    }
                }, null, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });
        findViewById(R.id.tv_3_necessary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "权限获取", "获取手机信息和SD卡权限", "申请", "退出", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        showToast("已获取读取手机信息权限和SD卡权限");
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("未获取读取手机信息权限和SD卡权限！可以关闭相关功能");
                    }
                }, null, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        });
    }

    private void initPhone() {
        findViewById(R.id.tv_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestPermission(MainActivity.this, "获取手机信息权限", "申请", "返回", new OnRequestPermissionListener() {
                    @Override
                    public void requestPermission(List<String> permissions) {
                        if (permissions.size() >= 1) {
                            showToast("已获取读取手机信息权限");
                        } else {
                            showToast("未获取读取手机信息权限！");
                        }
                    }
                }, Manifest.permission.READ_PHONE_STATE);
            }
        });
        findViewById(R.id.tv_2_necessary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "获取读取手机信息权限", "申请", "退出", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        showToast("已获取读取手机信息权限");
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("未获取读取手机信息权限！可以关闭相关功能");
                    }
                }, Manifest.permission.READ_PHONE_STATE);
            }
        });
    }

    private void initNet() {
        findViewById(R.id.tv_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestPermission(MainActivity.this, "获取网络权限", "申请", "返回", new OnRequestPermissionListener() {
                    @Override
                    public void requestPermission(List<String> permissions) {
                        if (permissions.size() >= 1) {
                            showToast("已获取网络权限");
                        } else {
                            showToast("未获取网络权限！");
                        }
                    }
                }, Manifest.permission.INTERNET);
            }
        });
        findViewById(R.id.tv_1_necessary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoPermission.Companion.requestNecessaryPermission(MainActivity.this, "获取网络权限", "申请", "退出", new OnRequestNecessaryPermissionListener() {
                    @Override
                    public void success(List<String> permissions) {
                        showToast("已获取网络权限");
                    }

                    @Override
                    public void fail(List<String> permissions) {
                        showToast("未获取网络权限！可以关闭相关功能");
                    }
                }, Manifest.permission.INTERNET);
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
