package com.dds.temple0.ui.user;

import android.text.TextUtils;
import android.util.Log;

import com.dds.temple0.ui.room.RoomInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2020/4/13.
 * android_shuai@163.com
 */
public class UserBean {
    private static final String TAG = "UserBean";
    private String userId;
    private String avatar;

    public UserBean(String userId1, String avatar1) {
        this.userId = userId1;
        this.avatar = avatar1;
    }

    public static List<UserBean> parseJson(String json) {
        List<UserBean> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String avatar1 = jsonObject.getString("avatar");
                String userId1 = jsonObject.getString("userId");
                UserBean userBean = new UserBean(userId1, avatar1);
                list.add(userBean);
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseJson: " + e);
        }
        return list;

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


}
