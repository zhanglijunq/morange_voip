package com.moredian.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

/**
 * Used 判断手机ROM,检测ROM是MIUI、EMUI还是Flyme
 */
public class RomUtil {
    private static final String TAG = "Rom";

    public static final String ROM_MIUI = "MIUI";
    public static final String ROM_EMUI = "EMUI";
    public static final String ROM_FLYME = "FLYME";
    public static final String ROM_OPPO = "OPPO";
    public static final String ROM_SMARTISAN = "SMARTISAN";
    public static final String ROM_VIVO = "VIVO";
    public static final String ROM_QIKU = "QIKU";

    private static final String KEY_VERSION_MIUI = "ro.miui.ui.version.name";
    private static final String KEY_VERSION_EMUI = "ro.build.version.emui";
    private static final String KEY_VERSION_OPPO = "ro.build.version.opporom";
    private static final String KEY_VERSION_SMARTISAN = "ro.smartisan.version";
    private static final String KEY_VERSION_VIVO = "ro.vivo.os.version";

    private static String sName;
    private static String sVersion;

    //华为
    public static boolean isEmui() {
        return check(ROM_EMUI);
    }
    //小米
    public static boolean isMiui() {
        return check(ROM_MIUI);
    }
    //vivo
    public static boolean isVivo() {
        return check(ROM_VIVO);
    }
    //oppo
    public static boolean isOppo() {
        return check(ROM_OPPO);
    }
    //魅族
    public static boolean isFlyme() {
        return check(ROM_FLYME);
    }
    //360手机
    public static boolean is360() {
        return check(ROM_QIKU) || check("360");
    }

    public static boolean isSmartisan() {
        return check(ROM_SMARTISAN);
    }

    public static String getName() {
        if (sName == null) {
            check("");
        }
        return sName;
    }

    public static String getVersion() {
        if (sVersion == null) {
            check("");
        }
        return sVersion;
    }

    public static boolean check(String rom) {
        if (sName != null) {
            return sName.equals(rom);
        }

        if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_MIUI))) {
            sName = ROM_MIUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_EMUI))) {
            sName = ROM_EMUI;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_OPPO))) {
            sName = ROM_OPPO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_VIVO))) {
            sName = ROM_VIVO;
        } else if (!TextUtils.isEmpty(sVersion = getProp(KEY_VERSION_SMARTISAN))) {
            sName = ROM_SMARTISAN;
        } else {
            sVersion = Build.DISPLAY;
            if (sVersion.toUpperCase().contains(ROM_FLYME)) {
                sName = ROM_FLYME;
            } else {
                sVersion = Build.UNKNOWN;
                sName = Build.MANUFACTURER.toUpperCase();
            }
        }
        return sName.equals(rom);
    }

    public static String getProp(String name) {
        String line = null;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read prop " + name, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    public static boolean checkAlertWindow(Context context) {
        if (RomUtil.isMiui()){
            return isMiuiFloatWindowOpAllowed(context);
        }else if (RomUtil.isEmui()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(context);
            }
        }else if (RomUtil.isVivo()){
            return getFloatPermissionStatus(context);
        }else if (RomUtil.isOppo()){
            return true;
        }else if (RomUtil.isFlyme()){
            return true;
        }
        return false;
    }

    public static boolean checkShowLockView(Context context) {
        if (RomUtil.isMiui()){
            return isMiuiCanShowLockView(context);
        }else if (RomUtil.isEmui()){
            return true;
        }else if (RomUtil.isOppo()){
            return true;
        }else if (RomUtil.isFlyme()){
            return true;
        }
        return false;
    }

    /**
     * 判断MIUI的后台弹窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isMiuiFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 10021);  // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 判断MIUI的锁屏显示权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isMiuiCanShowLockView(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 10020);  // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static void jumpToPermissionsEditorActivity(Activity activity,int requestCode) {
        if (RomUtil.isMiui()) {
            try {
                // MIUI 8
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", activity.getPackageName());
                activity.startActivityForResult(localIntent,requestCode);
            } catch (Exception e) {
                try {
                    // MIUI 5/6/7
                    Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                    localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                    localIntent.putExtra("extra_pkgname", activity.getPackageName());
                    activity.startActivityForResult(localIntent,requestCode);
                } catch (Exception e1) {
                    // 否则跳转到应用详情
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivityForResult(intent,requestCode);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = manager.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class}
                );
                Integer result = (Integer) method.invoke(manager, op, android.os.Process.myUid(), context.getPackageName());
                LogUtils.e("miui result" + result);
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                LogUtils.e("miui result exception" + e.getMessage());
            }
            return false;
        } else {
            LogUtils.e("Below API 19 cannot invoke!");
        }
        return false;
    }

    /**
     * 获取悬浮窗权限状态
     *
     * @param context
     * @return 1或其他是没有打开，0是打开，该状态的定义和{@link android.app.AppOpsManager#MODE_ALLOWED}，MODE_IGNORED等值差不多，自行查阅源码
     */
    public static boolean getFloatPermissionStatus(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        String packageName = context.getPackageName();
        Uri uri = Uri.parse("content://com.iqoo.secure.provider.secureprovider/allowfloatwindowapp");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            cursor.getColumnNames();
            if (cursor.moveToFirst()) {
                int currentmode = cursor.getInt(cursor.getColumnIndex("currentlmode"));
                cursor.close();
                return 1!=currentmode;
            } else {
                cursor.close();
                return 1!=getFloatPermissionStatus2(context);
            }

        } else {
            return 1!=getFloatPermissionStatus2(context);
        }
    }


    /**
     * vivo比较新的系统获取方法
     *
     * @param context
     * @return
     */
    private static int getFloatPermissionStatus2(Context context) {
        String packageName = context.getPackageName();
        Uri uri2 = Uri.parse("content://com.vivo.permissionmanager.provider.permission/start_bg_activity");
        String selection = "pkgname = ?";
        String[] selectionArgs = new String[]{packageName};
        Cursor cursor = context.getContentResolver().query(uri2, null, selection, selectionArgs, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int currentstate = cursor.getInt(cursor.getColumnIndex("currentstate"));
                cursor.close();
                return currentstate;
            } else {
                cursor.close();
                return 1;
            }
        }
        return 1;
    }


        public static final String HAS_OPEN_SETTING_AUTO_START = "hasOpenSettingAutoStart";//是否已经打开过设置自启动界面的标记，存储起来

        /*打开自启动管理页*/
        public static void openStart(Context context){
            if(Build.VERSION.SDK_INT < 23){
                return;
            }
            Intent intent = new Intent();
            if(RomUtil.isEmui()){//华为
                ComponentName componentName = new ComponentName("com.huawei.systemmanager","com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
                intent.setComponent(componentName);
            }else if(RomUtil.isMiui()){//小米
                ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                intent.setComponent(componentName);
            }else if(RomUtil.isOppo()){//oppo
                ComponentName componentName = null;
                if (Build.VERSION.SDK_INT >=26){
                    componentName =new ComponentName("com.coloros.safecenter","com.coloros.safecenter.startupapp.StartupAppListActivity");
                }else {
                    componentName = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.startup.StartupAppListActivity");
                }
                intent.setComponent(componentName);
                //上面的代码不管用了，因为oppo手机也是手机管家进行自启动管理
            }else if(RomUtil.isVivo()){//Vivo
                ComponentName componentName = null;
                if (Build.VERSION.SDK_INT >=26) {
                    componentName =new ComponentName("com.vivo.permissionmanager","com.vivo.permissionmanager.activity.PurviewTabActivity");
                }else {
                    componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity");
                }
                intent.setComponent(componentName);
            }else if(RomUtil.isFlyme()){//魅族
                // 通过测试，发现魅族是真恶心，也是够了，之前版本还能查看到关于设置自启动这一界面，
                // 系统更新之后，完全找不到了，心里默默Fuck！
                // 针对魅族，我们只能通过魅族内置手机管家去设置自启动，
                // 所以我在这里直接跳转到魅族内置手机管家界面，具体结果请看图
                ComponentName componentName = ComponentName.unflattenFromString("com.meizu.safe" +
                        "/.permission.PermissionMainActivity");
                intent.setComponent(componentName);
            }else {
                // 以上只是市面上主流机型，由于公司你懂的，所以很不容易才凑齐以上设备
                // 针对于其他设备，我们只能调整当前系统app查看详情界面
                // 在此根据用户手机当前版本跳转系统设置界面
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings",
                            "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName",
                            context.getPackageName());
                }
                intent = new Intent(Settings.ACTION_SETTINGS);
            }
            try{
                context.startActivity(intent);
            }catch (Exception e){//抛出异常就直接打开设置页面
                Intent intent1 = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent1);
            }
        }
}