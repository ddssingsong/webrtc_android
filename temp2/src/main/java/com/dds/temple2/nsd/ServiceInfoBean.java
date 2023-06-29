package com.dds.temple2.nsd;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceInfoBean {
    // version
    private String v;
    private String time;
    private String uuid;

    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("time", this.time);
            jsonObject.put("uuid", this.uuid);

            jsonObject.put("v", this.v);
            return jsonObject.toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public ServiceInfoBean toObject(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            this.time = jsonObject.optString("time");
            this.uuid = jsonObject.optString("uuid");
            this.v = jsonObject.optString("v");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public String getTime() {
        return this.time;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getV() {
        return this.v;
    }

    public void setTime(String str) {
        this.time = str;
    }

    public void setUuid(String str) {
        this.uuid = str;
    }

    public void setV(String str) {
        this.v = str;
    }
}
