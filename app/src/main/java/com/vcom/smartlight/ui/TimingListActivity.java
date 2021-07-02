package com.vcom.smartlight.ui;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityTimingListBinding;
import com.vcom.smartlight.databinding.ItemSceneTimingBinding;
import com.vcom.smartlight.model.Equip;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Timing;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.TimingListVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Banlap on 2021/5/28
 */
public class TimingListActivity extends BaseMvvmActivity<TimingListVM, ActivityTimingListBinding>
        implements TimingListVM.TimingListCallBack {

    private List<Timing> timingList = new ArrayList<>();;
    private TimingAdapter timingAdapter;

    private String mSceneId="";

    private String mHour="";
    private String mMin="";

    private boolean isSunday=false;
    private boolean isMonday=false;
    private boolean isTuesday=false;
    private boolean isWednesday=false;
    private boolean isThursday=false;
    private boolean isFriday=false;
    private boolean isSaturday=false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_timing_list;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshTiming));

        timingAdapter = new TimingAdapter(this);
        getViewDataBind().rvTimingList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        getViewDataBind().rvTimingList.setAdapter(timingAdapter);
        timingAdapter.setItems(timingList);
        timingAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initDatum() {
        mSceneId = getIntent().getStringExtra("SceneId");
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new MessageEvent(MessageEvent.timingReady));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch(event.msgCode) {
            case MessageEvent.refreshTiming:
                if (mSceneId != null && !mSceneId.equals("")) {
                    getViewModel().showTimingList(mSceneId);
                }
                break;
            case MessageEvent.timingReady:
                timingList.clear();
                timingList.addAll(VcomSingleton.getInstance().getTimingList());
                timingAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void goAddNewTiming() {
        Intent intent = new Intent(this, AddTimingActivity.class);
        intent.putExtra("SceneId", mSceneId);
        startActivity(intent);
       /* registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 0x1A) {
                if (result.getData() != null) {
                    mHour = result.getData().getStringExtra("NewHour");
                    mMin = result.getData().getStringExtra("NewMin");

                    isSunday = result.getData().getBooleanExtra("NewSunday", false);
                    isMonday = result.getData().getBooleanExtra("NewMonday", false);
                    isTuesday = result.getData().getBooleanExtra("NewTuesday", false);
                    isWednesday = result.getData().getBooleanExtra("NewWednesday", false);
                    isThursday = result.getData().getBooleanExtra("NewThursday", false);
                    isFriday = result.getData().getBooleanExtra("NewFriday", false);
                    isSaturday = result.getData().getBooleanExtra("NewSaturday", false);

                    int intWeekList = Integer.parseInt((isSaturday? "1": "0") +
                            (isFriday? "1": "0") + (isThursday? "1": "0") + (isWednesday? "1": "0") +
                            (isTuesday? "1": "0") + (isMonday? "1": "0") + (isSunday? "1": "0"));

                    Timing timing = new Timing();
                    timing.setHour(mHour);
                    timing.setMinute(mMin);
                    timing.setTimingId("0");
                    timing.setWeek(String.valueOf(intWeekList));

                    timingList.add(timing);
                    timingAdapter.notifyDataSetChanged();
                }

            }
        }).launch(intent);*/
    }

    @Override
    public void viewBack() {
        Timing thisTiming = new Timing();
        if(timingList.size()>0){
            for(int i=0; i<timingList.size(); i++) {
                if(timingList.get(i).isEnabled()) {
                    thisTiming = timingList.get(i);
                }
            }
        }

        if(thisTiming.getTimingId()!=null) {
            if(!thisTiming.getTimingId().equals("0") ) {
                String weekList = Integer.toBinaryString(Integer.parseInt(thisTiming.getWeek()));
                int weekRule=0;
                for(int j = weekList.length()-1; j>=0; j--) {
                    if(weekList.charAt(j) == '1') {
                        switch(weekRule) {
                            case 0: isSunday = !isSunday; break;
                            case 1: isMonday = !isMonday; break;
                            case 2: isTuesday = !isTuesday; break;
                            case 3: isWednesday = !isWednesday; break;
                            case 4: isThursday = !isThursday; break;
                            case 5: isFriday = !isFriday; break;
                            case 6: isSaturday = !isSaturday; break;
                        }
                    }
                    weekRule++;
                }
            }
        }

        Intent intent = new Intent();
        intent.putExtra("NewHour", thisTiming.getHour());
        intent.putExtra("NewMin", thisTiming.getMinute());
        intent.putExtra("NewSunday"   ,isSunday);
        intent.putExtra("NewMonday"   ,isMonday);
        intent.putExtra("NewTuesday"  ,isTuesday);
        intent.putExtra("NewWednesday",isWednesday);
        intent.putExtra("NewThursday" ,isThursday);
        intent.putExtra("NewFriday"   ,isFriday);
        intent.putExtra("NewSaturday" ,isSaturday);
        setResult(0x1A, intent);
        finish();
    }


    private class TimingAdapter extends BaseBindingAdapter<Timing, ItemSceneTimingBinding> {

        public TimingAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_scene_timing;
        }

        @Override
        protected void onBindItem(ItemSceneTimingBinding itemSceneTimingBinding, Timing item, int i) {
           /* String timing = item.getHour() + ":" + item.getMinute();
            itemSceneTimingBinding.tvShowTiming.setText(timing);

            //显示星期
            if(item.getTimingId().equals("0")) {
                String showWeek =
                        (isSunday? getString(R.string.sunday)+" ":"") +
                        (isMonday? getString(R.string.monday)+" ":"") +
                        (isTuesday? getString(R.string.tuesday)+" ":"")  +
                        (isWednesday? getString(R.string.wednesday)+" ":"") +
                        (isThursday? getString(R.string.thursday)+" ":"") +
                        (isFriday? getString(R.string.friday)+" ":"") +
                        (isSaturday? getString(R.string.saturday)+" ":"");
                itemSceneTimingBinding.tvShowWeek.setText(showWeek);
            } else {
                String weekList = Integer.toBinaryString(Integer.parseInt(item.getWeek()));
                StringBuilder showWeek= new StringBuilder();
                int weekRule=0;
                for(int j = weekList.length()-1; j>=0; j--) {
                    if(weekList.charAt(j) == '1') {
                        showWeek.append(weekRule == 0 ? getString(R.string.sunday)+" " : "")
                                .append(weekRule == 1 ? getString(R.string.monday)+" " : "")
                                .append(weekRule == 2 ? getString(R.string.tuesday)+" " : "")
                                .append(weekRule == 3 ? getString(R.string.wednesday)+" " : "")
                                .append(weekRule == 4 ? getString(R.string.thursday)+" " : "")
                                .append(weekRule == 5 ? getString(R.string.friday)+" " : "")
                                .append(weekRule == 6 ? getString(R.string.saturday)+" " : "");
                    }
                    weekRule++;

                }
                itemSceneTimingBinding.tvShowWeek.setText(showWeek.toString());

            }*/

            /*if (item.isEnabled()) {
                itemSceneTimingBinding.swTimingSwitch.setChecked(true);
            } else {
                itemSceneTimingBinding.swTimingSwitch.setChecked(false);
            }*/

            itemSceneTimingBinding.tvShowTiming.setText(item.getTimingTime());
            itemSceneTimingBinding.tvShowWeek.setText(item.getTimingWeek());

            if (item.getTimingIsStart().equals("1")) {
                itemSceneTimingBinding.ivTimingSwitch.setBackgroundResource(R.drawable.ic_switch_on);
            } else {
                itemSceneTimingBinding.ivTimingSwitch.setBackgroundResource(R.drawable.ic_switch_off);
            }

            itemSceneTimingBinding.getRoot().setOnClickListener(v-> {
                Intent intent = new Intent(mContext, AddTimingActivity.class);
                intent.putExtra("IsEdit", true);
                intent.putExtra("ItemTime", item.getTimingTime());
                intent.putExtra("ItemWeek", item.getTimingWeek());
                intent.putExtra("ItemTimingId", item.getTimingId());
                intent.putExtra("ItemSceneId", item.getSceneId());
                intent.putExtra("ItemCreateTime", item.getTimingCreateTime());
                startActivity(intent);
            });

            itemSceneTimingBinding.ivTimingSwitch.setOnClickListener(v-> {
                //setIsEnable();
                switch(item.getTimingIsStart()) {
                    case "1": item.setTimingIsStart("0"); break;
                    case "0": item.setTimingIsStart("1"); break;
                }
                notifyDataSetChanged();
            });
        }

       /* private void setIsEnable(){
            for(Timing timing : items) {
                timing.setEnabled(false);
            }
            notifyDataSetChanged();
        }*/
    }
}
