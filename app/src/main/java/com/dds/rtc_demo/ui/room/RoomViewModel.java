package com.dds.rtc_demo.ui.room;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSON;
import com.dds.rtc_demo.core.consts.Urls;
import com.dds.base.net.HttpRequestPresenter;
import com.dds.base.net.ICallback;

import java.util.List;

public class RoomViewModel extends ViewModel {
    private static final String TAG = "RoomViewModel";
    private MutableLiveData<List<RoomInfo>> mList;
    private Thread thread;

    public RoomViewModel() {
    }

    public MutableLiveData<List<RoomInfo>> getRoomList() {
        if (mList == null) {
            mList = new MutableLiveData<>();
            loadRooms();
        }
        return mList;
    }

    public void loadRooms() {
        if (thread != null && thread.isAlive()) {
            return;
        }
        thread = new Thread(() -> {
            String url = Urls.getRoomList();
            HttpRequestPresenter.getInstance().get(url, null, new ICallback() {
                @Override
                public void onSuccess(String result) {
                    Log.d(TAG, result);
                    List<RoomInfo> roomInfos = JSON.parseArray(result, RoomInfo.class);
                    mList.postValue(roomInfos);
                }

                @Override
                public void onFailure(int code, Throwable t) {
                    Log.d(TAG, "code:" + code + ",msg:" + t.toString());
                }
            });
        });
        thread.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
        }
    }

}