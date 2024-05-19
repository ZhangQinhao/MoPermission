# MoPermission
最方便的安卓权限申请框架
  
### 引入

在build.gradle引入  `implementation 'io.github.zhangqinhao:MoPermission:1.0.8'`

### 使用
![enter description here][1]

#### 非必要权限获取（默认申请权限，所有权限只申请一次）
``` stylus
MoPermission.Companion.requestPermission(MainActivity.this, new OnRequestPermissionListener() {
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
```


#### 申请必要权限 (如果用户未获取相关权限则弹窗提示再次申请，如果用户选取取消且不再提示，则手动申请后跳转到系统权限设置页，只能通过弹窗取消退出)
``` stylus
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
```

#### 自定义特殊权限适配器(由于权限过多，可以通过注册权限适配器来扩展框架中不支持的权限)
``` stylus
MoPermission.registerPermissionAdapter(new MoPermissionBaseAdapter() {
            @Override
            public int isSpecialPermission(String permission) {
                if (TextUtils.equals(permission, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                    return 1;
                }
                return 0;
            }

            @Override
            public int checkPermission(Context context, String permission) {
                if (TextUtils.equals(permission, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        return ((PowerManager) getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(context.getPackageName()) ? 1 : -1;
                    } else {
                        return 1;
                    }
                }
                return 0;
            }

            @Override
            public boolean requestPermission(Context context, String permission) {
                if (TextUtils.equals(permission, Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                    try {
                        Intent intent = new Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
```


#### 弹窗样式自定义修改
继承实现 MoPermissionBaseDialog 相关方法并定制UI，可参考MoPermissionDialog或者Sample中CustomPermissionDialog  
注意：必须实现show(String titleStr, String descStr, String yesStr, String noStr, View.OnClickListener yesClickListener, View.OnClickListener noClickListener)  
申请的点击事件需要执行clickToRequest(),取消/退出的点击事件需要执行clickToCancel()


具体用法参照Sample代码

[1]: ./images/1.gif "1.gif"
