package com.dds.temple0.ui.room;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2020/5/1.
 * ddssingsong@163.com
 */
public class RoomInfo {
    private static final String TAG = "RoomInfo";
    private String roomId;
    private String userId;
    private int maxSize;
    private int currentSize;

    public RoomInfo() {

    }

    public RoomInfo(String roomId1, String userId1, int maxSize1, int currentSize1) {
        this.roomId = roomId1;
        this.userId = userId1;
        this.maxSize = maxSize1;
        this.currentSize = currentSize1;
    }

    public static List<RoomInfo> parseJson(String json) {
        List<RoomInfo> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String roomId1 = jsonObject.getString("roomId");
                String userId1 = jsonObject.getString("userId");
                int maxSize1 = jsonObject.getInt("maxSize");
                int currentSize1 = jsonObject.getInt("currentSize");
                RoomInfo roomInfo = new RoomInfo(roomId1, userId1, maxSize1, currentSize1);
                list.add(roomInfo);
            }
        } catch (JSONException e) {
            Log.e(TAG, "parseJson: " + e);
        }
        return list;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
}
