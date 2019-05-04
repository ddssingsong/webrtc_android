package com.dds.webrtclib.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dds.webrtclib.R;
import com.dds.webrtclib.utils.Utils;

/**
 * 视频会议控制界面
 * Created by dds on 2019/1/2.
 * android_shuai@163.com
 */
public class ChatRoomFragment extends Fragment {

    public View rootView;
    private TextView wr_switch_mute;
    private TextView wr_switch_hang_up;
    private TextView wr_switch_camera;
    private ChatRoomActivity chatRoomActivity;

    private boolean enableMic = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        chatRoomActivity = (ChatRoomActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = onInitloadView(inflater, container, savedInstanceState);
            initView(rootView);
            initListener();
        }
        return rootView;
    }


    private View onInitloadView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.wr_fragment_room_control, container, false);
    }

    private void initView(View rootView) {
        wr_switch_mute = rootView.findViewById(R.id.wr_switch_mute);
        wr_switch_hang_up = rootView.findViewById(R.id.wr_switch_hang_up);
        wr_switch_camera = rootView.findViewById(R.id.wr_switch_camera);
    }

    private void initListener() {
        wr_switch_mute.setOnClickListener(v -> {
            enableMic = !enableMic;
            if (enableMic) {
                Drawable drawable = ContextCompat.getDrawable(chatRoomActivity, R.drawable.webrtc_mute_default);
                if (drawable != null) {
                    drawable.setBounds(0, 0, Utils.dip2px(chatRoomActivity, 60), Utils.dip2px(chatRoomActivity, 60));
                }
                wr_switch_mute.setCompoundDrawables(null, drawable, null, null);
            } else {
                Drawable drawable = ContextCompat.getDrawable(chatRoomActivity, R.drawable.webrtc_mute);
                if (drawable != null) {
                    drawable.setBounds(0, 0, Utils.dip2px(chatRoomActivity, 60), Utils.dip2px(chatRoomActivity, 60));
                }
                wr_switch_mute.setCompoundDrawables(null, drawable, null, null);
            }
            chatRoomActivity.toggleMic(enableMic);

        });
        wr_switch_hang_up.setOnClickListener(v -> chatRoomActivity.hangUp());
        wr_switch_camera.setOnClickListener(v -> chatRoomActivity.switchCamera());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null) {
            ViewGroup viewGroup = (ViewGroup) rootView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(rootView);
            }
        }
    }


}
