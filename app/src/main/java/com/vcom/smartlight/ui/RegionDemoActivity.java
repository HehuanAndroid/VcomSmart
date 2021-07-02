package com.vcom.smartlight.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityRegionDemoBinding;
import com.vcom.smartlight.databinding.ItemRegionListDemoBinding;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.uivm.RegionDemoVM;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Banlap on 2021/4/6
 */
public class RegionDemoActivity extends BaseMvvmActivity<RegionDemoVM, ActivityRegionDemoBinding>
        implements RegionDemoVM.RegionDemoCallBack {

    private List<Region> mRegionList = new ArrayList<>();
    private String localeLanguage="";

    @Override
    protected int getLayoutId() {
        return R.layout.activity_region_demo;
    }

    @Override
    protected void initDatum() {
        localeLanguage = Locale.getDefault().getLanguage();
        mRegionList.clear();
        List<Region> regionsList1 = new ArrayList<>();
        Region region1 = new Region();
        region1.setSpaceId("100100100");
        region1.setSpaceImg("0");
        region1.setSpaceName(getString(R.string.meeting));
        regionsList1.add(region1);

        Region region2 = new Region();
        region2.setSpaceId("100100101");
        region2.setSpaceImg("1");
        region2.setSpaceName(getString(R.string.bedroom));
        regionsList1.add(region2);

        Region region3 = new Region();
        region3.setSpaceId("100100102");
        region3.setSpaceImg("2");
        region3.setSpaceName(getString(R.string.classroom));
        regionsList1.add(region3);

        mRegionList.addAll(regionsList1);
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        RegionDemoAdapter regionDemoAdapter = new RegionDemoAdapter(this);
        getViewDataBind().rvDemoRegionList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        getViewDataBind().rvDemoRegionList.setAdapter(regionDemoAdapter);
        regionDemoAdapter.setItems(mRegionList);
        regionDemoAdapter.notifyDataSetChanged();
    }



    @Override
    public void viewBack() {
        finish();
    }

    private static class RegionDemoAdapter extends BaseBindingAdapter<Region, ItemRegionListDemoBinding> {

        public RegionDemoAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_region_list_demo;
        }

        @Override
        protected void onBindItem(ItemRegionListDemoBinding itemRegionListDemoBinding, Region item, int i) {
            //banlap: 设置图片圆角大小
            RoundedCorners roundedCorners = new RoundedCorners(20);

            Glide.with(mContext)
                    .load(mContext.getResources().getIdentifier("ic_region_img_" + item.getSpaceImg(), "mipmap", mContext.getPackageName()))
                    .apply(RequestOptions.bitmapTransform(roundedCorners))
                    .into(itemRegionListDemoBinding.ivShowRegionImg);

            itemRegionListDemoBinding.tvRegionName.setText(item.getSpaceName());

            //banlap: 点击图片进入该区域管理界面
            itemRegionListDemoBinding.getRoot().setOnClickListener(v->{
                Intent intent = new Intent(mContext, RegionActivity.class);
                intent.putExtra("RegionDemoId", item.getSpaceId());
                intent.putExtra("RegionDemoImg", item.getSpaceImg());
                intent.putExtra("RegionDemo", true);
                mContext.startActivity(intent);
            });
        }
    }
}
