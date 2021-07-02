package com.vcom.smartlight.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAccountSettingsBinding;
import com.vcom.smartlight.databinding.DialogChangePasswordBinding;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.AccountSettingsVM;

import java.util.Objects;

/**
 * @author Banlap on 2021/3/3
 */
public class AccountSettingsActivity extends BaseMvvmActivity<AccountSettingsVM, ActivityAccountSettingsBinding>
        implements AccountSettingsVM.AccountSettingsCallBack {

    private AlertDialog mDialog;
    private AlertDialog changePasswordDialog;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_account_settings;
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
    public void onResume() {
        super.onResume();
        if (VcomSingleton.getInstance().getLoginUser().isEmpty()){
            return;
        }
        getViewDataBind().tvAccountValue.setText(VcomSingleton.getInstance().getLoginUser().getUserId());
    }

    @Override
    public void goSetNewPassword() {
        DialogChangePasswordBinding changePasswordBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_change_password,
                null, false);
        mDialog = new AlertDialog.Builder(this)
                .setView(changePasswordBinding.getRoot())
                .create();

        mDialog.show();
        Objects.requireNonNull(mDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
        changePasswordBinding.btCancel.setOnClickListener(v-> mDialog.dismiss());
        changePasswordBinding.btConform.setOnClickListener(v-> {
            //banlap: 更改账号的密码
            if(TextUtils.isEmpty(changePasswordBinding.etOldPassword.getText()) ||
                TextUtils.isEmpty(changePasswordBinding.etNewPassword.getText()) ||
                 TextUtils.isEmpty(changePasswordBinding.etNewPasswordConform.getText())) {
                    changePasswordDialog = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.dialog_title))
                            .setMessage(getString(R.string.dialog_message_fill_information))
                            .setPositiveButton(getString(R.string.dialog_confirm), (dialog, which) -> dialog.dismiss())
                            .create();
                    changePasswordDialog.show();
            } else {
                //banlap: 判断旧密码是否正确
                getViewModel().changePasswordAuthentication(
                        String.valueOf(changePasswordBinding.etOldPassword.getText()),
                        String.valueOf(changePasswordBinding.etNewPassword.getText()),
                        String.valueOf(changePasswordBinding.etNewPasswordConform.getText()));
            }

        });
    }

    @Override
    public void AuthenticationSuccess(String newPassword, String newPasswordConform) {
        //banlap: 判断输入的两次新密码是否正确
        if(newPassword.equals(newPasswordConform)) {
            getViewModel().changePassword(newPassword);
        } else {
            Toast.makeText(getApplication(),getString(R.string.toast_incorrect_new_password),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void AuthenticationError() {
        Toast.makeText(getApplication(),getString(R.string.toast_old_password_error),Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void updateSuccess() {
        Toast.makeText(getApplication(),getString(R.string.toast_update_success),Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }

    @Override
    public void updateError() {
        Toast.makeText(getApplication(),getString(R.string.toast_update_error),Toast.LENGTH_SHORT).show();
        mDialog.dismiss();
    }


    @Override
    public void viewBack() {
        finish();
    }
}
