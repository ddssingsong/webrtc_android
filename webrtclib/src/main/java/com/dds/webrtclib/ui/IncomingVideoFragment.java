package com.dds.webrtclib.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dds.webrtclib.R;
import com.dds.webrtclib.WebRTCManager;
import com.dds.webrtclib.bean.WrUserInfo;
import com.dds.webrtclib.callback.WrCallBack;
import com.dds.webrtclib.widget.RoundedCornersTransformation;

import java.io.IOException;

/**
 * 视频来电界面
 * Created by dds on 2019/1/16.
 * android_shuai@163.com
 */
public class IncomingVideoFragment extends Fragment implements SurfaceHolder.Callback {
    private ImageView wr_invite_avatar;
    private TextView wr_invite_name;
    private String userId;

    private SurfaceView local_view;
    private Camera camera;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wr_fragment_incoming_video, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        wr_invite_avatar = view.findViewById(R.id.wr_invite_avatar);
        wr_invite_name = view.findViewById(R.id.wr_invite_name);
        local_view = view.findViewById(R.id.local_view_render);
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


        local_view.getHolder().addCallback(this);
        // 打开摄像头并将展示方向旋转90度
        camera = Camera.open();
        camera.setDisplayOrientation(90);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.release();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (camera != null) {
            camera.release();
            camera = null;
        }


    }
}
