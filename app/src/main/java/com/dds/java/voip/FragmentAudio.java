package com.dds.java.voip;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

/**
 * Created by dds on 2018/7/26.
 * android_shuai@163.com
 * 语音通话控制界面
 */
public class FragmentAudio extends Fragment implements CallSession.CallSessionCallback, View.OnClickListener {
    private ImageView minimizeImageView;
    private ImageView portraitImageView;  // 用户头像
    private TextView nameTextView;        // 用户昵称
    private TextView descTextView;        // 状态提示用语
    private Chronometer durationTextView;    // 通话时长

    private ImageView muteImageView;
    private ImageView outgoingHangupImageView;
    private ImageView speakerImageView;
    private ImageView incomingHangupImageView;
    private ImageView acceptImageView;


    private SkyEngineKit gEngineKit;

    private View outgoingActionContainer;
    private View incomingActionContainer;


    private boolean micEnabled = false;  // 静音
    private boolean isSpeakerOn = false;// 扬声器
    private CallSingleActivity activity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CallSingleActivity) getActivity();
        if (activity != null) {
            gEngineKit = activity.getEngineKit();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);
        initView(view);
        init();
        return view;
    }

    private void initView(View view) {
        minimizeImageView = view.findViewById(R.id.minimizeImageView);
        portraitImageView = view.findViewById(R.id.portraitImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        descTextView = view.findViewById(R.id.descTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        muteImageView = view.findViewById(R.id.muteImageView);
        outgoingHangupImageView = view.findViewById(R.id.outgoingHangupImageView);
        speakerImageView = view.findViewById(R.id.speakerImageView);
        incomingHangupImageView = view.findViewById(R.id.incomingHangupImageView);
        acceptImageView = view.findViewById(R.id.acceptImageView);

        outgoingActionContainer = view.findViewById(R.id.outgoingActionContainer);
        incomingActionContainer = view.findViewById(R.id.incomingActionContainer);

        acceptImageView.setOnClickListener(this);
        incomingHangupImageView.setOnClickListener(this);
        outgoingHangupImageView.setOnClickListener(this);
        muteImageView.setOnClickListener(this);
        speakerImageView.setOnClickListener(this);
        minimizeImageView.setOnClickListener(this);

        durationTextView.setVisibility(View.GONE);
    }

    private void init() {
        CallSession currentSession = gEngineKit.getCurrentSession();
        // 如果已经接通
        if (currentSession != null && currentSession.getState() == EnumType.CallState.Connected) {
            descTextView.setVisibility(View.GONE); // 提示语
            outgoingActionContainer.setVisibility(View.VISIBLE);
            durationTextView.setVisibility(View.VISIBLE);
            startRefreshTime();
        } else {
            // 如果未接通
            if (activity.isOutgoing()) {
                descTextView.setText(R.string.av_waiting);
                outgoingActionContainer.setVisibility(View.VISIBLE);
                incomingActionContainer.setVisibility(View.GONE);
            } else {
                descTextView.setText(R.string.av_audio_invite);
                outgoingActionContainer.setVisibility(View.GONE);
                incomingActionContainer.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    // ======================================界面回调================================
    @Override
    public void didCallEndWithReason(EnumType.CallEndReason callEndReason) {

    }

    @Override
    public void didChangeState(EnumType.CallState state) {
        runOnUiThread(() -> {
            if (state == EnumType.CallState.Connected) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.VISIBLE);
                descTextView.setVisibility(View.GONE);

                startRefreshTime();
            } else {
                // do nothing now
            }
        });
    }

    @Override
    public void didChangeMode(boolean isAudio) {

    }

    @Override
    public void didCreateLocalVideoTrack() {

    }

    @Override
    public void didReceiveRemoteVideoTrack() {

    }

    @Override
    public void didError(String error) {

    }


    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(runnable);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 接听
        if (id == R.id.acceptImageView) {
            CallSession session = gEngineKit.getCurrentSession();
            if (session != null && session.getState() == EnumType.CallState.Incoming) {
                session.joinHome();
            } else {
                activity.finish();
            }
        }
        // 挂断电话
        if (id == R.id.incomingHangupImageView || id == R.id.outgoingHangupImageView) {
            CallSession session = gEngineKit.getCurrentSession();
            if (session != null) {
                SkyEngineKit.Instance().endCall();
                activity.finish();
            } else {
                activity.finish();
            }
        }
        // 静音
        if (id == R.id.muteImageView) {
            CallSession session = gEngineKit.getCurrentSession();
            if (session != null && session.getState() != EnumType.CallState.Idle) {
                if (session.muteAudio(!micEnabled)) {
                    micEnabled = !micEnabled;
                }
                muteImageView.setSelected(micEnabled);
            }
        }
        // 扬声器
        if (id == R.id.speakerImageView) {
            CallSession session = gEngineKit.getCurrentSession();
            if (session != null && session.getState() != EnumType.CallState.Idle) {
                if (session.toggleSpeaker(!isSpeakerOn)) {
                    isSpeakerOn = !isSpeakerOn;
                }
                speakerImageView.setSelected(isSpeakerOn);
            }

        }
        // 小窗
        if (id == R.id.minimizeImageView) {
            activity.showFloatingView();
        }

    }

    private void startRefreshTime() {
        CallSession session = SkyEngineKit.Instance().getCurrentSession();
        if (session == null) {
            return;
        }
        if (durationTextView != null) {
            durationTextView.setVisibility(View.VISIBLE);
            durationTextView.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - session.getStartTime()));
            durationTextView.start();
        }
    }
}
