/*
 * Copyright (C) 2015 The Telink Bluetooth Light Project
 *
 */
package com.telink.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.telink.util.ContextUtil;

import java.util.UUID;

/**
 * 蓝牙扫描接口
 */
public final class LeBluetooth {

    public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;
    // 未开启定位

    /**
     * LOCATION_CHECK_PERIOD内 扫描结果为空，且定位未打开
     */
    public static final int SCAN_FAILED_LOCATION_DISABLE = 8;

    private Handler mDelayHandler = new Handler();
    private LocationCheckTask mLocationCheckTask = new LocationCheckTask();
    private static final long LOCATION_CHECK_PERIOD = 10 * 1000;

    private static LeBluetooth mThis;
    private volatile boolean mStarted = false;
    private volatile boolean mScanning = false;

    /********************************************************************************
     * Bluetooth API
     *******************************************************************************/

    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothLeScanner mScanner;
    private ScanCallback mScanCallback;
    private LeScanCallback mCallback;
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private boolean mSupportLoScan = false;

    /********************************************************************************
     * Construct
     *******************************************************************************/

    private LeBluetooth() {
    }

    /********************************************************************************
     * Singleton
     *******************************************************************************/

    public static LeBluetooth getInstance() {

        synchronized (LeBluetooth.class) {
            if (LeBluetooth.mThis == null) {
                LeBluetooth.mThis = new LeBluetooth();
            }
        }

        return LeBluetooth.mThis;
    }

    /********************************************************************************
     * Public API
     *******************************************************************************/

    /**
     * 是否正在扫描
     *
     * @return
     */
    public boolean isScanning() {
        synchronized (this) {
            return this.mScanning;
        }
    }

    public void setSupportLoScan(boolean support) {
        this.mSupportLoScan = support;
    }

    /**
     * 设置回调函数
     *
     * @param callback
     */
    public void setLeScanCallback(LeScanCallback callback) {
        this.mCallback = callback;

        if (mCallback == null) return;

        if (this.isSupportLollipop()) {
            this.mScanCallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (isSupportLollipop()) {
                        byte[] scanRecord = null;

                        if (result.getScanRecord() != null)
                            scanRecord = result.getScanRecord().getBytes();
                        if (mCallback != null)
                            mCallback.onLeScan(result.getDevice(), result.getRssi(), scanRecord);
                    }

                    if (isSupportM() && ContextUtil.isLocationEnable(mContext)) {
                        mDelayHandler.removeCallbacks(mLocationCheckTask);
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    if (errorCode != ScanCallback.SCAN_FAILED_ALREADY_STARTED)
                        if (mCallback != null)
                            mCallback.onScanFail(LeBluetooth.SCAN_FAILED_FEATURE_UNSUPPORTED);
                }
            };
        } else {
            this.mLeScanCallback = (device, rssi, scanRecord) -> {
                if (mCallback != null)
                    mCallback.onLeScan(device, rssi, scanRecord);

                if (isSupportM() && ContextUtil.isLocationEnable(mContext)) {
                    mDelayHandler.removeCallbacks(mLocationCheckTask);
                }
            };
        }
    }

    /**
     * 开始扫描
     *
     * @param serviceUUIDs
     * @return
     */
    synchronized public boolean startScan(final UUID[] serviceUUIDs) {

        synchronized (this) {
            if (this.mScanning || this.mStarted)
                return true;
        }

        TelinkLog.w("LeBluetooth#StartScan -> " + this.isEnabled());
        if (!this.isEnabled()) {
            return false;
        }
        synchronized (this) {
            this.mStarted = true;
            this.scan(serviceUUIDs);
        }

        return true;
    }


    private void scan(final UUID[] serviceUUIDs) {
//        if (isSupportM() && ContextUtil.isLocationEnable(mContext)) {
//            mDelayHandler.removeCallbacks(mLocationCheckTask);
//            mDelayHandler.postDelayed(mLocationCheckTask, LOCATION_CHECK_PERIOD);
//            return;
//        }

        if (isSupportLollipop()) {
            mScanner = mAdapter.getBluetoothLeScanner();
            if (mScanner == null) {
                synchronized (this) {
                    mScanning = false;
                }
                if (mCallback != null)
                    mCallback.onScanFail(SCAN_FAILED_FEATURE_UNSUPPORTED);
            } else {
                if (mScanCallback != null) {
                    mScanner.startScan(mScanCallback);
                    synchronized (this) {
                        mScanning = true;
                    }
                    mCallback.onStartedScan();
                } else {
                    Log.e("curtain_test", "mScanCallback is null");
                }
            }

        } else {
            if (!mAdapter.startLeScan(serviceUUIDs,
                    mLeScanCallback)) {
                synchronized (this) {
                    mScanning = false;
                }
                if (mCallback != null)
                    mCallback.onScanFail(SCAN_FAILED_FEATURE_UNSUPPORTED);
            } else {
                synchronized (this) {
                    mScanning = true;
                }
                mCallback.onStartedScan();
            }
        }
    }

    public boolean isSupportLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mSupportLoScan;
//        return false;
    }

    public boolean isSupportM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
//        return false;
    }

    /**
     * 停止扫描
     */
    synchronized public void stopScan() {
        TelinkLog.w("LeBluetooth#stopScan");
        synchronized (this) {
            if (!mScanning)
                return;
        }

        try {
            if (isSupportLollipop()) {
                if (mScanner != null)
                    mScanner.stopScan(mScanCallback);
            } else {
                if (mAdapter != null)
                    mAdapter.stopLeScan(mLeScanCallback);
            }
        } catch (Exception e) {
            TelinkLog.d("蓝牙停止异常");
        }

        synchronized (this) {
            mStarted = false;
            mScanning = false;
        }

        if (mCallback != null)
            mCallback.onStoppedScan();
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    public boolean isEnabled() {
        return this.mAdapter != null
                && this.mAdapter.isEnabled();
    }

    /**
     * 是否支持BLE
     *
     * @param context
     * @return
     */
    public boolean isSupport(@NonNull Context context) {
        this.mContext = context;
        return this.getAdapter(context) != null;
    }

    public boolean enable(Context context) {
        BluetoothAdapter mAdapter = getAdapter(context);
        if (mAdapter == null)
            return false;
        if (mAdapter.isEnabled())
            return true;
        return mAdapter.enable();
    }

    public void disable(Context context) {
        BluetoothAdapter mAdapter = getAdapter(context);
        if (mAdapter == null) {
            return;
        }
        if (mAdapter.isEnabled()) {
            mAdapter.disable();
        }
    }

    public BluetoothAdapter getAdapter(Context context) {
        synchronized (this) {
            if (mAdapter == null) {
                BluetoothManager manager = (BluetoothManager) context
                        .getSystemService(Context.BLUETOOTH_SERVICE);
                this.mAdapter = manager.getAdapter();
            }
        }

        return this.mAdapter;
    }

    private class LocationCheckTask implements Runnable {

        @Override
        public void run() {

            if (mCallback != null) {
                mCallback.onScanFail(SCAN_FAILED_LOCATION_DISABLE);
            }
        }
    }

    public interface LeScanCallback {
        void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

        void onScanFail(int errorCode);

        void onStartedScan();

        void onStoppedScan();
    }
}
