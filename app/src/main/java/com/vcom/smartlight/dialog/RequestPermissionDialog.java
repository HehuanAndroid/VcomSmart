package com.vcom.smartlight.dialog;

import android.Manifest;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vcom.smartlight.R;
import com.vcom.smartlight.VcomApp;
import com.vcom.smartlight.base.BaseMvvmDialogFragment;
import com.vcom.smartlight.databinding.DialogRequestPermissionBinding;
import com.vcom.smartlight.dvm.RequestPermissionDVM;
import com.vcom.smartlight.singleton.VcomSingleton;

import io.reactivex.disposables.Disposable;

/**
 * @Author Lzz
 * @Date 2020/10/28 13:43
 */
public class RequestPermissionDialog extends BaseMvvmDialogFragment<RequestPermissionDVM, DialogRequestPermissionBinding>
        implements RequestPermissionDVM.RequestCallBack {

    private Disposable disposable;

    private static final String[] permissionsGroup = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_request_permission;
    }

    @Override
    protected void initDatum() {

    }

    @Override
    protected void initViews() {
        getViewDataBinding().setVm(getViewModel());
        getViewModel().setCallBack(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshParams();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }

    public void refreshParams() {
        boolean isBleReady = VcomSingleton.getInstance().isBleReady.get();
        boolean isGpsReady = VcomSingleton.getInstance().isGpsReady.get();
        boolean isPermissionReady = VcomSingleton.getInstance().isLocationReady.get();

        getViewDataBinding().dialogRequestPermissionOpenBle.setBackground(ResourcesCompat.getDrawable(VcomApp.getApp().getResources(),
                isBleReady ? R.drawable.shape_radius_gray : R.drawable.shape_radius_green, null));
        getViewDataBinding().dialogRequestPermissionOpenBle.setClickable(!isBleReady);

        getViewDataBinding().dialogRequestPermissionOpenGps.setBackground(ResourcesCompat.getDrawable(VcomApp.getApp().getResources(),
                isGpsReady ? R.drawable.shape_radius_gray : R.drawable.shape_radius_green, null));
        getViewDataBinding().dialogRequestPermissionOpenGps.setClickable(!isGpsReady);

        getViewDataBinding().dialogRequestPermissionLocation.setBackground(ResourcesCompat.getDrawable(VcomApp.getApp().getResources(),
                isPermissionReady ? R.drawable.shape_radius_gray : R.drawable.shape_radius_green, null));
        getViewDataBinding().dialogRequestPermissionLocation.setClickable(!isPermissionReady);

        if (isBleReady && isGpsReady && isPermissionReady) {
            dismiss();
        }

    }

    @Override
    public void openGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void requestPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        disposable = rxPermissions.requestEach(permissionsGroup).subscribe(permission -> {
            Log.i("curtain_test", "权限名称:" + permission.name + ",申请结果:" + permission.granted);
            refreshParams();
        });
    }
}
