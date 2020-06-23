-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}
#抖音分享和授权登录
-keep class com.bytedance.sdk.open.aweme.** {*;}
#安全验证码以及OKHttp混淆配置
-keep class com.ss.union.okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keepnames class com.ss.union.okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keepattributes *Annotation*
-keepattributes SetJavaScriptEnabled
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}