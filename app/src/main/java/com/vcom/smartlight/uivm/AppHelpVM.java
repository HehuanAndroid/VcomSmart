package com.vcom.smartlight.uivm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vcom.smartlight.utils.ActivityUtil;

/**
 * @author Banlap on 2021/5/8
 */
public class AppHelpVM extends AndroidViewModel {

    private AppHelpCallBack callBack;

    public AppHelpVM(@NonNull Application application) {
        super(application);
    }

    public void setCallBack(AppHelpCallBack callBack) {
        this.callBack = callBack;
    }

    public void viewBack() {
        callBack.viewBack();
    }

    public interface AppHelpCallBack {
        void viewBack();
    }
}
