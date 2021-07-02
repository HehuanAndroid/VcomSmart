package com.vcom.smartlight.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAddTimingBinding;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.server.VcomService;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.AddTimingVM;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Banlap on 2021/5/28
 */
public class AddTimingActivity extends BaseMvvmActivity<AddTimingVM, ActivityAddTimingBinding>
        implements AddTimingVM.AddTimingCallBack {

    private boolean mIsEdit = false;

    private AtomicInteger hour;     //小时
    private AtomicInteger min;      //分钟

    private boolean isSunday=false;
    private boolean isMonday=false;
    private boolean isTuesday=false;
    private boolean isWednesday=false;
    private boolean isThursday=false;
    private boolean isFriday=false;
    private boolean isSaturday=false;

    private List<Equip> sceneEquipList = VcomSingleton.getInstance().getCurrentSceneEquips();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_timing;
    }

    @Override
    protected void initDatum() {

    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        getViewDataBind().tpSelectTime.setIs24HourView(true);
        hour = new AtomicInteger();
        min = new AtomicInteger();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour.set(getViewDataBind().tpSelectTime.getHour());
            min.set(getViewDataBind().tpSelectTime.getMinute());
        } else {
            hour.set(getViewDataBind().tpSelectTime.getCurrentHour());
            min.set(getViewDataBind().tpSelectTime.getCurrentMinute());
        }

        //banlap: 是否编辑定时 区分 添加定时 还是 修改定时
        mIsEdit = getIntent().getBooleanExtra("IsEdit", false);
        if(!mIsEdit) {
            getViewDataBind().rlDelete.setVisibility(View.GONE);
        } else {
            getViewDataBind().rlDelete.setVisibility(View.VISIBLE);
            String time = getIntent().getStringExtra("ItemTime");
            if(time!=null){
                int itemHour = Integer.parseInt(time.substring(0, time.indexOf(":")));
                int itemMinute = Integer.parseInt((time.substring(time.indexOf(":"))).substring(1));
                if (itemHour!=0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getViewDataBind().tpSelectTime.setHour(itemHour);
                        getViewDataBind().tpSelectTime.setMinute(itemMinute);
                    } else {
                        getViewDataBind().tpSelectTime.setCurrentHour(itemHour);
                        getViewDataBind().tpSelectTime.setCurrentMinute(itemMinute);
                    }
                }

            }

            String week = getIntent().getStringExtra("ItemWeek");
            if(week!=null) {
                String[] weekList = week.split(",");
                for(int i=0; i<weekList.length; i++) {
                    switch(weekList[i]) {
                        case "日": isSunday=!isSunday; break;
                        case "一": isMonday=!isMonday; break;
                        case "二": isTuesday=!isTuesday; break;
                        case "三": isWednesday=!isWednesday; break;
                        case "四": isThursday=!isThursday; break;
                        case "五": isFriday=!isFriday; break;
                        case "六": isSaturday=!isSaturday; break;
                    }
                }
            }

            getViewDataBind().tvSunday.setBackgroundResource(isSunday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvSunday.setTextColor(isSunday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvMonday.setBackgroundResource(isMonday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvMonday.setTextColor(isMonday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvTuesday.setBackgroundResource(isTuesday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvTuesday.setTextColor(isTuesday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvWednesday.setBackgroundResource(isWednesday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvWednesday.setTextColor(isWednesday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvThursday.setBackgroundResource(isThursday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvThursday.setTextColor(isThursday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvFriday.setBackgroundResource(isFriday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvFriday.setTextColor(isFriday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

            getViewDataBind().tvSaturday.setBackgroundResource(isSaturday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvSaturday.setTextColor(isSaturday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));

        }

        //banlap: 选择时间 监听
        getViewDataBind().tpSelectTime.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            hour.set(hourOfDay);
            min.set(minute);
        });

        //banlap: 选择星期 监听
        getViewDataBind().tvSunday.setOnClickListener(v-> {
            isSunday = !isSunday;
            getViewDataBind().tvSunday.setBackgroundResource(isSunday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvSunday.setTextColor(isSunday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvMonday.setOnClickListener(v-> {
            isMonday = !isMonday;
            getViewDataBind().tvMonday.setBackgroundResource(isMonday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvMonday.setTextColor(isMonday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvTuesday.setOnClickListener(v-> {
            isTuesday = !isTuesday;
            getViewDataBind().tvTuesday.setBackgroundResource(isTuesday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvTuesday.setTextColor(isTuesday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvWednesday.setOnClickListener(v-> {
            isWednesday = !isWednesday;
            getViewDataBind().tvWednesday.setBackgroundResource(isWednesday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvWednesday.setTextColor(isWednesday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvThursday.setOnClickListener(v-> {
            isThursday = !isThursday;
            getViewDataBind().tvThursday.setBackgroundResource(isThursday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvThursday.setTextColor(isThursday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvFriday.setOnClickListener(v-> {
            isFriday = !isFriday;
            getViewDataBind().tvFriday.setBackgroundResource(isFriday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvFriday.setTextColor(isFriday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });
        getViewDataBind().tvSaturday.setOnClickListener(v -> {
            isSaturday = !isSaturday;
            getViewDataBind().tvSaturday.setBackgroundResource(isSaturday? R.drawable.shape_round_green:R.drawable.shape_round_circle_white);
            getViewDataBind().tvSaturday.setTextColor(isSaturday? getResources().getColor(R.color.white):getResources().getColor(R.color.black_33));
        });

    }


    @Override
    public void viewDeleteTiming() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_2))
                .setMessage(getString(R.string.dialog_message_delete_timing))
                .setPositiveButton(getString(R.string.dialog_confirm), (dialogInterface, i) -> {
                    deleteTiming();
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawableResource(R.drawable.shape_switch_bg_gray_f9);
    }

    /**
     * banlap: 删除当前定时
     * */
    public void deleteTiming() {
        String itemTimingId = getIntent().getStringExtra("ItemTimingId"); //表示定时索引
        getViewModel().deleteTimingApi(itemTimingId);
        /*int itemTimingId = getIntent().getIntExtra("ItemTimingId", 0); //表示定时索引
        if(itemTimingId!=0) {
            byte opcode = (byte) 0xE5;     //闹钟 操作码
            byte[] param = new byte[] {(byte) 0x01, (byte) itemTimingId};

            if(sceneEquipList.size()>0) {
                for(int i=0; i < sceneEquipList.size(); i++) {
                    if (Integer.parseInt(sceneEquipList.get(i).getMeshAddress()) != -1) {
                        VcomService.getInstance().sendCommandNoResponse(opcode, Integer.parseInt(sceneEquipList.get(i).getMeshAddress()), param);
                    }
                }
            }
        }*/
    }

    @Override
    public void deleteTimingSuccess() {
        EventBus.getDefault().post(new MessageEvent(MessageEvent.timingReady));
        Toast.makeText(this, getString(R.string.toast_delete_success), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void deleteTimingFailure() {
        Toast.makeText(this, getString(R.string.toast_delete_error), Toast.LENGTH_SHORT).show();
    }

    /**
     * banlap: 保存场景定时信息
     * */
    @Override
    public void viewSaveTiming() {
        if(!isSunday && !isMonday && !isTuesday && !isWednesday && !isThursday && !isFriday && !isSaturday) {
            Toast.makeText(this, getString(R.string.toast_select_week), Toast.LENGTH_SHORT).show();
            return;
        }

        String time = hour.get() + ":" + (min.get()<10? "0"+min.get() : min.get());
        String week = (isSunday? "日,":"") + (isMonday? "一,":"") + (isTuesday? "二,":"") +
                (isWednesday? "三,":"") + (isThursday? "四,":"") + (isFriday? "五,":"") + (isSaturday? "六,":"");
        week = week.substring(0, week.length()-1);

        //banlap: 判断 添加定时还是修改定时
        if(mIsEdit) {
            String itemTimingId = getIntent().getStringExtra("ItemTimingId");
            String itemSceneId = getIntent().getStringExtra("ItemSceneId");
            String itemCreateTime = getIntent().getStringExtra("ItemCreateTime");

            if(itemSceneId !=null && itemTimingId !=null) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Map<String, Object> params = new HashMap<>();
                params.put("sceneId", itemSceneId);
                params.put("timingTime", time);
                params.put("timingWeek", week);
                params.put("timingIsStart", 1);
                params.put("timingId", itemTimingId);
                params.put("timingCreateTime", itemCreateTime);
                params.put("timingUpdateTime", simpleDateFormat.format(new Date()));

                getViewModel().saveTimingApi(true, params);
            }
        } else {
            String sceneId = getIntent().getStringExtra("SceneId");
            if(sceneId !=null) {
                Map<String, Object> params = new HashMap<>();
                params.put("sceneId", sceneId);
                params.put("timingTime", time);
                params.put("timingWeek", week);
                params.put("timingIsStart", 1);

                getViewModel().saveTimingApi(false, params);
            }
        }
        finish();
    }

    @Override
    public void timingSuccess(boolean isEdit) {
        String showText = (isEdit? getString(R.string.toast_update_success):getString(R.string.toast_add_success));
        EventBus.getDefault().post(new MessageEvent(MessageEvent.timingReady));
        Toast.makeText(this, showText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void timingFailure(boolean isEdit) {
        String showText = (isEdit? getString(R.string.toast_update_error):getString(R.string.toast_add_error));
        Toast.makeText(this, showText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void viewBack() {
        finish();
    }
}
