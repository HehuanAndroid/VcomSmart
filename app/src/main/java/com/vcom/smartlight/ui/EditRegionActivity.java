package com.vcom.smartlight.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityEditRegionBinding;
import com.vcom.smartlight.databinding.DialogAddRegionBinding;
import com.vcom.smartlight.databinding.DialogEditRegionBinding;
import com.vcom.smartlight.databinding.ItemEditRegionBinding;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.EditRegionVM;

import java.util.ArrayList;
import java.util.List;

public class EditRegionActivity extends BaseMvvmActivity<EditRegionVM, ActivityEditRegionBinding> implements EditRegionVM.EditRegionVmCallBack {

    private final List<Region> regionList = new ArrayList<>();
    private RegionListAdapter adapter;

    private static AlertDialog dialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_edit_region;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        adapter = new RegionListAdapter(this);
        getViewDataBind().editRegionRecycler.setAdapter(adapter);
        adapter.setItems(regionList);
        regionList.addAll(VcomSingleton.getInstance().getUserRegion());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initDatum() {

    }

    private class RegionListAdapter extends BaseBindingAdapter<Region, ItemEditRegionBinding> {

        public RegionListAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_edit_region;
        }

        @Override
        protected void onBindItem(ItemEditRegionBinding itemEditRegionBinding, Region item, int i) {
            itemEditRegionBinding.itemEditRegionName.setText(item.getSpaceName());
            itemEditRegionBinding.getRoot().setOnClickListener(v -> editRegion(item));
        }
    }

    @Override
    public void back() {
        finish();
    }

    public void editRegion(Region region) {
        DialogEditRegionBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_edit_region, null, false);
        viewDataBinding.dialogEditRegionName.setText(region.getSpaceName());
        viewDataBinding.dialogEditRegionName.setSelection(region.getSpaceName().length());
        dialog = new AlertDialog.Builder(this)
                .setView(viewDataBinding.getRoot())
                .create();
        dialog.show();
        viewDataBinding.dialogEditRegionCancel.setOnClickListener(v -> dialog.dismiss());
        viewDataBinding.dialogEditRegionDelete.setOnClickListener(v -> getViewModel().deleteRegion(region));
        viewDataBinding.dialogEditRegionUpdate.setOnClickListener(v -> {
            String regionName = viewDataBinding.dialogEditRegionName.getText().toString();
            if (TextUtils.isEmpty(regionName)) {
                return;
            }
            region.setSpaceName(regionName);
            getViewModel().updateRegion(region);
        });
    }

    @Override
    public void addRegion() {
        DialogAddRegionBinding viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_add_region, null, false);
        dialog = new AlertDialog.Builder(this)
                .setView(viewDataBinding.getRoot())
                .create();
        dialog.show();
        viewDataBinding.dialogAddRegionCancel.setOnClickListener(v -> dialog.dismiss());
        viewDataBinding.dialogAddRegionImageCancel.setOnClickListener(v -> dialog.dismiss());
        viewDataBinding.dialogAddRegionCommit.setOnClickListener(v -> {
            String regionName = viewDataBinding.dialogAddRegionName.getText().toString();
            if (TextUtils.isEmpty(regionName)) {
                return;
            }
            getViewModel().requestAddRegion(regionName);
        });
    }

    @Override
    public void requestSuccess() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        getViewModel().getRegion();
    }

    @Override
    public void requestFailure() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void newRegionData(List<Region> regions) {
        regionList.clear();
        regionList.addAll(regions);
        adapter.notifyDataSetChanged();
    }
}