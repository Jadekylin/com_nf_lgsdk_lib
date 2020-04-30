package com.madnow.lgsdk.service;

import android.app.Activity;
import android.util.Log;

import com.bytedance.applog.AppLog;
import com.madnow.lgsdk.activity.FullScreenVideoADActivity;
import com.madnow.lgsdk.activity.RewardVideoADActivity;
import com.wogame.cinterface.TeaInterface;

import org.json.JSONObject;

public class TeaService extends TeaInterface {

    private static TeaService mService = null;

    public static TeaService getInstance() {
        if (mService == null) {
            mService = new TeaService();
            mService.setDelegate(mService);
        }
        return mService;
    }

    //region 统计
    public void onEventV3(final String eventName, JSONObject uploadParam){
        AppLog.onEventV3(eventName ,uploadParam);
    }

    public String getAbTestConfig(String key, String defaultValue){
        String value = AppLog.getAbConfig(key, defaultValue);
        Log.e("TEA:" + key, value);
        return value;
    }
    //endregion
}
