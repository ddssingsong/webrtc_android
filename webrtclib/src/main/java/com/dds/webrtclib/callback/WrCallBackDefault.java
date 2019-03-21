package com.dds.webrtclib.callback;

import com.dds.webrtclib.bean.WrUserInfo;

import java.util.List;


/**
 * Created by dds on 2018/5/11.
 * android_shuai@163.com
 */

public class WrCallBackDefault implements WrCallBack {

    @Override
    public void terminateCall(boolean isVideo, String friendId, String message) {
    }

    @Override
    public void terminateIncomingCall(boolean isVideo, String friendId, String message, boolean isMiss) {
    }

    @Override
    public void startMeeting(String groupId) {

    }

    @Override
    public WrUserInfo getInviteInfo(String userId) {
        return null;
    }

    @Override
    public List<WrUserInfo> getRoomMembers(String ids) {
        return null;
    }


}
