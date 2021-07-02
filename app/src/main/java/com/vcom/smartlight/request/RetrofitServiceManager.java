package com.vcom.smartlight.request;

import android.annotation.SuppressLint;
import android.content.Context;

import com.vcom.smartlight.VcomApp;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitServiceManager {

    private static final int DEFAULT_TIME_OUT = 5;
    private static final int DEFAULT_READ_TIME_OUT = 10;

    private final Retrofit mRetrofit;

    public Context mContext;

    private RetrofitServiceManager() {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                .addHeaderParams("Content-Type","application/json")
                .build();
        builder.addInterceptor(commonInterceptor);
        builder.sslSocketFactory(createSSLSocketFactory(),new TrustAllManager());
        builder.hostnameVerifier((hostname, session) -> true);


        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(VcomApp.BASE_URL)
                .build();

    }

    /*
    * banlap: http请求并添加cookies
    * */
    private RetrofitServiceManager(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cookieJar(new CookieManager(context));//cookie保持
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                .addHeaderParams("Content-Type","application/json")
                .build();
        builder.addInterceptor(commonInterceptor);
        builder.sslSocketFactory(createSSLSocketFactory(),new TrustAllManager());
        builder.hostnameVerifier((hostname, session) -> true);


        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(VcomApp.BASE_URL)
                .build();

    }

    private RetrofitServiceManager(String newApi) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //builder.cookieJar(new CookieManager());//cookie保持
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);
        HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                .addHeaderParams("Content-Type","application/json")
                .build();
        builder.addInterceptor(commonInterceptor);
        builder.sslSocketFactory(createSSLSocketFactory(),new TrustAllManager());
        builder.hostnameVerifier((hostname, session) -> true);

        String baseURL = "";
        //banlap: api 获取天气
        if(newApi.equals("weather")){
            baseURL = "https://tianqiapi.com/";
        }
        mRetrofit = new Retrofit.Builder()
                .client(builder.build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseURL)
                .build();
    }


    private static class SingletonHolder {
        private static final RetrofitServiceManager INSTANCE = new RetrofitServiceManager();
        private static final RetrofitServiceManager INSTANCE_API_WEATHER = new RetrofitServiceManager("weather");
    }

    private static class SingletonHolder2 extends RetrofitServiceManager {
        private SingletonHolder2(Context context) {
            RetrofitServiceManager INSTANCE_CONTEXT = new RetrofitServiceManager(context);
        }

    }

    /**
     * 获取RetrofitServiceManager
     */
    public static RetrofitServiceManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static RetrofitServiceManager getInstanceWeather() {
        return SingletonHolder.INSTANCE_API_WEATHER;
    }

    public static RetrofitServiceManager getInstanceContext(Context context) {
        return new SingletonHolder2(context);
    }

    /**
     * 获取对应的Service
     */
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    @SuppressLint("TrulyRandom")
    public static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllManager()},
                    new SecureRandom());
            sSLSocketFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }
        return sSLSocketFactory;
    }

    public static class TrustAllManager implements X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    public static class TrustAllHostnameVerifier implements HostnameVerifier {
        @SuppressLint("BadHostnameVerifier")
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

}