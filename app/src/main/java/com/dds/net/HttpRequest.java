package com.dds.net;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by dds on 2018/4/23.
 */

public interface HttpRequest {

    /**
     * get请求
     *
     * @param url      url
     * @param params   params
     * @param callback callback
     */
    void get(String url, Map<String, Object> params, ICallback callback);

    /**
     * post请求
     *
     * @param url      url
     * @param params   params
     * @param callback callback
     */
    void post(String url, Map<String, Object> params, ICallback callback);

    /**
     * 设置双向证书
     *
     * @param certificate certificate
     * @param pwd         pwd
     */
    void setCertificate(InputStream certificate, String pwd);
}
