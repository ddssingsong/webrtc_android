package com.dds.net.urlconn;


import com.dds.net.HttpRequest;
import com.dds.net.ICallback;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by dds on 2019/12/20.
 */
public class UrlConnRequest implements HttpRequest {


    public UrlConnRequest() {
    }

    @Override
    public void get(String url, Map<String, Object> params, ICallback callback) {
        try {
            String param = null;
            if (params != null) {
                param = UrlConnUtils.builderUrlParams(params);

            }
            String s = UrlConnUtils.sendGet(url, param);
            callback.onSuccess(s);

        } catch (Exception e) {
            callback.onFailure(-1, e);
        }

    }

    @Override
    public void post(String url, Map<String, Object> params, ICallback callback) {
        try {
            String postStr = null;
            if (params != null) {
                postStr = UrlConnUtils.builderUrlParams(params);
            }
            String result = UrlConnUtils.sendPost(url, postStr);
            callback.onSuccess(result);
        } catch (Exception e) {
            callback.onFailure(-1, e);
        }
    }

    @Override
    public void setCertificate(InputStream certificate, String pwd) {

    }
}
