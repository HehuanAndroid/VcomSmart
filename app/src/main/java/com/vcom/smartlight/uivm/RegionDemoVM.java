package com.vcom.smartlight.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * @author Banlap on 2021/4/6
 */
public class RegionDemoVM extends AndroidViewModel {

    private RegionDemoCallBack callBack;

    public void setCallBack(RegionDemoCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public RegionDemoVM(@NonNull Application application) {
        super(application);
    }

    public interface RegionDemoCallBack {
        void viewBack();
    }
}
