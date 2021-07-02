package com.vcom.smartlight;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import androidx.room.Room;

import com.telink.TelinkApplication;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.vcom.smartlight.database.MainDatabase;
import com.vcom.smartlight.receiver.VcomReceiver;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.ui.CustomErrorActivity;
import com.vcom.smartlight.ui.OpenActivity;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class VcomApp extends TelinkApplication {

    @SuppressLint("StaticFieldLeak")
    private static VcomApp app;

    private MainDatabase database;
    private VcomReceiver mBroadcastReceiver;

    //banlap: 后台api接口 内外网切换
    //public static final String BASE_URL = "https://192.168.3.235:8080/vcom/";
    //public static final String BASE_URL = "https://api.vcom.eu.org:52010/vcom/";
    //public static final String BASE_URL = "https://47.117.132.58:8080/vcom/";
    public static final String BASE_URL = "https://api1.vcom.eu.org:52010/vcom/";

    public static final String DEFAULT_NAME = "ZDD_Mesh";
    public static final String DEFAULT_PASSWORD = "654321";
    public static final String NEW_NAME = "Vcom_Mesh";

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        Log.e("Update api url:", BASE_URL);
        mBroadcastReceiver = new VcomReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        filter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, filter);

        database = Room.databaseBuilder(this, MainDatabase.class, "VcomDB")
                .addMigrations()
                .allowMainThreadQueries()
                .build();

        CaocConfig.Builder.create()
                .showErrorDetails(false)
                .restartActivity(OpenActivity.class)
                .errorActivity(CustomErrorActivity.class)
                .apply();

        UMConfigure.init(this, "5fb72ced1e29ca3d7bdec208", "default", UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);

    }

    public MainDatabase getDatabase() {
        return database;
    }

    public static VcomApp getApp() {
        return app;
    }

    @Override
    public void doInit() {
        super.doInit();
        startLightService(VcomService.class);
    }

    @Override
    public void doDestroy() {
        super.doDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

}
