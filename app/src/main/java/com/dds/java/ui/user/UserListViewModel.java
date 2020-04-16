package com.dds.java.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class UserListViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<List<UserBean>> mList;


    public UserListViewModel() {
        mList = new MutableLiveData<>();
    }

    public LiveData<List<UserBean>> getUserList() {
        return mList;
    }
}