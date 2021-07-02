package com.vcom.smartlight.request;

import android.util.Log;

import com.vcom.smartlight.utils.GsonUtil;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * @Author Lzz
 * @Date 2020/10/30 13:15
 */
public abstract class ApiObserver<T extends ResponseBody> implements Observer<T> {

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T responseBody) {
        try {
            String json = responseBody.string();

            Log.e("curtain_test", json);

            boolean flag = GsonUtil.getInstance().getBooleanValue(json, "flag");
            String msg = GsonUtil.getInstance().getValue(json, "msg");
            showMessage(msg);
            if (flag) {
                String data = GsonUtil.getInstance().getValue(json, "data");
                onSuccess(data);
                return;
            } else {
                //banlap: 判断api 天气是否获取成功
                String cityId = GsonUtil.getInstance().getValue(json, "cityid");
                if(cityId != null){
                    onSuccess(json);
                    return;
                }

                //banlap: 判断是否后台获取 天气信息
                int code = Integer.parseInt(GsonUtil.getInstance().getValue(json,"code"));
                if(code==400){
                    onSuccess(GsonUtil.getInstance().getValue(json, "data"));
                    return;
                }
            }
            onFailure();
        } catch (IOException e) {
            showMessage(e.getMessage());
            onError();
        }
    }

    @Override
    public void onError(@NonNull Throwable e) {
        showMessage(e.getMessage());
        onError();
    }

    @Override
    public void onComplete() {

    }

    protected abstract void showMessage(String message);

    protected abstract void onSuccess(String data);

    protected abstract void onFailure();

    protected abstract void onError();
}
