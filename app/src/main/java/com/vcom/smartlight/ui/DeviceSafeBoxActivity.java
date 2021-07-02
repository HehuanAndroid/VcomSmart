package com.vcom.smartlight.ui;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAddSafeBoxBinding;
import com.vcom.smartlight.uivm.DeviceSafeBoxVM;

public class DeviceSafeBoxActivity extends BaseMvvmActivity<DeviceSafeBoxVM, ActivityAddSafeBoxBinding>
        implements DeviceSafeBoxVM.DeviceSafeBoxVMCallBack {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_safe_box;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
    }

    @Override
    protected void initDatum() {

    }

    @Override
    public void saveData() {
       finish();
    }

    @Override
    public void viewBack() {
        finish();
    }
}
