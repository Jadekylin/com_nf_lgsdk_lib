package com.madnow.lgsdk.service;

import android.app.Activity;
import android.util.Log;

import com.bytedance.applog.AppLog;
import com.madnow.lgsdk.activity.FullScreenVideoADActivity;
import com.madnow.lgsdk.activity.RewardVideoADActivity;
import com.ss.union.game.sdk.LGSDK;
import com.ss.union.login.sdk.LGException;
import com.ss.union.login.sdk.callback.LGAccountBindCallback;
import com.ss.union.login.sdk.callback.LGLoginCallback;
import com.ss.union.login.sdk.callback.LGSwitchAccountCallback;
import com.ss.union.login.sdk.model.User;
import com.wogame.cinterface.LoginCallBack;
import com.wogame.cinterface.TeaInterface;
import com.wogame.common.AppMacros;
import com.wogame.util.GMDebug;

import org.json.JSONObject;

public class TeaService extends TeaInterface {

    private static final String TAG = "LgSdkService";

    private static TeaService mService = null;
    private StringBuilder mBuilder;
    private Activity mActivity;

    public static TeaService getInstance() {
        if (mService == null) {
            mService = new TeaService();
            mService.setDelegate(mService);
        }
        return mService;
    }

    //region 统计
    public void onEventV3(final String eventName, JSONObject uploadParam){
        if(!LgSdkService.mIsInitSdk){
            GMDebug.LogE( "login: is not init sdk");
        }
        AppLog.onEventV3(eventName ,uploadParam);
    }

    public String getAbTestConfig(String key, String defaultValue){
        String value = AppLog.getAbConfig(key, defaultValue);
        Log.e("TEA:" + key, value);
        return value;
    }
    //endregion


    public void login(Activity activity, final LoginCallBack callBack){
        if(!LgSdkService.mIsInitSdk){
            GMDebug.LogE( "login: is not init sdk");
        }
        mActivity = activity;
        if(LGSDK.isVisitor()){
            LGSDK.switchAccount(mActivity,new LGSwitchAccountCallback(){
                @Override
                public void onSuccess(User user) {
                    mBuilder = new StringBuilder();
                    mBuilder.append("{");
                    if(user.nick_name != null){
                        mBuilder.append("\"name\":"+"\""+user.nick_name+"\"");
                        mBuilder.append(",");
                    }
                    if(user.open_id != null){
                        mBuilder.append("\"openid\":"+"\""+user.open_id+"\"");
                        mBuilder.append(",");
                    }
                    if(user.avatar != null){
                        mBuilder.append("\"iconurl\":"+"\""+user.avatar+"\"");
                        mBuilder.append(",");
                    }
                    mBuilder.deleteCharAt(mBuilder.length()-1);
                    mBuilder.append("}");
//                    GMDebug.Log(mBuilder.toString());

                    if(callBack != null){
                        callBack.onCallBack(1, AppMacros.CALL_SUCCESS, mBuilder.toString());
                    }
                }

                @Override
                public void onFail(LGException e) {
                    if(callBack != null){
                        callBack.onCallBack(1, AppMacros.CALL_FALIED,"");
                    }
                }
            });
        }
        else {
            try {
                LGSDK.loginNormal(mActivity, new LGLoginCallback() {
                    @Override
                    public void onSuccess(User user) {
                        //Log.e(TAG, "gameLogin() onSuccess :" + user.nick_name + ",token:" + user.token);
                        mBuilder = new StringBuilder();
                        mBuilder.append("{");
                        if(user.nick_name != null){
                            mBuilder.append("\"name\":"+"\""+user.nick_name+"\"");
                            mBuilder.append(",");
                        }
                        if(user.open_id != null){
                            mBuilder.append("\"openid\":"+"\""+user.open_id+"\"");
                            mBuilder.append(",");
                        }
                        if(user.avatar != null){
                            mBuilder.append("\"iconurl\":"+"\""+user.avatar+"\"");
                            mBuilder.append(",");
                        }
                        mBuilder.deleteCharAt(mBuilder.length()-1);
                        mBuilder.append("}");
//                    GMDebug.Log(mBuilder.toString());

                        if(callBack != null){
                            callBack.onCallBack(1, AppMacros.CALL_SUCCESS, mBuilder.toString());
                        }
                    }

                    @Override
                    public void onFail(LGException e) {
                        Log.e(TAG, "gameLogin() onFail: errorCode =" + e.getError_code() + " errorMsg =" + e.getError_msg());
                        if(callBack != null){
                            callBack.onCallBack(1, AppMacros.CALL_FALIED,"");
                        }
                    }
                });
            }
            catch (Exception e){
                GMDebug.LogE(e.getMessage());
            }
        }



    }


    public boolean isVisitor(Activity activity){
        return LGSDK.isVisitor();
    }
}
