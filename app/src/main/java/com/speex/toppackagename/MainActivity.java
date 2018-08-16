package com.speex.toppackagename;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.speex.toppackagename.service.TopAppService;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //绑定服务
        bindTopService();
    }

    private void bindTopService() {
        Intent intent = new Intent(this, TopAppService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private TopAppService mTopAppService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected 服务绑定成功了 ");
            try {
                mTopAppService = ((TopAppService.TopBinder) service).getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected 解绑服务了 ");
            mTopAppService = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkPromission();
    }

    /**
     * 启动栈顶检测服务
     *
     * @param view
     */
    public void start(View view) {
        if (mTopAppService != null) {
            mTopAppService.startDetect();
        }
    }

    /**
     * 停止栈顶检测服务
     *
     * @param view
     */
    public void stop(View view) {
        if (mTopAppService != null) {
            mTopAppService.stopDetect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    /**
     * 检测权限
     */
    public void checkPromission() {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        Log.i(TAG, "checkPromission 当前系统版本: " + currentVersion);
        if (currentVersion > 20) {
            if (!isNoSwitch()) {
                requestPromission();
            } else {
                Log.i(TAG, "安卓系统5.0以后，已经授权了");
            }
        } else {
            Log.i(TAG, "安卓系统5.0以前,直接启动获取栈顶检测");
            if (mTopAppService != null) {
                mTopAppService.startDetect();
            }
        }
    }

    public void requestPromission() {
        new AlertDialog.Builder(this).
                setTitle("设置").
                //setMessage("开启usagestats权限")
                        setMessage(String.format(Locale.US, "晓听请允许应用锁查看应用的使用情况。"))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        startActivity(intent);
                        //finish();
                    }
                }).show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isNoSwitch() {
        long ts = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = (UsageStatsManager) MyApplication.getInstance()
                .getSystemService("usagestats");
        List queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_BEST, 0, ts);
        return !(queryUsageStats == null || queryUsageStats.isEmpty());
    }
}