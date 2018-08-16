package com.speex.toppackagename;

import android.app.Application;
import android.content.Intent;

import com.speex.toppackagename.service.TopAppService;

/**
 * Created by Byron on 2018/8/17.
 */

public class MyApplication extends Application {
    public static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService();
    }

    /**
     * 启动检测栈顶应用的服务
     */
    private void startService() {
        Intent intent = new Intent();
        intent.setClass(this, TopAppService.class);
        startService(intent);
    }
}
