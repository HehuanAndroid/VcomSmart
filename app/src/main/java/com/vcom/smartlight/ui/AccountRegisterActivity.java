package com.vcom.smartlight.ui;

import android.content.Context;
import android.widget.Toast;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAccountRegisterBinding;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.uivm.AccountRegisterVM;
import com.vcom.smartlight.utils.TimeUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Random;

/**
 * @author Banlap on 2021/6/17
 */
public class AccountRegisterActivity extends BaseMvvmActivity<AccountRegisterVM, ActivityAccountRegisterBinding>
        implements AccountRegisterVM.AccountRegisterCallBack {

    private TimeUtil timeUtil;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_register;
    }


    @Override
    protected void initDatum() {

    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        EventBus.getDefault().register(this);

        timeUtil = new TimeUtil(getViewDataBind().tvSmsCode, "#FFFFFF");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch(event.msgCode) {
            case MessageEvent.sendMessageSuccess:

                break;
            case MessageEvent.sendMessageError:
                Toast.makeText(this, "发送失败，请联系管理人员。", Toast.LENGTH_SHORT).show();
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        timeUtil.cancelTimer();
    }

    @Override
    public void viewBack() {
        finish();
    }

    @Override
    public void sendSMSCode() {
        if(getViewDataBind().etMobileNumber.getText()==null || getViewDataBind().etMobileNumber.getText().toString().equals("")) {
            Toast.makeText(this, getString(R.string.toast_input_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if(getViewDataBind().etMobileNumber.getText().length() >=10) {
            //getViewModel().sendSMSCodeRequest(String.valueOf(getViewDataBind().etMobileNumber.getText()));
            Random random = new Random();
            int code = random.nextInt(9999);
            String codeStr = "" + code;
            codeStr = (code>=0 && code<=99)? "00" + code : codeStr;
            codeStr = (code>=100 && code<=999)? "0" + code : codeStr;

            Toast.makeText(this, "正确, 验证码为：" + codeStr, Toast.LENGTH_SHORT).show();
            timeUtil.RunTimer();
        } else {
            Toast.makeText(this, getString(R.string.toast_incorrect_phone_number), Toast.LENGTH_SHORT).show();
        }

    }


}
