package com.dds.webrtclib.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dds.webrtclib.R;
import com.dds.webrtclib.utils.Utils;

/**
 * 单聊控制界面
 * Created by dds on 2019/1/7.
 * android_shuai@163.com
 */
public class ChatSingleFragment extends Fragment {

    public View rootView;
    private TextView wr_switch_mute;
    private TextView wr_switch_hang_up;
    private TextView wr_switch_camera;
    private TextView wr_hand_free;
    private boolean enableMic = true;
    private boolean enableSpeaker = false;
    private boolean videoEnable;
    private ChatSingleActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ChatSingleActivity) getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoEnable = bundle.getBoolean("videoEnable");
        }
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
        return inflater.inflate(R.layout.wr_fragment_room_control_single, container, false);
    }

    private void initView(View rootView) {
        wr_switch_mute = rootView.findViewById(R.id.wr_switch_mute);
        wr_switch_hang_up = rootView.findViewById(R.id.wr_switch_hang_up);
        wr_switch_camera = rootView.findViewById(R.id.wr_switch_camera);
        wr_hand_free = rootView.findViewById(R.id.wr_hand_free);
        if (videoEnable) {
            wr_hand_free.setVisibility(View.GONE);
            wr_switch_camera.setVisibility(View.VISIBLE);
        } else {
            wr_hand_free.setVisibility(View.VISIBLE);
            wr_switch_camera.setVisibility(View.GONE);
        }
    }

    private void initListener() {
        wr_switch_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableMic = !enableMic;
                if (enableMic) {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_mute_default);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_switch_mute.setCompoundDrawables(null, drawable, null, null);
                } else {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_mute);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_switch_mute.setCompoundDrawables(null, drawable, null, null);
                }
                activity.toggleMic(enableMic);

            }
        });
        wr_switch_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.hangUp();
            }
        });
        wr_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.switchCamera();
            }
        });

        wr_hand_free.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSpeaker = !enableSpeaker;
                if (enableSpeaker) {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_hands_free);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_hand_free.setCompoundDrawables(null, drawable, null, null);
                } else {
                    Drawable drawable = ContextCompat.getDrawable(activity, R.drawable.webrtc_hands_free_default);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, Utils.dip2px(activity, 60), Utils.dip2px(activity, 60));
                    }
                    wr_hand_free.setCompoundDrawables(null, drawable, null, null);
                }
                activity.toggleSpeaker(enableSpeaker);
            }
        });
    }

}
