package com.vcom.smartlight.ui;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseActivity;
import com.vcom.smartlight.databinding.ActivityCustomErrorBinding;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CustomErrorActivity extends BaseActivity<ActivityCustomErrorBinding> {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_custom_error;
    }

    @Override
    protected void afterInit() {

    }

    @Override
    protected void initViews() {

        getViewDataBind().errorDetails.setText(CustomActivityOnCrash.getStackTraceFromIntent(getIntent()));

        CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            getViewDataBind().restartButton.setOnClickListener(v -> CustomActivityOnCrash.restartApplication(CustomErrorActivity.this, config));
        } else {
            getViewDataBind().restartButton.setOnClickListener(v -> CustomActivityOnCrash.closeApplication(CustomErrorActivity.this, config));
        }
    }

    @Override
    protected void initDatum() {

    }
}