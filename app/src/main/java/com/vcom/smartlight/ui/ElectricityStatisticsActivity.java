package com.vcom.smartlight.ui;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityElectricityStatisticsBinding;
import com.vcom.smartlight.uivm.ElectricityStatisticsVM;

/**
 * @author Banlap on 2021/1/19
 */
public class ElectricityStatisticsActivity extends BaseMvvmActivity<ElectricityStatisticsVM, ActivityElectricityStatisticsBinding>
        implements ElectricityStatisticsVM.ElectricityStatisticsCallBack {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_electricity_statistics;
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
