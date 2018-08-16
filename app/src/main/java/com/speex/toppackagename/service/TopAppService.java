package com.speex.toppackagename.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Byron on 2018/8/17.
 */

public class TopAppService extends Service {
    private final String TAG = "TopAppService";
    private final int WHAT_DETECT_TOP_PACKAGE_NAME = 1;//启动检测栈顶应用
    private Handler mHandler;
    private int mCycleTime = 500;//轮休时间间隔
    private int mCycleTime2 = mCycleTime * 2;//轮休时间间隔1秒

    private Handler mDetectHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_DETECT_TOP_PACKAGE_NAME:
                    Log.i(TAG, "handleMessage xxxxxxx");
                    try {
                        topStackPackageName();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    mDetectHandler.sendEmptyMessageDelayed(WHAT_DETECT_TOP_PACKAGE_NAME, mCycleTime);
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate 启动服务了");
        mHandler = new Handler(getMainLooper());

        //启动轮休检测栈顶应用服务
//        mDetectHandler.sendEmptyMessage(WHAT_DETECT_TOP_PACKAGE_NAME);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TopBinder();
    }

    public class TopBinder extends Binder {
        public TopAppService getService() {
            return TopAppService.this;
        }
    }

    /**
     * 启动检测栈顶应用轮休
     */
    public void startDetect() {
        mDetectHandler.sendEmptyMessage(WHAT_DETECT_TOP_PACKAGE_NAME);
    }

    /**
     * 停止检测栈顶应用轮休
     */
    public void stopDetect() {
        mDetectHandler.removeMessages(WHAT_DETECT_TOP_PACKAGE_NAME);
    }

    /**
     * 获取当前栈顶应用名称
     *
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    public String topStackPackageName() throws PackageManager.NameNotFoundException {
        // TODO Auto-generated method stub
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);

        //一、第一种方法,使用授权
        String packageName;
        if (Build.VERSION.SDK_INT > 20) {
            Log.i(TAG, "SDK_INT > 20 安卓5.0之后");
            UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService("usagestats");
            long ts = System.currentTimeMillis();
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            UsageStats recentStats = null;
            for (UsageStats usageStats : queryUsageStats) {
                if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                    recentStats = usageStats;
                }
            }
            packageName = recentStats != null ? recentStats.getPackageName() : null;
        } else {
            Log.i(TAG, "SDK_INT < 20 安卓5.0之前");
            // 5.0之前
            // 获取正在运行的任务栈(一个应用程序占用一个任务栈) 最近使用的任务栈会在最前面
            // 1表示给集合设置的最大容量 List<RunningTaskInfo> infos = am.getRunningTasks(1);
            // 获取最近运行的任务栈中的栈顶Activity(即用户当前操作的activity)的包名
            packageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }

        //二、第二种方法
        //据说能成功的方法,在魅族上面测试不通过,在全志H5盒子上测试不通过
//        ComponentName topActivity = mActivityManager.getRunningTasks(1).get(0).topActivity;
//        String packageName = topActivity.getPackageName();

       /* String packageName = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            // 5.0及其以后的版本
            Log.i(TAG, "第二种方法 SDK_INT > 20 安卓5.0之后");
            List<ActivityManager.RunningAppProcessInfo> tasks = mActivityManager.getRunningAppProcesses();
            if (null != tasks && tasks.size() > 0) {
                packageName = tasks.get(0).processName;
                Log.i(TAG, "xxxxxxxxx packageName = " + packageName);
            }
        } else {
            Log.i(TAG, "第二种方法 SDK_INT < 20 安卓5.0之前");
            // 5.0之前
            // 获取正在运行的任务栈(一个应用程序占用一个任务栈) 最近使用的任务栈会在最前面
            // 1表示给集合设置的最大容量 List<RunningTaskInfo> infos = am.getRunningTasks(1);
            // 获取最近运行的任务栈中的栈顶Activity(即用户当前操作的activity)的包名
            packageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }*/

        Log.e(TAG, "当前栈顶的应用名称: " + packageName);


        Context context = getApplicationContext();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        Log.i(TAG, "屏幕是否亮了: isScreenOn = " + isScreenOn);
        //当前的Activity是桌面app
        if (getHomes().contains(packageName)) {
            Log.i(TAG, "当前的Activity是桌面app: " + packageName);
        }
        return packageName;
    }

    /**
     * 获取Lancher应用列表
     *
     * @return
     */
    public List<String> getHomes() {
        // TODO Auto-generated method stub
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        //属性
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
            Log.d(TAG, "Home apps: " + ri.activityInfo.packageName);
        }
        return names;
    }
}
