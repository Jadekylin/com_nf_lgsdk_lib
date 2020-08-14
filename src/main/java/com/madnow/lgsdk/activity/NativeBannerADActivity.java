package com.ss.union.GameSdkDemo.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ss.union.GameSdkDemo.BaseActivity;
import com.ss.union.GameSdkDemo.R;
import com.ss.union.GameSdkDemo.utils.TToast;
import com.ss.union.game.sdk.LGSDK;
import com.ss.union.sdk.ad.LGAdManager;
import com.ss.union.sdk.ad.bean.LGImage;
import com.ss.union.sdk.ad.callback.LGAppDownloadCallback;
import com.ss.union.sdk.ad.dto.LGBaseConfigAdDTO;
import com.ss.union.sdk.ad.dto.LGNativeBannerAdDTO;
import com.ss.union.sdk.ad.type.LGBaseAd;
import com.ss.union.sdk.ad.type.LGNativeAd;
import com.ss.union.sdk.ad.views.LGAdDislike;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("EmptyMethod")
public class NativeBannerADActivity extends BaseActivity {
    public static final String TAG = "native_banner";
    // 广告ID，仅demo 使用，接入方请申请自己的广告ID
    public static final String SAMPLE_CODE_ID = "901121423";
    private FrameLayout mBannerContainer;
    private Context mContext;
    private Button mLoadBannerAdBtn;
    private Button mButtonLandingPage;
    private Button mCreativeButton;

    private LGAdManager mLGADManager;
    int width;
    int height;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_banner);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
        mContext = this.getApplicationContext();
        mBannerContainer = (FrameLayout) findViewById(R.id.banner_container);
        mLoadBannerAdBtn = (Button) findViewById(R.id.btn_banner_download);
        mLoadBannerAdBtn.setText("展示原生BANNER广告");
        mLoadBannerAdBtn.setOnClickListener(mClickListener);
        mButtonLandingPage = (Button) findViewById(R.id.btn_banner_landingpage);
        mButtonLandingPage.setVisibility(View.GONE);

        // step1:LGADManager 广告管理类初始化
        mLGADManager = LGSDK.getAdManager();
        // step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        LGSDK.requestPermissionIfNecessary(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCreativeButton != null) {
            mCreativeButton = null;
        }
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btn_banner_download) {
                loadBannerAd(SAMPLE_CODE_ID);
            }
        }
    };

    /**
     * 加载广告
     *
     * @param codeId 广告ID
     */
    private void loadBannerAd(String codeId) {
        //step3:创建广告请求参数LGNativeBannerAdDTO
        LGNativeBannerAdDTO bannerADDTO = new LGNativeBannerAdDTO();
        // context
        bannerADDTO.context = this;
        // 广告ID
        bannerADDTO.codeID = codeId;
        bannerADDTO.requestAdCount = 1;
        // 设置期望的展示图片的大小
        bannerADDTO.expectedImageSize = new LGBaseConfigAdDTO.ExpectedImageSize(600, 257);
        //step4:请求广告，对请求回调的广告作渲染处理
        mLGADManager.loadNativeAd(bannerADDTO, new LGAdManager.NativeAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(TAG, "onError: code:" + code + ",message:" + message);
            }

            @Override
            public void onNativeAdLoad(List<LGNativeAd> ads) {
                if (ads.get(0) == null) {
                    return;
                }
                showAd(ads.get(0));
            }
        });
    }

    /**
     * 展示广告
     */
    private void showAd(LGNativeAd nativeAd) {
        View bannerView = LayoutInflater.from(mContext).inflate(R.layout.native_ad, mBannerContainer, false);
        if (bannerView == null) {
            return;
        }
        if (mCreativeButton != null) {
            //防止内存泄漏
            mCreativeButton = null;
        }
        //step5:addview 展示广告
        mBannerContainer.removeAllViews();
        mBannerContainer.addView(bannerView);
        //绑定原生广告的数据
        setAdData(bannerView, nativeAd);

    }

    @SuppressWarnings("RedundantCast")
    private void setAdData(View nativeView, LGNativeAd nativeAd) {
        ((TextView) nativeView.findViewById(R.id.tv_native_ad_title)).setText(nativeAd.getTitle());
        ((TextView) nativeView.findViewById(R.id.tv_native_ad_desc)).setText(nativeAd.getDescription());
        ImageView imgDislike = nativeView.findViewById(R.id.img_native_dislike);
        bindDislikeAction(nativeAd, imgDislike);
        if (nativeAd.getImageList() != null && !nativeAd.getImageList().isEmpty()) {
            LGImage image = nativeAd.getImageList().get(0);
            if (image != null && image.isValid()) {
                ImageView im = nativeView.findViewById(R.id.iv_native_image);
                /*
                  此只为DEMO显示 CP接入过程需要根据自己的尺寸大小进行配置
                  目前DEMO尺寸为 width 填充屏幕  height是屏幕的1/4
                 */
                Glide.with(this).load(image.getImageUrl()).diskCacheStrategy(DiskCacheStrategy.NONE).override(width, height / 4).into(im);
            }
        }
        LGImage icon = nativeAd.getIcon();
        if (icon != null && icon.isValid()) {
            ImageView im = nativeView.findViewById(R.id.iv_native_icon);
            Glide.with(this).load(icon.getImageUrl()).into(im);
        }
        mCreativeButton = (Button) nativeView.findViewById(R.id.btn_native_creative);
        //可根据广告类型，为交互区域设置不同提示信息

        if (nativeAd.getInteractionType() == LGBaseAd.InteractionType.DOWNLOAD) {
            //如果初始化ttAdManager.createAdNative(getApplicationContext())没有传入activity 则需要在此传activity，否则影响使用Dislike逻辑
            nativeAd.setActivityForDownloadApp(this);
            mCreativeButton.setVisibility(View.VISIBLE);
            nativeAd.setDownloadCallback(downloadCallback); // 注册下载监听器
        } else if (nativeAd.getInteractionType() == LGBaseAd.InteractionType.DIAL) {
            mCreativeButton.setVisibility(View.VISIBLE);
            mCreativeButton.setText("立即拨打");
        } else if (nativeAd.getInteractionType() == LGBaseAd.InteractionType.LANDING_PAGE
                || nativeAd.getInteractionType() == LGBaseAd.InteractionType.BROWSER) {
            mCreativeButton.setVisibility(View.VISIBLE);
            mCreativeButton.setText("查看详情");
        } else {
            mCreativeButton.setVisibility(View.GONE);
            TToast.show(mContext, "交互类型异常");
        }

        //可以被点击的view, 也可以把nativeView放进来意味整个广告区域可被点击
        List<View> clickViewList = new ArrayList<>();
        clickViewList.add(nativeView);

        //触发创意广告的view（点击下载或拨打电话）
        List<View> creativeViewList = new ArrayList<>();
        //如果需要点击图文区域也能进行下载或者拨打电话动作，请将图文区域的view传入
        //creativeViewList.add(nativeView);
        creativeViewList.add(mCreativeButton);

        //重要! 这个涉及到广告计费，必须正确调用。convertView必须使用ViewGroup。
        nativeAd.registerViewForInteraction((ViewGroup) nativeView, clickViewList, creativeViewList, imgDislike, new LGNativeAd.AdInteractionCallback() {
            @Override
            public void onAdClicked(View view, LGNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被点击");
                }
            }

            @Override
            public void onAdCreativeClick(View view, LGNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "被创意按钮被点击");
                }
            }

            @Override
            public void onAdShow(LGNativeAd ad) {
                if (ad != null) {
                    TToast.show(mContext, "广告" + ad.getTitle() + "展示");
                }
            }
        });
    }

    //接入网盟的dislike 逻辑，有助于提示广告精准投放度
    private void bindDislikeAction(LGNativeAd ad, View dislikeView) {
        final LGAdDislike ttAdDislike = ad.getDislikeDialog(this);
        if (ttAdDislike != null) {
            ttAdDislike.setDislikeInteractionCallback(new LGAdDislike.InteractionCallback() {
                @Override
                public void onSelected(int position, String value) {
                    mBannerContainer.removeAllViews();
                }

                @Override
                public void onCancel() {

                }
            });
        }
        dislikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ttAdDislike != null)
                    ttAdDislike.showDislikeDialog();
            }
        });
    }

    // 设置下载回调，接入方可选择进行设置
    private final LGAppDownloadCallback downloadCallback = new LGAppDownloadCallback() {
        @Override
        public void onIdle() {
            if (mCreativeButton != null) {
                mCreativeButton.setText("开始下载");
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
            if (mCreativeButton != null) {
                if (totalBytes <= 0L) {
                    mCreativeButton.setText("下载中 percent: 0");
                } else {
                    mCreativeButton.setText("下载中 percent: " + (currBytes * 100 / totalBytes));
                }
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
            if (mCreativeButton != null) {
                mCreativeButton.setText("下载暂停 percent: " + (currBytes * 100 / totalBytes));
            }
        }

        @Override
        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
            if (mCreativeButton != null) {
                mCreativeButton.setText("重新下载");
            }
        }

        @Override
        public void onInstalled(String fileName, String appName) {
            if (mCreativeButton != null) {
                mCreativeButton.setText("点击打开");
            }
        }

        @Override
        public void onDownloadFinished(long totalBytes, String fileName, String appName) {
            if (mCreativeButton != null) {
                mCreativeButton.setText("点击安装");
            }
        }
    };

}
