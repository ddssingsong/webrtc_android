package com.dds.temple0.ui.user;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


 import com.dds.base.net.HttpRequestPresenter;
import com.dds.base.net.ICallback;
import com.dds.temple0.socket.Urls;

import java.util.List;

public class UserListViewModel extends ViewModel {
    private static final String TAG = "UserListViewModel";
    private MutableLiveData<List<UserBean>> mList;
    private Thread thread;

    public LiveData<List<UserBean>> getUserList() {
        if (mList == null) {
            mList = new MutableLiveData<>();
            loadUsers();
        }
        return mList;
    }


    // 获取远程用户列表
    public void loadUsers() {
        if (thread != null && thread.isAlive()) {
            return;
        }
        thread = new Thread(() -> {
            String url = Urls.getUserList();
            HttpRequestPresenter.getInstance()
                    .get(url, null, new ICallback() {
                        @Override
                        public void onSuccess(String result) {
                            List<UserBean> userBeans = UserBean.parseJson(result);
                            mList.postValue(userBeans);
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