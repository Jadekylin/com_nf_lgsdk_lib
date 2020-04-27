package com.madnow.lgsdk.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.madnow.lgsdk.service.LgSdkService;
import com.madnow.lgsdk.utils.TToast;
import com.ss.union.game.sdk.LGSDK;
import com.ss.union.sdk.ad.LGAdManager;
import com.ss.union.sdk.ad.callback.LGAppDownloadCallback;
import com.ss.union.sdk.ad.dto.LGBaseConfigAdDTO;
import com.ss.union.sdk.ad.dto.LGFullScreenVideoAdDTO;
import com.ss.union.sdk.ad.type.LGFullScreenVideoAd;
import com.wogame.common.AppMacros;

/**
 * 全屏视频广告接入示例
 *
 * @author dongbin
 * @since 2019.7.9
 */

public class FullScreenVideoADActivity {

    public static final String TAG = "full_screen_video_ad";
    // 广告ID，仅demo 使用，接入方请申请自己的广告ID
    public static final String SAMPLE_CODE_ID_VERTICAL = "901121375";

    public static final String SAMPLE_CODE_ID_HORIZONTAL = "901121184";

    private Button mLoadAdHorizontal;
    private Button mLoadAdVertical;
    private Button mShowAd;
    private LGAdManager lgAdManager;
    private LGFullScreenVideoAd fullScreenVideoAd;

    private boolean mHasShowDownloadActive = false;

    private Activity mContext;
    private String mCodeId;

    public void init(Context activity, final String codeId){
        mContext = (Activity)activity;
        mCodeId = codeId;
        // step1:LGADManager 广告管理类初始化
        lgAdManager = LGSDK.getAdManager();
        // step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        LGSDK.requestPermissionIfNecessary(mContext);

        loadAd();
    }


    public void loadAd(){
        loadAd(mCodeId, AppMacros.AT_RewardVideo);
    }
    /**
     * 加载广告
     *
     * @param codeId      广告ID
     * @param orientation 展示的广告反向
     */
    private void loadAd(String codeId, int orientation) {
        //step3:创建广告请求参数LGFullScreenVideoAdDTO
        LGFullScreenVideoAdDTO fullScreenVideoDTO = new LGFullScreenVideoAdDTO();
        // Context
        fullScreenVideoDTO.context = mContext;
        // 广告ID，需要和运营同学确认，已申请该参数
        fullScreenVideoDTO.codeID = codeId;
        // 期望的返回的图片尺寸
        fullScreenVideoDTO.expectedImageSize =
                new LGBaseConfigAdDTO.ExpectedImageSize(1080, 1920);
        // 视频方向
        fullScreenVideoDTO.videoPlayOrientation = orientation;

        //step4:请求广告
        lgAdManager.loadFullScreenVideoAd(fullScreenVideoDTO, new LGAdManager.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "onError: code:" + code + ",message:" + message);
//                TToast.show(mContext, message);
                LgSdkService.getInstance().adsError(mCodeId, AppMacros.AT_FullScreenVideo,code,message);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadAd();
                    }
                }, 15000);
            }

            @Override
            public void onFullScreenVideoAdLoad(LGFullScreenVideoAd LGFullScreenVideoAd) {
                Log.e(TAG, "onFullScreenVideoAdLoad");
//                TToast.show(mContext,"FullVideoAd loaded");
                fullScreenVideoAd = LGFullScreenVideoAd;
                LgSdkService.getInstance().adsLoaded(mCodeId, AppMacros.AT_FullScreenVideo);
            }
        });
    }

    /**
     * 展示广告
     */
    public void showAd() {
        if (fullScreenVideoAd == null) {
            TToast.show(mContext, "请先加载广告");
            return;
        }

        // 设置用户操作交互回调，接入方可选择是否设置
        fullScreenVideoAd.setInteractionCallback(new LGFullScreenVideoAd.InteractionCallback() {
            @Override
            public void onAdShow() {
                Log.e(TAG, "FullVideoAd show");
                TToast.show(mContext, "FullVideoAd show");
                LgSdkService.getInstance().adsShown(mCodeId, AppMacros.AT_FullScreenVideo);
            }

            @Override
            public void onAdVideoBarClick() {
                Log.e(TAG, "FullVideoAd bar click");
                TToast.show(mContext, "FullVideoAd bar click");
                LgSdkService.getInstance().adsClicked(mCodeId, AppMacros.AT_FullScreenVideo);
            }

            @Override
            public void onAdClose() {
                Log.e(TAG, "FullVideoAd close");
                TToast.show(mContext, "FullVideoAd close");
                LgSdkService.getInstance().adsClosed(mCodeId, AppMacros.AT_FullScreenVideo,"");

                loadAd();
            }

            @Override
            public void onVideoComplete() {
                Log.e(TAG, "FullVideoAd complete");
                TToast.show(mContext, "FullVideoAd complete");
            }

            @Override
            public void onSkippedVideo() {
                Log.e(TAG, "FullVideoAd skipped");
                TToast.show(mContext, "FullVideoAd skipped");
            }
        });

        // 设置下载回调，接入方可选择是否设置
        fullScreenVideoAd.setDownloadCallback(new LGAppDownloadCallback() {
            @Override
            public void onIdle() {
                Log.e(TAG, "onIdle");
                mHasShowDownloadActive = false;
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    Log.e(TAG, "onDownloadActive");
                    TToast.show(mContext, "下载中，点击下载区域暂停", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(TAG, "onDownloadPaused");
                TToast.show(mContext, "下载暂停，点击下载区域继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(TAG, "onDownloadFailed");
                TToast.show(mContext, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.e(TAG, "onDownloadFinished" + ",filename:" + fileName + ",appName:" + appName);
                TToast.show(mContext, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                Log.e(TAG, "onInstalled" + ",filename:" + fileName + ",appName:" + appName);
                TToast.show(mContext, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
            }
        });

        //step5:在获取到广告（onFullScreenVideoAdLoad）后展示
//        fullScreenVideoAd.showFullScreenVideoAd(this);
        //step5:在获取到广告（onFullScreenVideoAdLoad）后展示 并传入广告场景
        fullScreenVideoAd.showFullScreenVideoAd(mContext, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);

        fullScreenVideoAd = null;
    }
}
