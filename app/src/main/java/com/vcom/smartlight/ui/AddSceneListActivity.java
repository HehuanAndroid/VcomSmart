package com.vcom.smartlight.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vcom.smartlight.R;
import com.vcom.smartlight.base.BaseBindingAdapter;
import com.vcom.smartlight.base.BaseMvvmActivity;
import com.vcom.smartlight.databinding.ActivityAddSceneListBinding;
import com.vcom.smartlight.databinding.ItemAddSceneListBinding;
import com.vcom.smartlight.model.MessageEvent;
import com.vcom.smartlight.model.Region;
import com.vcom.smartlight.model.Scene;
import com.vcom.smartlight.singleton.VcomSingleton;
import com.vcom.smartlight.uivm.AddSceneListVM;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Banlap on 2021/2/26
 */
public class AddSceneListActivity extends BaseMvvmActivity<AddSceneListVM, ActivityAddSceneListBinding>
        implements AddSceneListVM.AddSceneListCallBack {

    private List<Scene> mScenesList = new ArrayList<>();
    private final List<Region> mRegionList = new ArrayList<>();

    private AddSceneListAdapter addSceneListAdapter;
    private String mRegionId;
    private int mSceneCount=0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_add_scene_list;
    }

    @Override
    protected void initViews() {
        getViewDataBind().setVm(getViewModel());
        getViewModel().setCallBack(this);

        getViewDataBind().tvMainNoSmartScene.setVisibility(View.VISIBLE);
        getViewDataBind().ivMainNoSmartScene.setVisibility(View.VISIBLE);
        getViewDataBind().btCreateScene.setVisibility(View.VISIBLE);
        getViewDataBind().rvAddSceneList.setVisibility(View.GONE);

        if (mScenesList.size() > 0) {
            getViewDataBind().tvMainNoSmartScene.setVisibility(View.GONE);
            getViewDataBind().ivMainNoSmartScene.setVisibility(View.GONE);
            getViewDataBind().btCreateScene.setVisibility(View.GONE);
            getViewDataBind().rvAddSceneList.setVisibility(View.VISIBLE);
        }

        addSceneListAdapter = new AddSceneListAdapter(this);
        getViewDataBind().rvAddSceneList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        getViewDataBind().rvAddSceneList.setAdapter(addSceneListAdapter);
        addSceneListAdapter.setItems(mScenesList);
        addSceneListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initDatum() {
        getSceneListData();
    }

    /*
     * banlap: ??????????????????????????????????????????
     * */
    private void getSceneListData() {
        mRegionList.addAll(VcomSingleton.getInstance().getUserRegion());
        //banlap: ??????????????????id
        mRegionId = getIntent().getStringExtra("CurrentRegionId");
        if (mRegionList.size() > 0 && mRegionId != null) {
            for (Region region : mRegionList) {
                if (region.getSpaceId().equals(mRegionId)) {
                    //banlap: ?????????????????????????????????
                    mScenesList.addAll(region.getSceneList());
                }
            }
            mSceneCount = mScenesList.size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    /*
     * banlap: EventBus ?????????????????????????????? sticky=true ??????????????????
     *           ???????????????????????????????????? ?????????????????????????????????
     * */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        switch (event.msgCode) {
            case MessageEvent.refreshRegion:
                getViewModel().getNewRegion();
                break;
            case MessageEvent.regionReady:
                mRegionList.clear();
                mScenesList.clear();
                getSceneListData();
                addSceneListAdapter.notifyDataSetChanged();
                //getViewDataBind().ivAddScene.setVisibility(View.VISIBLE);
                getViewDataBind().rlViewAdd.setVisibility(View.VISIBLE);
                getViewDataBind().prLoading.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //banlap: ??????????????????????????????????????????????????????????????????????????????
        //getViewDataBind().ivAddScene.setVisibility(View.GONE);
        getViewDataBind().rlViewAdd.setVisibility(View.GONE);
        getViewDataBind().prLoading.setVisibility(View.VISIBLE);
        if (!VcomSingleton.getInstance().getLoginUser().isEmpty()) {
            EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /*
    * banlap: ???????????????
    * */
    @Override
    public void addNewScene(){

        //getViewDataBind().btCreateScene.isRunning();


       /* Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message=new Message();
                message.what=0;
                mHandler.sendMessage(message);

            }
        }, 0, 50);//3????????????TimeTask???run??????*/

        if(!mRegionId.equals("")){
            List<Scene> addNewScene = new ArrayList<>();
            String sceneName = getString(R.string.add_new_scene) + mSceneCount++;
            Scene scene = new Scene(sceneName, "0");
            addNewScene.add(scene);
            //getViewModel().addSceneList(mRegions.get(regionIndex.get()).getSpaceId(), addNewScene);
            getViewModel().addSceneList(mRegionId, addNewScene);



        }
    }


    /*private Handler mHandler = new Handler(msg -> {
        if(msg.what == 0){
            if(mSceneCount<=100) {
                getViewDataBind().btCreateScene.setProgress(mSceneCount);
                mSceneCount++;
            }
        }
        return false;
    });
*/

    @Override
    public void viewBack() {
        finish();
    }

    /*
     * banlap: ???????????? ????????????
     * */
    @Override
    public void addNewSceneSuccess(List<Scene> scenes) {
        Toast.makeText(this, getString(R.string.toast_add_success), Toast.LENGTH_SHORT).show();
        //???????????????
        Intent addNewSceneIntent = new Intent(this, AddSceneActivity.class);
        addNewSceneIntent.putExtra("CurrentRegionId", mRegionId);
        addNewSceneIntent.putExtra("NewSceneDefaultName", scenes.get(0).getSceneName());
        startActivity(addNewSceneIntent);
        refreshData();
    }

    /*
     * banlap: ???????????? ????????????
     * */
    @Override
    public void addNewSceneFailure() {
        Toast.makeText(this, getString(R.string.toast_add_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshData() {

      /*  if(!data.equals("")) {
            Intent addNewSceneIntent = new Intent(getApplication(), AddSceneActivity.class);
            addNewSceneIntent.putExtra("CurrentRegionId", mRegionId);
            addNewSceneIntent.putExtra("NewSceneDefaultName", scene.get(0).getSceneName());
            getApplication().startActivity(addNewSceneIntent);

            EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));

            getViewDataBind().ivMainNoSmartScene.setVisibility(View.GONE);
            getViewDataBind().tvMainNoSmartScene.setVisibility(View.GONE);
            getViewDataBind().btCreateScene.setVisibility(View.GONE);
            getViewDataBind().rvAddSceneList.setVisibility(View.VISIBLE);
        }*/

        EventBus.getDefault().post(new MessageEvent(MessageEvent.refreshRegion));

        getViewDataBind().ivMainNoSmartScene.setVisibility(View.GONE);
        getViewDataBind().tvMainNoSmartScene.setVisibility(View.GONE);
        getViewDataBind().btCreateScene.setVisibility(View.GONE);
        getViewDataBind().rvAddSceneList.setVisibility(View.VISIBLE);
    }

    //banlap: ??????????????????
    public void goSceneManager(Scene scene) {
        Intent intent = new Intent(this, SceneManagerActivity.class);
        intent.putExtra("sceneId", scene.getSceneId());
        intent.putExtra("regionId", mRegionId);
        startActivity(intent);
    }

    /*
     * banlap: ????????????... ???????????? ?????????
     * */
    private class AddSceneListAdapter extends BaseBindingAdapter<Scene, ItemAddSceneListBinding> {

        public AddSceneListAdapter(Context mContext) {
            super(mContext);
        }

        @Override
        protected int getLayoutId(int layoutId) {
            return R.layout.item_add_scene_list;
        }

        @Override
        protected void onBindItem(ItemAddSceneListBinding binding, Scene item, int i) {
            binding.itemSceneNormalName.setText(item.getSceneName());
            if(item.getUserEquipList().size()>0) {
                binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_selected_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            } else {
                binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                        .getIdentifier("ic_scene_" + item.getSceneImg(), "drawable", mContext.getPackageName()));
            }

            binding.getRoot().setOnClickListener(v -> goSceneManager(item));
        }
    }

   /* private class AddSceneListAdapter extends RecyclerView.Adapter {

        private Context mContext;
        private List<Scene> mList;

        public AddSceneListAdapter(Context context, List<Scene> list){
            this.mContext = context;
            this.mList = list;
        }

        private class AddSceneListViewHolder extends RecyclerView.ViewHolder {
            public AddSceneListViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAddSceneListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.item_add_scene_list, parent,false);
            return new AddSceneListViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemAddSceneListBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.itemSceneNormalName.setText(mList.get(position).getSceneName());
            binding.itemSceneNormalIcon.setBackgroundResource(mContext.getResources()
                    .getIdentifier("ic_scene_" + mList.get(position).getSceneImg(), "drawable", mContext.getPackageName()));
            binding.getRoot().setOnClickListener(v -> goSceneManager(mList.get(position)));
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }*/
}



