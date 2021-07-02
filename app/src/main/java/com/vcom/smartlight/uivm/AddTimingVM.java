package com.vcom.smartlight.uivm;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Timing;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/5/28
 */
public class AddTimingVM extends AndroidViewModel {

    private AddTimingCallBack callBack;

    public AddTimingVM(@NonNull Application application) {
        super(application);
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public void saveTiming() {
        callBack.viewSaveTiming();
    }

    public void saveTimingApi(boolean isEdit, Map<String, Object> params) {

        ApiLoader.getApi().addTiming(GsonUtil.getInstance().toJson(params), new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshTiming));
                callBack.timingSuccess(isEdit);
            }

            @Override
            protected void onFailure() {
                callBack.timingFailure(isEdit);
            }

            @Override
            protected void onError() {
                callBack.timingFailure(isEdit);
            }
        });
    }

    public void deleteTiming() {
        callBack.viewDeleteTiming();
    }

    public void deleteTimingApi(String timingId) {

        ApiLoader.getApi().deleteTiming(timingId, new ApiObserver<ResponseBody>() {

            @Override
            protected void showMessage(String message) {

            }

            @Override
            protected void onSuccess(String data) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshTiming));

                callBack.deleteTimingSuccess();
            }

            @Override
            protected void onFailure() {
                callBack.deleteTimingFailure();
            }

            @Override
            protected void onError() {
                callBack.deleteTimingFailure();

            }
        });
    }


    public void setCallBack(AddTimingCallBack callBack) {
        this.callBack = callBack;
    }

    public interface AddTimingCallBack {
        void viewBack();
        void viewSaveTiming();
        void viewDeleteTiming();
        void timingSuccess(boolean isEdit);
        void timingFailure(boolean isEdit);
        void deleteTimingSuccess();
        void deleteTimingFailure();

    }
}
