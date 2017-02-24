package com.zmtmt.zhibohao;

import android.app.Activity;
import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/28.
 */
public class MyApplication extends Application {
    private static final String TAG = "ZHIBOHAO";
    public static IWXAPI api;
    public static final String APP_ID = "wx19674a62b3628c8f";
    public static final String APPSECRET = "73259c1134e73bdcc73c07400f574890";
    public static final String URL_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/oauth2/access_token?";
    public static final String URL_REFRESH_TOKEN = "https://api.weixin.qq.com/sns/oauth2/refresh_token?";
    public static final String URL_USERINFO = "https://api.weixin.qq.com/sns/userinfo?";
    public static boolean isInstallWx = false;
    public static ArrayList<Activity> list = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        isInstallWx = isWXAppInstalledAndSupported();
        initLogger();
    }

    private void initLogger() {
        Logger.init(TAG).logLevel(LogLevel.FULL);
    }

    private boolean isWXAppInstalledAndSupported() {
        api = WXAPIFactory.createWXAPI(this, null);
        api.registerApp(APP_ID);

        boolean sIsWXAppInstalledAndSupported = api.isWXAppInstalled() && api.isWXAppSupportAPI();

        return sIsWXAppInstalledAndSupported;
    }


}
