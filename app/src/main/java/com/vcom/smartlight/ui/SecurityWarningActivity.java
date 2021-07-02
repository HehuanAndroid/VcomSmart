package com.vcom.smartlight.ui;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivitySecurityWarningBinding;
import com.vcom.smartlight.uivm.SecurityWarningVM;

/**
 * @author Banlap on 2021/1/19
 */
public class SecurityWarningActivity extends BaseMvvmActivity<SecurityWarningVM, ActivitySecurityWarningBinding>
        implements SecurityWarningVM.SecurityWarningVMCallBack {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_security_warning;
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
