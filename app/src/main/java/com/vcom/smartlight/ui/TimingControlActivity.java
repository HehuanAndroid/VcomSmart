package com.vcom.smartlight.ui;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityTimingControlBinding;
import com.vcom.smartlight.uivm.TimingControlVM;

/**
 * @author Banlap on 2021/1/19
 */
public class TimingControlActivity extends BaseMvvmActivity<TimingControlVM, ActivityTimingControlBinding>
        implements TimingControlVM.TimingControlVMCallBack  {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_timing_control;
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
    public void viewBack() {
        finish();
    }
}
