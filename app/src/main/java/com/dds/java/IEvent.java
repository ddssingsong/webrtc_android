package com.dds.java;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public interface IEvent {
    void onOpen();

    void loginSuccess(String json);

    void logout(String str);

}
