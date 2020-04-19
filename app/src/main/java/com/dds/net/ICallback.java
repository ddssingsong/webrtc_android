package com.dds.net;

/**
 * Created by dds on 2018/4/23.
 */

public interface ICallback {

    void onSuccess(String result);

    void onFailure(int code, Throwable t);
}
