package com.vcom.smartlight.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.reflect.TypeToken;
import com.telink.bluetooth.light.ConnectionStatus;
import com.telink.util.Event;
import com.telink.util.EventListener;
import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityRegionBinding;
import com.vcom.smartlight.databinding.DialogShowRegionDetailBinding;
import com.vcom.smartlight.databinding.ItemDeviceFragmentEquipBinding;
import com.vcom.smartlight.databinding.ItemMainSceneBinding;
import com.vcom.smartlight.databinding.ItemRegionDeviceBinding;
import com.vcom.smartlight.databinding.ItemRegionSceneBinding;
import com.vcom.smartlight.fragment.MainFragment;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Product;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.RegionVM;
import com.vcom.smartlight.utils.GsonUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author Banlap on 2021/3/30
 */
public class RegionActivity extends BaseMvvmActivity<RegionVM, ActivityRegionBinding>
        implements EventListener<String>, RegionVM.RegionCallBack {

    private AlertDialog alertDialog;
    private SceneAdapter sceneAdapter;         //???????????????
    private DeviceAdapter deviceAdapter;         //???????????????

    private List<Region> mRegionList = new ArrayList<>();
    private List<Scene> mSceneList = new ArrayList<>();
    private List<Equip> mEquipList = new ArrayList<>();

    private boolean mIsRegionDemo = false;    //??????????????????
    private String mSpaceId=null;
    private int tagMesh = -1;
    //banlap: ????????????????????????;
    private String localeLanguage = "";

    private List<Scene> mSceneDemoList = new ArrayList<>();      //????????????????????????
    private List<Equip> mEquipDemoList = new ArrayList<>();      //????????????????????????

    private Equip mEquipTemp = new Equip();   //?????????????????????????????????

    @Override
    protected int getLayoutId() {
        return R.layout.activity_region;
    }

    @Override
    protected void initDatum() {
        //banlap: ??????????????????
        localeLanguage = Locale.getDefault().getLanguage();

        //banlap: ??????????????????????????????
        mIsRegionDemo = getIntent().getBooleanExtra("RegionDemo", false);

        //banlap: ???????????? ????????????
        if(mIsRegionDemo) {
            //banlap: ???????????????????????????????????????????????????????????????
            String mRegionDemoId = getIntent().getStringExtra("RegionDemoId");
            if (mRegionDemoId !=null) {
                mSceneDemoList.clear();
                mEquipDemoList.clear();
                List<Scene> newSceneDemoList = new ArrayList<>();
                List<Equip> newEquipDemoList = new ArrayList<>();

                if(mRegionDemoId.equals("100100100")) {
                    Scene scene1 = new Scene();
                    scene1.setSceneName(getString(R.string.go_to_work));
                    scene1.setSceneImg("0");
                    newSceneDemoList.add(scene1);

                    Scene scene2 = new Scene();
                    scene2.setSceneName(getString(R.string.go_off_work));
                    scene2.setSceneImg("1");
                    newSceneDemoList.add(scene2);

                    Scene scene3 = new Scene();
                    scene3.setSceneName(getString(R.string.projection));
                    scene3.setSceneImg("0");
                    newSceneDemoList.add(scene3);

                    Scene scene4 = new Scene();
                    scene4.setSceneName(getString(R.string.left_lamp));
                    scene4.setSceneImg("2");
                    newSceneDemoList.add(scene4);

                    Scene scene5 = new Scene();
                    scene5.setSceneName(getString(R.string.right_lamp));
                    scene5.setSceneImg("2");
                    newSceneDemoList.add(scene5);

                    Scene scene6 = new Scene();
                    scene6.setSceneName(getString(R.string.front_lamp));
                    scene6.setSceneImg("2");
                    newSceneDemoList.add(scene6);

                    Scene scene7 = new Scene();
                    scene7.setSceneName(getString(R.string.partial_lamp));
                    scene7.setSceneImg("2");
                    newSceneDemoList.add(scene7);

                    Equip equip1 = new Equip();
                    equip1.setProductUuid("8961");
                    equip1.setEquipInfoPid("ClassroomLamp");
                    equip1.setEquipName("LED?????????");
                    equip1.setConnectionStatus(ConnectionStatus.ON);
                    newEquipDemoList.add(equip1);

                    Equip equip2 = new Equip();
                    equip2.setProductUuid("000");
                    equip2.setEquipInfoPid("Projector");
                    equip2.setEquipName("?????????");
                    equip2.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip2);

                } else if (mRegionDemoId.equals("100100101")) {

                    Scene scene1 = new Scene();
                    scene1.setSceneName(getString(R.string.get_up));
                    scene1.setSceneImg("0");
                    newSceneDemoList.add(scene1);

                    Scene scene2 = new Scene();
                    scene2.setSceneName(getString(R.string.sleep));
                    scene2.setSceneImg("1");
                    newSceneDemoList.add(scene2);

                    Scene scene3 = new Scene();
                    scene3.setSceneName(getString(R.string.open_panel_light));
                    scene3.setSceneImg("2");
                    newSceneDemoList.add(scene3);

                    Scene scene4 = new Scene();
                    scene4.setSceneName(getString(R.string.open_down_light));
                    scene4.setSceneImg("2");
                    newSceneDemoList.add(scene4);

                    Scene scene5 = new Scene();
                    scene5.setSceneName(getString(R.string.open_ceiling_lamp));
                    scene5.setSceneImg("2");
                    newSceneDemoList.add(scene5);

                    Scene scene6 = new Scene();
                    scene6.setSceneName(getString(R.string.reading));
                    scene6.setSceneImg("1");
                    newSceneDemoList.add(scene6);

                    Scene scene7 = new Scene();
                    scene7.setSceneName(getString(R.string.film_viewing));
                    scene7.setSceneImg("3");
                    newSceneDemoList.add(scene7);

                    Equip equip1 = new Equip();
                    equip1.setProductUuid("21508");
                    equip1.setEquipInfoPid("PanelLight");
                    equip1.setEquipName("?????????");
                    equip1.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip1);

                    Equip equip2 = new Equip();
                    equip2.setProductUuid("258");
                    equip2.setEquipInfoPid("DownLight");
                    equip2.setEquipName("??????");
                    equip2.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip2);

                    Equip equip3 = new Equip();
                    equip3.setProductUuid("001");
                    equip3.setEquipInfoPid("CeilingLamp");
                    equip3.setEquipName("??????");
                    equip3.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip3);

                    Equip equip4 = new Equip();
                    equip4.setProductUuid("20484");
                    equip4.setEquipInfoPid("ElectricCurtains");
                    equip4.setEquipName("??????");
                    equip4.setConnectionStatus(ConnectionStatus.ON);
                    newEquipDemoList.add(equip4);

                    Equip equip5 = new Equip();
                    equip5.setProductUuid("002");
                    equip5.setEquipInfoPid("DeskLamp");
                    equip5.setEquipName("??????");
                    equip5.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip5);

                    Equip equip6 = new Equip();
                    equip6.setProductUuid("003");
                    equip6.setEquipInfoPid("Television");
                    equip6.setEquipName("??????");
                    equip6.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip6);

                } else if (mRegionDemoId.equals("100100102")) {

                    Scene scene1 = new Scene();
                    scene1.setSceneName(getString(R.string.class_begin));
                    scene1.setSceneImg("0");
                    newSceneDemoList.add(scene1);

                    Scene scene2 = new Scene();
                    scene2.setSceneName(getString(R.string.class_over));
                    scene2.setSceneImg("1");
                    newSceneDemoList.add(scene2);

                    Scene scene3 = new Scene();
                    scene3.setSceneName(getString(R.string.projection));
                    scene3.setSceneImg("2");
                    newSceneDemoList.add(scene3);

                    Equip equip1 = new Equip();
                    equip1.setProductUuid("4865");
                    equip1.setEquipInfoPid("BlackBoardLamp");
                    equip1.setEquipName("?????????");
                    equip1.setConnectionStatus(ConnectionStatus.ON);
                    newEquipDemoList.add(equip1);

                    Equip equip2 = new Equip();
                    equip2.setProductUuid("000");
                    equip2.setEquipInfoPid("Projector");
                    equip2.setEquipName("?????????");
                    equip2.setConnectionStatus(ConnectionStatus.OFF);
                    newEquipDemoList.add(equip2);

                }

                mSceneDemoList.addAll(newSceneDemoList);
                mEquipDemoList.addAll(newEquipDemoList);
            }

        } else {
            //banlap: ??????????????? ???????????????????????????
            //banlap: ???????????? ?????????????????????ID??????????????????????????????
            mSpaceId = getIntent().getStringExtra("SpaceId");
            String mSpaceName = getIntent().getStringExtra("SpaceName");
            if (mSpaceName !=null) {
                getViewDataBind().tvRegionNameTitle.setText(mSpaceName);
            }
            String mSpaceImg = getIntent().getStringExtra("SpaceImg");
            if(mSpaceImg !=null) {
                RequestOptions options = new RequestOptions()
                        //.override(300, 800)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                Glide.with(getApplication())
                        .load(getResources().getIdentifier("ic_region_img_new_" + mSpaceImg, "mipmap", getPackageName()))
                        .apply(options)
                        .into(getViewDataBind().ivBackgroundImg);
            }
            //banlap: ???????????? ?????????????????????????????????????????????
            mSceneList = VcomSingleton.getInstance().getUserCurrentRegion().getSceneList();

            //banlap: ???????????? ???????????????????????????????????????????????? (?????????refreshSceneList()????????????????????????????????????)
            int mSceneListSize = mSceneList.size();
            for(int i=0; i<mSceneListSize; i++) {
                if(mSceneList.get(i).getUserEquipList().size()>0) {
                    for(Equip equip : mSceneList.get(i).getUserEquipList()) {
                        if(mEquipList.size()>0) {
                            boolean isAdd = true;
                            for(int j=0; j<mEquipList.size(); j++) {
                                if(mEquipList.get(j).getProductUuid().equals(equip.getProductUuid())) {
                                    isAdd = false;
                                }
                            }
                            if(isAdd) {
                                mEquipList.add(equip);
                            }
                        } else {
                            mEquipList.add(equip);
                        }
                    }
                }
            }
        }

    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        if(!mIsRegionDemo) {
            //banlap: ???????????? ???????????????????????????????????????????????????????????????????????? equipReady
            getViewModel().getUserAllEquip();
        } else {
            String regionDemoImg = getIntent().getStringExtra("RegionDemoImg");
            if(regionDemoImg != null) {
                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_" + regionDemoImg, "mipmap", getPackageName()));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!mIsRegionDemo) {
            if (!VcomSingleton.getInstance().getLoginUser().isEmpty()) {
                EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
                //banlap: ???????????? ????????????????????????
                new Handler().postDelayed(() -> getViewModel().startAutoConnect(), 300);
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            //banlap: ???????????? ??????activity???????????????????????????????????????
            case MessageEvent.refreshEquip:
            case MessageEvent.refreshRegion:
                if(!mIsRegionDemo) {
                    getViewModel().selectUserRegion();
                }
                break;
            //banlap: ???????????? ??????????????????????????????????????????????????????
            case MessageEvent.regionReady:
                if(!mIsRegionDemo) {
                    refreshSceneList();
                }
                break;
            //banlap: ???????????? ?????????????????????????????????????????? equipReady
            case MessageEvent.equipReady:
                if(!mIsRegionDemo) {
                    new Handler().postDelayed(() -> getViewModel().startAutoConnect(), 300);
                }
                break;
            //banlap: ???????????? ???????????? ????????????
            case MessageEvent.onlineStatusNotify:
                if(!mIsRegionDemo) {
                    getViewModel().onOnlineStatusNotify(event.data);
                }
                break;
            case MessageEvent.deviceStatusChanged:
                //getViewModel().onDeviceStatusChanged(event.event);
                break;
        }
    }

    //banlap: ???????????? ???????????????????????????
    public void refreshSceneList() {
        if(!mSpaceId.equals("")) {
            mRegionList = VcomSingleton.getInstance().getUserRegion();
            List<Scene> newSceneList = new ArrayList<>();
            for (int a = 0; a < mRegionList.size(); a++) {
                if (mRegionList.get(a).getSpaceId().equals(mSpaceId)) {
                    newSceneList.addAll(mRegionList.get(a).getSceneList());
                }
            }

            mSceneList.clear();
            mSceneList.addAll(newSceneList);

        }

        //banlap: ???????????????????????????????????????
        mEquipList.clear();
        int mSceneListSize = mSceneList.size();
        for(int i=0; i<mSceneListSize; i++) {
            if(mSceneList.get(i).getUserEquipList().size()>0) {
                for(Equip equip : mSceneList.get(i).getUserEquipList()) {
                    if(mEquipList.size()>0) {
                        boolean isAdd = true;
                        for(int j=0; j<mEquipList.size(); j++) {
                            if(mEquipList.get(j).getProductUuid().equals(equip.getProductUuid())) {
                                isAdd = false;
                            }
                        }
                        if(isAdd) {
                            mEquipList.add(equip);
                        }
                    } else {
                        mEquipList.add(equip);
                    }
                }
            }
        }

        //banlap: ??????????????????????????????????????????
        List<Equip> equipListStatus = new ArrayList<>(VcomSingleton.getInstance().getUserEquips());
        List<Equip> equipListTemp = new ArrayList<>(mEquipList);

        mEquipList.clear();
        int equipListStatusSize = equipListStatus.size();
        int equipListTempSize = equipListTemp.size();
        for(int i=0; i<equipListStatusSize; i++) {
            for(int j=0; j<equipListTempSize; j++) {
                if(equipListStatus.get(i).getProductUuid().equals(equipListTemp.get(j).getProductUuid())) {
                    if(mEquipList.size()>0) {
                        boolean isAdd = true;
                        for(int k=0; k<mEquipList.size(); k++) {
                            if(mEquipList.get(k).getProductUuid().equals(equipListStatus.get(i).getProductUuid())) {
                                isAdd = false;
                            }
                        }
                        if(isAdd) {
                            mEquipList.add(equipListStatus.get(i));
                        }
                    } else {
                        mEquipList.add(equipListStatus.get(i));
                    }
                }
            }

        }

        sceneAdapter.notifyDataSetChanged();
        deviceAdapter.notifyDataSetChanged();

    }

    /*
    * banlap: ???????????? ?????????????????? ???????????????????????????
    * */
    @Override
    public void showDetail() {

        getViewDataBind().clShowDetail.setVisibility(View.GONE);

        //banlap: ???????????????????????????????????????
        DialogShowRegionDetailBinding detailBinding = DataBindingUtil.inflate(LayoutInflater.from(this),
                R.layout.dialog_show_region_detail, null, false);

        alertDialog = new AlertDialog.Builder(this)
                .setView(detailBinding.getRoot())
                .create();

        Window window = alertDialog.getWindow();
        //banlap: ??????AlertDialog????????????
        window.getDecorView().setPadding(0, 0, 0, 0);

        //banlap: ??????????????????
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        //banlap: ??????AlertDialog??????
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.y = -(int)(display.getHeight());

        //banlap: ??????AlertDialog????????????
        window.setBackgroundDrawableResource(R.drawable.shape_half_round_black_alpha);
        //banlap: ??????AlertDialog????????????
        window.setGravity(Gravity.BOTTOM);
        window.setAttributes(params);
        window.setWindowAnimations(R.style.dialogStyleBottom);
        //banlap: ??????AlertDialog???????????????
        window.setDimAmount(0f);

        detailBinding.llDialogSelectSettings.setOnClickListener(v->{
            getViewModel().goSceneListManager();
        });


        //banlap: ???????????? ???????????????
        sceneAdapter = new SceneAdapter(this);
        detailBinding.rvSceneList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL,false));
        detailBinding.rvSceneList.setAdapter(sceneAdapter);
        //
        if(!mIsRegionDemo){
            sceneAdapter.setItems(mSceneList);
        } else {
            sceneAdapter.setItems(mSceneDemoList);
        }
        sceneAdapter.notifyDataSetChanged();

        //banlap: ???????????? ???????????????
        deviceAdapter = new DeviceAdapter(this);
        detailBinding.rvDeviceList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
        detailBinding.rvDeviceList.setAdapter(deviceAdapter);
        //
        if(!mIsRegionDemo) {
            deviceAdapter.setItems(mEquipList);
        } else {
            deviceAdapter.setItems(mEquipDemoList);
        }
        deviceAdapter.notifyDataSetChanged();

        alertDialog.show();

        //banlap: ????????????????????????????????????
        alertDialog.setOnCancelListener(v->{
            getViewDataBind().clShowDetail.setVisibility(View.VISIBLE);
        });
    }

    /*
    * banlap: ?????????????????? ????????????
    * */
    @Override
    public void notifyChanged(List<Equip> equips) {
        //Toast.makeText(this, "???????????????"+ equips.get(0).getProductUuid(), Toast.LENGTH_SHORT).show();
        List<Equip> thisEquips = new ArrayList<>(mEquipList);
        mEquipList.clear();
        //??????for??????
        int equipsSize = equips.size();
        int thisEquipsSize = thisEquips.size();
        for(int i=0; i<equipsSize; i++) {
            for(int j=0; j<thisEquipsSize; j++) {
                if(equips.get(i).getProductUuid().equals(thisEquips.get(j).getProductUuid())) {
                    if(mEquipList.size()>0) {
                        boolean isAdd = true;
                        for(int k=0; k<mEquipList.size(); k++) {
                            if(mEquipList.get(k).getProductUuid().equals(equips.get(i).getProductUuid())) {
                                isAdd = false;
                            }
                        }
                        if(isAdd) {
                            mEquipList.add(equips.get(i));
                        }
                    } else {
                        mEquipList.add(equips.get(i));
                    }
                }
            }

        }


        deviceAdapter.setItems(mEquipList);
        deviceAdapter.notifyDataSetChanged();
    }

    /*
    * banlap: ??????????????????????????????
    * */
    @Override
    public void goSceneListManager() {
        if(!mIsRegionDemo) {
            if (!mSpaceId.equals("")) {
                Intent goSceneMangerIntent = new Intent(this, AddSceneListActivity.class);
                goSceneMangerIntent.putExtra("CurrentRegionId", mSpaceId);
                startActivity(goSceneMangerIntent);
            }
        }
    }

    /*
     * banlap: ??????????????????????????????
     * */
    @Override
    public void goScanDevice() {
        if(!mIsRegionDemo) {
            Intent goScan = new Intent(this, ScanByBleActivity.class);
            startActivity(goScan);
        }
    }

    @Override
    public void viewBack() {
        finish();
    }

    /*
     * banlap: ???????????? ??????????????????????????????
     * */
    public void showRemoveDialog(Equip equip) {
        tagMesh = -1;
        new android.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_2))
                .setMessage(getString(R.string.dialog_message_initialization_action))
                .setPositiveButton(getString(R.string.dialog_confirm), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    removeAllSceneUserEquip(mSpaceId, equip.getUserEquipId());
                    //getViewModel().removeEquip(equip.getUserEquipId());
                    //tagMesh = Integer.parseInt(equip.getMeshAddress());
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialogInterface, i) -> dialogInterface.dismiss()).show();
    }

    public void removeAllSceneUserEquip(String spaceId, String userEquipId) {
        //banlap: ?????????????????????????????????????????????????????????id
        int mSceneListSize = mSceneList.size();
        if(mSceneListSize>0) {
            for(int i=0; i<mSceneListSize; i++) {
                if(mSceneList.get(i).getUserEquipList().size()>0) {
                    List<Equip> equipList = mSceneList.get(i).getUserEquipList();
                    int equipListSize = equipList.size();
                    for(int j=0; j<equipListSize; j++) {
                        if(equipList.get(j).getUserEquipId().equals(userEquipId)) {
                            int meshId = Integer.parseInt(equipList.get(j).getMeshAddress());
                            byte opcode = (byte) 0xEE;
                            byte[] param = {0x00, (byte) Integer.parseInt(mSceneList.get(i).getSceneMeshId())};
                            VcomService.getInstance().sendCommandNoResponse(opcode, meshId, param);
                        }
                    }
                }
            }

        }

        getViewModel().removeUserEquip(spaceId, userEquipId);
    }

    @Override
    public void removeUserEquipSuccess() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshEquip));
        Toast.makeText(this, getString(R.string.toast_delete_success),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeUserEquipFailure() {
        Toast.makeText(this, getString(R.string.toast_delete_error),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeEquipSuccess() {
        if (tagMesh == -1) {
            return;
        }
        byte kickCode = (byte) 0xE3;
        byte[] params = new byte[]{0x01};
        VcomService.getInstance().sendCommandNoResponse(kickCode, tagMesh, params);
    }


    /*
     * banlap: ????????????????????????????????????
     * */
    private void showParams(Equip equip) {
        getViewModel().selectRecord(equip, "0", equip.getUserEquipId());
    }

    @Override
    public void selectRecordSuccess(Equip equip, String data) {
        mEquipTemp = GsonUtil.getInstance().json2List(data, new TypeToken<Equip>() {}.getType());
        goDeviceSetting(equip);
    }

    @Override
    public void selectRecordFailure(Equip equip) {
        goDeviceSetting(equip);
    }

    public void goDeviceSetting(Equip equip) {
        Intent intent = new Intent(this, DeviceSettingActivity.class);
        intent.putExtra("equipId", equip.getUserEquipId());
        intent.putExtra("isSmartView", true);

        if(mEquipTemp!=null) {
            intent.putExtra("mode", mEquipTemp.getMode());
            intent.putExtra("brightness", Integer.parseInt(String.valueOf(mEquipTemp.getBrightness())));
            intent.putExtra("temp", Integer.parseInt(String.valueOf(mEquipTemp.getTemperature())));
            intent.putExtra("red", mEquipTemp.getRed());
            intent.putExtra("green", mEquipTemp.getGreen());
            intent.putExtra("blue", mEquipTemp.getBlue());
        }

        startActivity(intent);

    }

    /*
     * banlap: ?????????????????? ?????? EventBus
     * */
    @Override
    public void performed(Event<String> event) {
        getViewModel().mainVmPerformed(event);
    }

    /*
     * banlap: ???????????? ?????????????????????
     * */
    private class SceneAdapter extends BaseBindingAdapter<Scene, ItemRegionSceneBinding> {

        public SceneAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_region_scene;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onBindItem(ItemRegionSceneBinding binding, Scene item, int i) {
            //banlap: ???????????????????????????7???????????????
            if(item.getSceneName().length()>5) {
                binding.itemSceneNormalName.setText(item.getSceneName().substring(0, 4) + getString(R.string.more));
            } else {
                binding.itemSceneNormalName.setText(item.getSceneName());
            }

            if(!mIsRegionDemo) {
                //banlap: ????????????????????????????????????????????????
                if (item.getUserEquipList().size() > 0) {
                    binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                            .getIdentifier("ic_scene_selected_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
                } else {
                    binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                            .getIdentifier("ic_scene_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
                }
            } else {
                binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_selected_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            }


            binding.getRoot().setOnClickListener(v -> {
                if(!mIsRegionDemo) {
                    //banlap: ????????????????????????????????????
                    int userEquipListSize = mSceneList.get(i).getUserEquipList().size();
                    //banlap: ?????????????????????????????????
                    if (userEquipListSize > 0) {
                        Toast.makeText(mContext, getString(R.string.toast_running) + item.getSceneName() + getString(R.string.toast_scene) + "!", Toast.LENGTH_SHORT).show();
                        int addr = 0xFFFF;
                        byte opcode = (byte) 0xEF;
                        byte[] params = {(byte) Integer.parseInt(item.getSceneMeshId())};
                        VcomService.getInstance().sendCommandNoResponse(opcode, addr, params);

                        //banlap: ???????????????????????????????????????
                        int cx = binding.getRoot().getWidth();
                        int cy = binding.getRoot().getHeight() / 2;
                        float radius = binding.getRoot().getWidth();

                        Animator anim = ViewAnimationUtils.createCircularReveal(binding.clRegionSceneItem, cx, cy, radius, 0);
                        anim.setDuration(500);
                        anim.start();

                    } else {
                        Toast.makeText(getApplication(), getString(R.string.toast_no_device_in_scene), Toast.LENGTH_LONG).show();
                    }
                } else {

                    String regionDemoImg = getIntent().getStringExtra("RegionDemoImg");
                    if (regionDemoImg != null) {

                        if(regionDemoImg.equals("0")) {
                            if(item.getSceneName().equals(getString(R.string.go_to_work))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.go_off_work))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_close_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                }
                            } else if(item.getSceneName().equals(getString(R.string.projection))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_projector_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.left_lamp))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_left_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.right_lamp))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_right_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.front_lamp))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_ahead_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.partial_lamp))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_half_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            }

                        } else if(regionDemoImg.equals("1")) {
                            if(item.getSceneName().equals(getString(R.string.get_up))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("21508")){
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("001")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                            mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.sleep))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_close_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                }
                            } else if(item.getSceneName().equals(getString(R.string.open_down_light))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_half_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("21508")){
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }                                }
                            } else if(item.getSceneName().equals(getString(R.string.open_panel_light))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("21508")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }                                }
                            } else if(item.getSceneName().equals(getString(R.string.open_ceiling_lamp))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_open_light_all_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("21508")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("001")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.reading))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_reading_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.film_viewing))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_film_" + regionDemoImg, "mipmap", getPackageName()));
                                for (int k = 0; k < mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }
                                }
                            }
                        } else if(regionDemoImg.equals("2")) {
                            if(item.getSceneName().equals(getString(R.string.class_begin))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    }
                                }
                            } else if(item.getSceneName().equals(getString(R.string.class_over))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_close_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                }
                            } else if(item.getSceneName().equals(getString(R.string.projection))) {
                                getViewDataBind().ivBackgroundImg.setImageResource(getResources().getIdentifier("ic_region_img_projector_" + regionDemoImg, "mipmap", getPackageName()));
                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                    if (mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                    } else {
                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                    }
                                }
                            }
                        }

                    }

                    deviceAdapter.notifyDataSetChanged();

                    //banlap: ???????????????????????????????????????
                    int cx = binding.getRoot().getWidth();
                    int cy = binding.getRoot().getHeight() / 2;
                    float radius = binding.getRoot().getWidth();

                    Animator anim = ViewAnimationUtils.createCircularReveal(binding.clRegionSceneItem, cx, cy, radius, 0);
                    anim.setDuration(500);
                    anim.start();
                }

            });
            //banlap: ????????????????????????
            //binding.itemSceneNormalSetting.setOnClickListener(v -> goSceneManager(item));
            //binding.flDelete.setOnClickListener(v -> goDeleteScene(item));
        }
    }


    /*
     * banlap: ???????????? ????????????????????????????????????
     * */
    private class DeviceAdapter extends BaseBindingAdapter<Equip, ItemRegionDeviceBinding> {

        public DeviceAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_region_device;
        }

        @Override
        protected void onBindItem(ItemRegionDeviceBinding binding, Equip item, int i) {

            if(mIsRegionDemo) {


                //banlap: ?????????????????????????????????en??????
                if(localeLanguage.equals("en")){
                    binding.itemDeviceName.setText(item.getEquipInfoPid());
                } else {
                    binding.itemDeviceName.setText(item.getEquipName());
                }

                int drawableId=0;
                switch (item.getConnectionStatus()) {
                    case ON:
                        switch(item.getProductUuid()) {
                            case "258": drawableId = R.drawable.ic_258_on; break;
                            case "20484": drawableId = R.drawable.ic_20484_on; break;
                            case "8961": drawableId = R.drawable.ic_8961_on; break;
                            case "4865": drawableId = R.drawable.ic_4865_on; break;
                            case "21508": drawableId = R.drawable.ic_21508_on; break;
                            case "000": drawableId = R.drawable.ic_000_on; break;
                            case "001": drawableId = R.drawable.ic_001_on; break;
                            case "002": drawableId = R.drawable.ic_002_on; break;
                            case "003": drawableId = R.drawable.ic_003_on; break;
                        }
                        binding.itemDeviceIcon.setBackgroundResource(drawableId);

                        //banlap: ?????? ?????????????????????
                        binding.itemDeviceSetting.setVisibility(View.GONE);
                        binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                        binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_on);
                        binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_green);
                        binding.tvItemDeviceStatus.setText(getString(R.string.open));
                        break;
                    case OFF:
                        switch(item.getProductUuid()) {
                            case "258": drawableId = R.drawable.ic_258_on; break;
                            case "20484": drawableId = R.drawable.ic_20484_on; break;
                            case "8961": drawableId = R.drawable.ic_8961_on; break;
                            case "4865": drawableId = R.drawable.ic_4865_on; break;
                            case "21508": drawableId = R.drawable.ic_21508_on; break;
                            case "000": drawableId = R.drawable.ic_000_on; break;
                            case "001": drawableId = R.drawable.ic_001_on; break;
                            case "002": drawableId = R.drawable.ic_002_on; break;
                            case "003": drawableId = R.drawable.ic_003_on; break;
                        }
                        binding.itemDeviceIcon.setBackgroundResource(drawableId);

                        //banlap: ?????? ?????????????????????
                        binding.itemDeviceSetting.setVisibility(View.GONE);
                        binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                        binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_off);
                        binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_orange);
                        binding.tvItemDeviceStatus.setText(getString(R.string.connected));
                        break;
                }
                String regionDemoImg = getIntent().getStringExtra("RegionDemoImg");
                binding.itemDeviceStatus.setOnClickListener(v->{
                    switch (item.getConnectionStatus()) {
                        case ON:
                            mEquipDemoList.get(i).setConnectionStatus(ConnectionStatus.OFF);
                            if (regionDemoImg != null) {
                                switch (regionDemoImg) {
                                    case "0":
                                        int imageId0=0;
                                        switch(item.getProductUuid()) {
                                            case "000":
                                                imageId0 = R.mipmap.ic_region_img_0;
                                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                                    if(mEquipDemoList.get(k).getProductUuid().equals("8961")) {
                                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                                    }
                                                }
                                                break;
                                            case "8961":
                                                imageId0 = R.mipmap.ic_region_img_close_0;
                                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                                    }
                                                }
                                                break;
                                        }
                                        getViewDataBind().ivBackgroundImg.setImageResource(imageId0);
                                        break;
                                    case "1":
                                        int imageId1 = R.mipmap.ic_region_img_close_1;
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                        }
                                        getViewDataBind().ivBackgroundImg.setImageResource(imageId1);
                                        break;
                                    case "2":
                                        int imageId2=0;
                                        switch(item.getProductUuid()) {
                                            case "000":
                                                imageId2 = R.mipmap.ic_region_img_2;
                                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                                    if(mEquipDemoList.get(k).getProductUuid().equals("4865")) {
                                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                                    }
                                                }
                                                break;
                                            case "4865":
                                                imageId2 = R.mipmap.ic_region_img_close_2;
                                                for(int k=0; k<mEquipDemoList.size(); k++) {
                                                    if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                                        mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                                    }
                                                }
                                                break;
                                        }
                                        getViewDataBind().ivBackgroundImg.setImageResource(imageId2);
                                        break;
                                }
                            }
                            break;
                        case OFF:
                            mEquipDemoList.get(i).setConnectionStatus(ConnectionStatus.ON);
                            if (regionDemoImg != null) {
                                if(regionDemoImg.equals("0")) {
                                    if(item.getProductUuid().equals("000")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_projector_0);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("8961")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    } else if(item.getProductUuid().equals("8961")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_0);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    }
                                } else if(regionDemoImg.equals("1")) {
                                    if(item.getProductUuid().equals("258")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_open_light_half_1);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("21508")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("20484")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("001")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    } else if(item.getProductUuid().equals("21508")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_open_light_1);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("20484")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("001")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    } else if(item.getProductUuid().equals("001")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_open_light_all_1);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("21508")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("20484")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            }
                                        }
                                    } else if(item.getProductUuid().equals("20484")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_1);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("258")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("21580")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("001")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            } else if (mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    }   else if(item.getProductUuid().equals("002")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_reading_1);
                                        for (int k = 0; k < mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("002")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            } else {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    } else if(item.getProductUuid().equals("003")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_film_1);
                                        for (int k = 0; k < mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("003")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.ON);
                                            } else {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }                                        }
                                    }
                                } else if(regionDemoImg.equals("2")) {
                                    if(item.getProductUuid().equals("000")) {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_projector_2);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("4865")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    } else {
                                        getViewDataBind().ivBackgroundImg.setImageResource(R.mipmap.ic_region_img_2);
                                        for(int k=0; k<mEquipDemoList.size(); k++) {
                                            if(mEquipDemoList.get(k).getProductUuid().equals("000")) {
                                                mEquipDemoList.get(k).setConnectionStatus(ConnectionStatus.OFF);
                                            }
                                        }
                                    }
                                }
                            }
                            break;
                    }
                    deviceAdapter.notifyDataSetChanged();
                });

            } else {
                Product mProduct = null;
                for (Product product : VcomSingleton.getInstance().getUserProduct()) {
                    for (Product.EquipInfo equipInfo : product.getEquipInfoList()) {
                        if (item.getProductUuid().equals(equipInfo.getEquipInfoPid())) {
                            mProduct = product;
                            break;
                        }
                    }
                }

                if (mProduct == null) {
                    return;
                }

                //banlap: ?????????????????????????????????en??????
                if(localeLanguage.equals("en")){
                    binding.itemDeviceName.setText(mProduct.getEquiNickName());
                } else {
                    binding.itemDeviceName.setText(mProduct.getEquiName());
                }

                int offLineResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_offline", "drawable", mContext.getPackageName());
                int offResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_off", "drawable", mContext.getPackageName());
                int onResId = mContext.getResources().getIdentifier("ic_" + item.getProductUuid() + "_on", "drawable", mContext.getPackageName());

                if (item.getConnectionStatus() == null) {
                    binding.itemDeviceIcon.setBackgroundResource(offResId);
                    binding.itemDeviceSetting.setVisibility(View.GONE);
                    binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                    binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_hourglass_loading);
                    binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_purple);
                    binding.tvItemDeviceStatus.setText(getString(R.string.connecting));

                } else {
                    binding.itemDeviceIcon.setBackgroundResource(onResId);
                    switch (item.getConnectionStatus()) {
                        case ON:
                            //banlap: ?????? ?????????????????????
                            binding.itemDeviceSetting.setVisibility(View.GONE);
                            binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                            binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_on);
                            binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_green);
                            binding.tvItemDeviceStatus.setText(getString(R.string.open));
                            break;
                        case OFF:
                            switch (item.getProductUuid()) {
                                case "24580"://2.8??? ????????????
                                case "25092"://3.5??? ????????????
                                case "25604"://4???????????????
                                case "20484"://????????????
                                case "22532"://????????????
                                case "21508"://??????????????????
                                    //banlap: ?????? ?????????????????????
                                    binding.itemDeviceIcon.setBackgroundResource(onResId);
                                    binding.itemDeviceSetting.setVisibility(View.GONE);
                                    binding.itemDeviceStatus.setVisibility(View.GONE);
                                    binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_orange);
                                    binding.tvItemDeviceStatus.setText(getString(R.string.connected));
                                    break;
                                default:
                                    //banlap: ?????? ?????????????????????
                                    binding.itemDeviceIcon.setBackgroundResource(offResId);
                                    binding.itemDeviceSetting.setVisibility(View.GONE);
                                    binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                                    binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_switch_weight_off);
                                    binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_orange);
                                    binding.tvItemDeviceStatus.setText(getString(R.string.connected));
                                    break;
                            }
                            break;
                        case OFFLINE:
                            binding.itemDeviceIcon.setBackgroundResource(offLineResId);
                            binding.itemDeviceSetting.setVisibility(View.GONE);
                            binding.itemDeviceStatus.setVisibility(View.VISIBLE);
                            binding.itemDeviceStatus.setBackgroundResource(R.drawable.ic_link_off_line);
                            binding.ivItemDeviceStatus.setBackgroundResource(R.drawable.shape_round_gray);
                            binding.tvItemDeviceStatus.setText(getString(R.string.disconnected));
                            break;
                    }
                }

                //banlap: ??????????????? ??????????????????????????????
                binding.getRoot().setOnClickListener(v -> {
                    if (item.getConnectionStatus() == null || item.getConnectionStatus().equals(ConnectionStatus.OFFLINE)) {
                        return;
                    }
                    showParams(item);
                });

                int dstAddr = Integer.parseInt(item.getMeshAddress());

                //banlap: ?????? ???????????????????????????
                binding.rlDeviceStatus.setOnClickListener(v->{
                    if (item.getConnectionStatus() == null) {
                        return;
                    }
                    //banlap: ???????????????????????????????????????
                    String json = null;

                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", VcomSingleton.getInstance().getLoginUser().getUserId());
                    map.put("userEquipId", item.getUserEquipId());

                    map.put("sceneId", 0);
                    map.put("spaceId", 0);
                    map.put("equipType", 0);
                    map.put("mode", item.getMode());
                    map.put("red", 0);
                    map.put("blue", 0);
                    map.put("green", 0);

                    switch (item.getConnectionStatus()) {
                        case ON:
                            byte offCode = (byte) 0xD0;
                            VcomService.getInstance().sendCommandNoResponse(offCode, dstAddr,
                                    new byte[]{0x00, 0x00, 0x00});
                            map.put("brightness", 0);
                            map.put("temperature", 2);
                            break;
                        case OFF:
                            byte onCode = (byte) 0xD0;
                            VcomService.getInstance().sendCommandNoResponse(onCode, dstAddr,
                                    new byte[]{0x01, 0x00, 0x00});
                            map.put("brightness", 50);
                            map.put("temperature", 2);
                            break;
                    }
                    json = GsonUtil.getInstance().toJson(map);
                    getViewModel().saveParams(json);
                });

                //banlap: ??????????????????
                binding.flDelete.setOnClickListener(v->{
                    //Toast.makeText(getActivity(), "????????????", Toast.LENGTH_LONG).show();
                    showRemoveDialog(item);

                });
            }
        }
    }


}
