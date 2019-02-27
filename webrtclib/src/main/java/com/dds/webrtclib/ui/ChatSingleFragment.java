package com.dds.webrtclib.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.bean.WrUserInfo;
import com.dds.webrtclib.callback.WrCallBack;
import com.dds.webrtclib.utils.Utils;
import com.dds.webrtclib.widget.RoundedCornersTransformation;

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
    private Chronometer chronometer;


    private boolean enableMic = true;
    private boolean enableSpeaker = false;
    private boolean videoEnable;
    private ChatSingleActivity activity;

    private ImageView wr_invite_avatar;
    private TextView wr_invite_name;
    private TextView wr_invite_tips;
    private String userId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ChatSingleActivity) getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            videoEnable = bundle.getBoolean("videoEnable");
            userId = getArguments().getString("userId");
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
        if (videoEnable) {
            return inflater.inflate(R.layout.wr_fragment_room_control_single_video, container, false);
        } else {
            return inflater.inflate(R.layout.wr_fragment_room_control_single_audio, container, false);
        }

    }

    private void initView(View view) {
        wr_switch_mute = view.findViewById(R.id.wr_switch_mute);
        wr_switch_hang_up = view.findViewById(R.id.wr_switch_hang_up);
        wr_switch_camera = view.findViewById(R.id.wr_switch_camera);
        wr_hand_free = view.findViewById(R.id.wr_hand_free);
        wr_invite_avatar = view.findViewById(R.id.wr_invite_avatar);
        wr_invite_name = view.findViewById(R.id.wr_invite_name);
        chronometer = view.findViewById(R.id.chronometer);
        wr_invite_tips = view.findViewById(R.id.wr_invite_tips);
        chronometer.setVisibility(View.GONE);
        if (videoEnable) {
            wr_hand_free.setVisibility(View.GONE);
            wr_switch_camera.setVisibility(View.VISIBLE);

        } else {
            wr_hand_free.setVisibility(View.VISIBLE);
            wr_switch_camera.setVisibility(View.GONE);

        }


        WrCallBack callBack = WebRTCManager.getInstance().getBushinessCallback();
        if (callBack != null && !TextUtils.isEmpty(userId)) {
            WrUserInfo inviteInfo = callBack.getInviteInfo(userId);
            if (inviteInfo != null) {
                String avatar = inviteInfo.getAvatar();
                Glide.with(this)
                        .load(avatar)
                        .transform(new RoundedCornersTransformation(getActivity(), 4))
                        .placeholder(R.drawable.webrtc_avatar_default)
                        .error(R.drawable.webrtc_avatar_default)
                        .into(wr_invite_avatar);
                String name = inviteInfo.getName();
                wr_invite_name.setText(name);

            }
        }


    }

    private void initListener() {
        // 静音
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
        // 挂断
        wr_switch_hang_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.hangUp();
            }
        });
        // 切换摄像头
        wr_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.switchCamera();
            }
        });

        // 扬声器
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

    // 接听成功，隐藏用户信息
    public void hideUserInfo() {
        if (wr_invite_avatar != null) {
            wr_invite_avatar.setVisibility(View.GONE);
            wr_invite_name.setVisibility(View.GONE);
            wr_invite_tips.setVisibility(View.GONE);
        }

    }

    public void hideTips() {
        if (wr_invite_tips != null) {
            wr_invite_tips.setVisibility(View.GONE);
        }

    }

    public void setChatTips(String tips) {
        if (wr_invite_tips != null) {
            wr_invite_tips.setText(tips);
        }

    }

    // 开启计时器
    public void startTimer() {
        if (chronometer != null) {
            chronometer.setVisibility(View.VISIBLE);
            chronometer.setBase(SystemClock.elapsedRealtime() - 1000 * WebRTCManager.getInstance().getTime());
            chronometer.start();
        }
    }

    @Override
    public void onDestroyView() {
        if (chronometer != null) {
            chronometer.stop();
        }
        super.onDestroyView();
        activity = null;
    }


}
