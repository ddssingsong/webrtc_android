package com.dds.java;

/**
 * Created by dds on 2019/7/26.
 * android_shuai@163.com
 */
public interface IEvent {
    void onOpen();

    void onClose(int code, String reason, boolean remote);

    void onError(Exception ex);
}
