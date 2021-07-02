package com.vcom.smartlight.ui;

import android.view.View;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAppHelpBinding;
import com.vcom.smartlight.uivm.AppHelpVM;

/**
 * @author Banlap on 2021/5/8
 */
public class AppHelpActivity extends BaseMvvmActivity<AppHelpVM, ActivityAppHelpBinding>
        implements AppHelpVM.AppHelpCallBack {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_app_help;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        boolean isAdvance = getIntent().getBooleanExtra("Advance", false);
        if(isAdvance) {
            getViewDataBind().llShowMoreQuestion.setVisibility(View.VISIBLE);
        } else {
            getViewDataBind().llShowMoreQuestion.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initDatum() {

    }

    @Override
    public void viewBack() {
        finish();
    }
}
