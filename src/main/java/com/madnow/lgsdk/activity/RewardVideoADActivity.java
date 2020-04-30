package com.madnow.lgsdk.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.madnow.lgsdk.service.LgSdkService;
import com.madnow.lgsdk.utils.TToast;
import com.ss.union.game.sdk.LGSDK;
import com.ss.union.sdk.ad.LGAdManager;
import com.ss.union.sdk.ad.callback.LGAppDownloadCallback;
import com.ss.union.sdk.ad.dto.LGBaseConfigAdDTO;
import com.ss.union.sdk.ad.dto.LGRewardVideoAdDTO;
import com.ss.union.sdk.ad.type.LGRewardVideoAd;
import com.wogame.common.AppMacros;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;



class RewardAd{
    public LGRewardVideoAd mRewardVideoAd;
    public String mCodeId;
    public boolean mIsReady;
    public boolean mIsFailure;
    public int mStatus;//0;1开始load；2load成功，3load 失败
    public int mShowCount;//展示次数
    public int mFailureCount;//load失败次数
}

/**
 * 激励视频广告示例
 *
 * @author dongbin
 * @since 2019.7.9
 */

public class RewardVideoADActivity {

    public static final String TAG = "RewardVideoAD";
    // 广告ID，仅demo 使用，接入方请申请自己的广告ID
    public static final String SAMPLE_HORIZONTAL_CODE_ID = "901121430";
    // 广告ID，仅demo 使用，接入方请申请自己的广告ID
    public static final String SAMPLE_VERTICAL_CODE_ID = "901121365";
    private Button mLoadAdHorizontal;
    private Button mLoadAdVertical;
    private Button mShowAd;

    private LGAdManager lgADManager;
    private RewardAd mRewardVideoAd;
    private List<String> mCodeList;
    private HashMap<String, RewardAd>mRewardVideoAdList;
    private boolean mHasShowDownloadActive = false;

    private Activity mContext;
//    private String mCodeId;
    private int mReconnectTimes = 0;

    public void  initActivity(Context activity){
        mContext = (Activity)activity;
        mCodeList = new ArrayList<>();
        mRewardVideoAdList = new HashMap<String, RewardAd>();


    }

    public void init(final String codeId,final String codeId2) {
        // step1:LGADManager 广告管理类初始化
        lgADManager = LGSDK.getAdManager();
        // step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
//        LGSDK.requestPermissionIfNecessary(mContext);
//        mCodeId = codeId;
        mCodeList.add(codeId);
        mCodeList.add(codeId2);

        RewardAd rewardAd1 = new RewardAd();
        rewardAd1.mCodeId = codeId;
        rewardAd1.mIsFailure = false;
        rewardAd1.mStatus = 0;
        mRewardVideoAdList.put(codeId,rewardAd1);

        RewardAd rewardAd2 = new RewardAd();
        rewardAd2.mCodeId = codeId2;
        rewardAd2.mIsFailure = false;
        rewardAd2.mStatus = 0;
        mRewardVideoAdList.put(codeId2,rewardAd2);

        loadAd();
    }

    private void loadAd(){
        for (String key : mRewardVideoAdList.keySet()) {
            RewardAd rewardAd = mRewardVideoAdList.get(key);
            if(rewardAd != null &&( rewardAd.mStatus == 0 ||  rewardAd.mStatus == 3)){
                rewardAd.mStatus = 1;
                loadAd(rewardAd.mCodeId,LGRewardVideoAdDTO.ORIENTATION_VERTICAL);
            }
        }
    }

    /**
     * 加载广告
     *
     * @param codeId      广告ID
     * @param orientation 广告展示方向
     */
    private void loadAd(String codeId, int orientation) {
        //step3:创建广告请求参数AdSlot,具体参数含义参考文档
        final LGRewardVideoAdDTO rewardVideoADDTO = new LGRewardVideoAdDTO();
        rewardVideoADDTO.context = mContext;
        // 广告位ID
        rewardVideoADDTO.codeID = codeId;
        // 唯一设备标识 用户ID
        rewardVideoADDTO.userID = "user123";
        // 期望返回的图片尺寸
        rewardVideoADDTO.expectedImageSize = new LGBaseConfigAdDTO.ExpectedImageSize(1080, 1920);
        // 奖励的名称
        rewardVideoADDTO.rewardName = "金币";
        // 奖励的数量
        rewardVideoADDTO.rewardAmount = 3;
        // 设置广告展示方向
        rewardVideoADDTO.videoPlayOrientation = orientation;
        //step4:请求广告
        lgADManager.loadRewardVideoAd(rewardVideoADDTO, new LGAdManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
//                TToast.show(mContext, message);
                Log.e(TAG, "code:" + code + ",message:" + message);

                RewardAd rewardAd = mRewardVideoAdList.get(rewardVideoADDTO.codeID);
                rewardAd.mStatus = 3;
                rewardAd.mFailureCount ++;

                LgSdkService.getInstance().adsError(rewardVideoADDTO.codeID, AppMacros.AT_RewardVideo,code,message);
                mReconnectTimes ++;
                if(mReconnectTimes <= 5){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadAd();
                        }
                    }, 60000);
                }
            }

            @Override
            public void onRewardVideoAdLoad(LGRewardVideoAd ad) {
                Log.e(TAG, "onRewardVideoAdLoad");
//                mRewardVideoAd = ad;

                RewardAd rewardAd = mRewardVideoAdList.get(rewardVideoADDTO.codeID);
                rewardAd.mStatus = 2;
                rewardAd.mRewardVideoAd = ad;
//                boolean isAdd = true;
//                mReconnectTimes = 0;
//                for(int i = 0 ; i < mRewardVideoAdList.size(); i ++){
//                    if(mRewardVideoAdList.get(i) == ad){
//                        isAdd = false;
//                        break;
//                    }
//                }
//                if(isAdd) mRewardVideoAdList.add(ad);
                LgSdkService.getInstance().adsLoaded(rewardVideoADDTO.codeID, AppMacros.AT_RewardVideo);

//                if(mRewardVideoAdList.size() != 2){
//                    loadAd();
//                }
            }
        });
    }

    public boolean isLoaded(){
        for (String key : mRewardVideoAdList.keySet()) {
            RewardAd rewardAd = mRewardVideoAdList.get(key);
            if (rewardAd != null && rewardAd.mStatus == 2) {
                return true;
            }
        }
        loadAd();
        return false;
//        if(mRewardVideoAdList.size() == 0){
//            loadAd();
//            return false;
//        }
//        return true;
    }

    private void cleanData(String codeId){
        RewardAd rewarAd = mRewardVideoAdList.get(codeId);
        if(rewarAd != null){
//            rewarAd.isReady = NO;
//            rewarAd.isValid = NO;
            rewarAd.mStatus = 0;
            if(rewarAd.mRewardVideoAd != null) {
                rewarAd.mRewardVideoAd = null;
            }
        }
    }

    /**
     * 展示广告
     */
    public void showAd() {

        if (mRewardVideoAdList.size() == 0) {
            TToast.show(mContext, "请先加载广告");
            return;
        }
        mRewardVideoAd = null;

        RewardAd rewardAd = mRewardVideoAdList.get(mCodeList.get(0));
        RewardAd rewardAd1 = mRewardVideoAdList.get(mCodeList.get(1));

        if(rewardAd != null && rewardAd.mRewardVideoAd != null && rewardAd1 != null && rewardAd1.mRewardVideoAd != null){
            if( rewardAd.mShowCount <= rewardAd1.mShowCount){
                rewardAd.mShowCount++;
                mRewardVideoAd = rewardAd;
            }
            else {
                rewardAd1.mShowCount++;
                mRewardVideoAd = rewardAd1;
            }
        }
        else if(rewardAd!= null && rewardAd.mRewardVideoAd != null){
            rewardAd.mShowCount++;
            mRewardVideoAd = rewardAd;
        }
        else if(rewardAd1 != null && rewardAd1.mRewardVideoAd != null){
            rewardAd1.mShowCount++;
            mRewardVideoAd = rewardAd1;
        }

//      mttRewardVideoAd.setShowDownLoadBar(false);
        // 设置用户操作交互回调，接入方可选择是否设置
        mRewardVideoAd.mRewardVideoAd.setInteractionCallback(new LGRewardVideoAd.InteractionCallback() {
            @Override
            /**
             * 广告的展示回调 每个广告仅回调一次
             */
            public void onAdShow() {
//                TToast.show(mContext, "rewardVideoAd show");
                Log.e(TAG, "rewardVideoAd show");
                LgSdkService.getInstance().adsShown(mRewardVideoAd.mCodeId, AppMacros.AT_RewardVideo);
            }

            @Override
            /**
             * 广告的下载bar点击回调
             */
            public void onAdVideoBarClick() {
//                TToast.show(mContext, "rewardVideoAd bar click");
                Log.e(TAG, "rewardVideoAd bar click");
                LgSdkService.getInstance().adsClicked(mRewardVideoAd.mCodeId, AppMacros.AT_RewardVideo);
            }

            @Override
            /**
             * 广告关闭的回调
             */
            public void onAdClose() {
//                TToast.show(mContext, "rewardVideoAd close");
                Log.e(TAG, "rewardVideoAd close");
                LgSdkService.getInstance().adsClosed(mRewardVideoAd.mCodeId, AppMacros.AT_RewardVideo,"");

                cleanData(mRewardVideoAd.mCodeId);
                loadAd();
            }

            //视频播放完成回调
            @Override
            /**
             * 视频播放完毕的回调
             */
            public void onVideoComplete() {
//                TToast.show(mContext, "rewardVideoAd complete");
                Log.e(TAG, "rewardVideoAd complete");
            }

            @Override
            public void onVideoError() {
//                TToast.show(mContext, "rewardVideoAd error");
                Log.e(TAG, "rewardVideoAd error");
            }

            /**
             * 激励视频播放完毕，验证是否有效发放奖励的回调
             *
             * @param rewardVerify rewardVerify
             * @param rewardAmount rewardAmount
             * @param rewardName   rewardName
             */
            //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励数量，rewardName：奖励名称
            @Override
            public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
//                TToast.show(mContext, "verify:" + rewardVerify + " amount:" + rewardAmount +  " name:" + rewardName);
                Log.e(TAG, "verify:" + rewardVerify + " amount:" + rewardAmount +  " name:" + rewardName);
                LgSdkService.getInstance().adsVideoComplete(mRewardVideoAd.mCodeId, AppMacros.AT_RewardVideo,"");
            }

            @Override
            public void onSkippedVideo() {
//                TToast.show(mContext, "onSkippedVideo");
                Log.e(TAG, "onSkippedVideo");
                LgSdkService.getInstance().adsSkippedVideo(mRewardVideoAd.mCodeId, AppMacros.AT_RewardVideo,"");
            }
        });

        // 设置下载回调，接入方可选择是否设置
        mRewardVideoAd.mRewardVideoAd.setDownloadCallback(new LGAppDownloadCallback() {
            @Override
            public void onIdle() {
                mHasShowDownloadActive = false;
                Log.e(TAG, "onIdle");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
//                    TToast.show(mContext, "下载中，点击下载区域暂停", Toast.LENGTH_LONG);
                    Log.e(TAG, "onDownloadActive");
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(mContext, "下载暂停，点击下载区域继续", Toast.LENGTH_LONG);
                Log.e(TAG, "onDownloadPaused");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(mContext, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
                Log.e(TAG, "onDownloadFailed");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                TToast.show(mContext, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
                Log.e(TAG, "onDownloadFinished");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
//                TToast.show(mContext, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
                Log.e(TAG, "onInstalled");
            }
        });

        //step5:在获取到广告（onRewardVideoAdLoad）后展示
//        rewardVideoAd.showRewardVideoAd(RewardVideoADActivity.this);

        //step5  展示广告，并传入广告展示的场景
        mRewardVideoAd.mRewardVideoAd.showRewardVideoAd(mContext, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
//        mRewardVideoAdList.remove(mRewardVideoAd);

        RewardAd tRewarAd = mRewardVideoAdList.get(mRewardVideoAd.mCodeId);
        tRewarAd.mRewardVideoAd = null;

//        mRewardVideoAd = null;

    }
}
