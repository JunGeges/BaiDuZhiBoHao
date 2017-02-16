package com.zmtmt.zhibohao.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/2/16.
 */
public class WifiChangeBroadReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiChangeBroadReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        mContext=context;
        getWifiInfo();
    }

    private void getWifiInfo() {
        //通过上下文拿到wifimanager对象
        WifiManager manager=(WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        //通过wifimanager拿到wifiinfo对象
        WifiInfo wifiInfo = manager.getConnectionInfo();
        if (wifiInfo.getBSSID()!=null){
            //wifi名称
            String ssid = wifiInfo.getSSID();
            //wifi信号强度
            int signalLevel = manager.calculateSignalLevel(wifiInfo.getRssi(), 5);
            //wifi速度
            int linkSpeed = wifiInfo.getLinkSpeed();
            //wifi速度单位
            String linkSpeedUnits = wifiInfo.LINK_SPEED_UNITS;
            Toast.makeText(mContext,"wifi名称:"+ssid+"  信号强度:"+signalLevel+"  wifi速度:"+linkSpeed+"  wifi速度单位:"+linkSpeedUnits,Toast.LENGTH_SHORT).show();
        }
    }
}
