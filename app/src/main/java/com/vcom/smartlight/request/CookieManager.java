package com.vcom.smartlight.request;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @author Banlap on 2021/3/25
 */
public class CookieManager implements CookieJar {


    private static Context mContext;
    private static PersistentCookieStore cookieStore;

    public CookieManager(Context context) {
        mContext = context;
        if (cookieStore == null ) {
            cookieStore = new PersistentCookieStore(mContext);
        }
    }

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        List<Cookie> cookies =cookieStore.get(httpUrl);
        return cookies;
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
        if (list != null && list.size() > 0) {
            for (Cookie item : list) {
                cookieStore.add(httpUrl, item);
            }
        }
    }
}
