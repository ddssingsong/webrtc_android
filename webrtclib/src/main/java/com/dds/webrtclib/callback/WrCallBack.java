package com.dds.webrtclib.callback;


import com.dds.webrtclib.bean.WrUserInfo;

import java.util.List;

/**
 * Created by dds on 2018/5/9.
 * android_shuai@163.com
 */

public interface WrCallBack {


    //拨出的电话挂断
    void terminateCall(boolean isVideo, String friendId, String message);

    // 接收的电话挂断
    void terminateIncomingCall(boolean isVideo, String friendId, String message, boolean isMiss);


    void startMeeting(String groupId);


    //获取需要在界面上显示的用户信息
    WrUserInfo getInviteInfo(String userId);


    List<WrUserInfo> getRoomMembers(String ids);


}
