package com.vcom.smartlight.uivm;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.gson.reflect.TypeToken;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Timing;
import com.vcom.smartlight.request.ApiLoader;
import com.vcom.smartlight.request.ApiObserver;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import okhttp3.ResponseBody;

/**
 * @author Banlap on 2021/5/28
 */
public class TimingListVM extends AndroidViewModel {

    private TimingListCallBack callBack;

    public TimingListVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(TimingListCallBack callBack) {
        this.callBack = callBack;
    }

    public void showTimingList(String sceneId) {
        ApiLoader.getApi().selectTiming(sceneId, new ApiObserver<ResponseBody>() {
            @Override
            protected void showMessage(String message) {
            }

            @Override
            protected void onSuccess(String data) {
                VcomSingleton.getInstance().setTimingList(GsonUtil.getInstance().json2List(data, new TypeToken<List<Timing>>() {}.getType()));
                EventBus.getDefault().post(new MessageEvent(MessageEvent.timingReady));
            }

            @Override
            protected void onFailure() {
            }

            @Override
            protected void onError() {
            }
        });
    }

    public void addNewTiming() {
        callBack.goAddNewTiming();
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public interface TimingListCallBack {
        void viewBack();
        void goAddNewTiming();

    }
}
