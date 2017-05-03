# AppFloatViewHelper

API level >= 19的时候, 使用TYPE_TOAST, 其他情况使用TYPE_PHONE(需要权限).

在manifest.xml里面声明:

uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"

ps：关于不同Rom悬浮窗权限检查 参考 https://github.com/zhaozepeng/FloatWindowPermission


使用：

AppFloatViewHelper floatHelper = new AppFloatViewHelper(applicaton);

View contentView = new ...(application);

floatHelper.setContentView(contentView);


![Image](https://github.com/msilemsile/AppFloatViewHelper/blob/master/demo.gif)
