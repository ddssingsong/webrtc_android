package com.dds.voip;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dds.skywebrtc.AVEngineKit;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.webrtc.R;


public class AudioFragment extends Fragment implements CallSession.CallSessionCallback, View.OnClickListener {
    private ImageView minimizeImageView;
    private ImageView portraitImageView;
    private TextView nameTextView;
    private TextView descTextView;
    private TextView durationTextView;
    private ImageView muteImageView;
    private ImageView outgoingHangupImageView;
    private ImageView speakerImageView;
    private LinearLayout hangupLinearLayout;
    private ImageView incomingHangupImageView;
    private LinearLayout acceptLinearLayout;
    private ImageView acceptImageView;
    private AVEngineKit gEngineKit;

    private View outgoingActionContainer;
    private View incomingActionContainer;

    private boolean micEnabled = true;
    private boolean isSpeakerOn = false;
    private SingleCallActivity activity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        hangupLinearLayout = view.findViewById(R.id.hangupLinearLayout);
        incomingHangupImageView = view.findViewById(R.id.incomingHangupImageView);
        acceptLinearLayout = view.findViewById(R.id.acceptLinearLayout);
        acceptImageView = view.findViewById(R.id.acceptImageView);

        outgoingActionContainer = view.findViewById(R.id.outgoingActionContainer);
        incomingActionContainer = view.findViewById(R.id.incomingActionContainer);

        acceptImageView.setOnClickListener(this);
        incomingHangupImageView.setOnClickListener(this);

        outgoingHangupImageView.setOnClickListener(this);
        muteImageView.setOnClickListener(this);
        speakerImageView.setOnClickListener(this);
    }

    private void init() {
        gEngineKit = activity.getEngineKit();
        CallSession currentSession = gEngineKit.getCurrentSession();
        // 如果已经接通
        if (currentSession != null && currentSession.getState() == EnumType.CallState.Connected) {
            descTextView.setVisibility(View.GONE); // 提示语
            outgoingActionContainer.setVisibility(View.VISIBLE);
            durationTextView.setVisibility(View.VISIBLE);
        } else {
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
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SingleCallActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void didCallEndWithReason(EnumType.CallEndReason var1) {

    }

    @Override
    public void didChangeState(EnumType.CallState state) {
        runOnUiThread(() -> {
            if (state == EnumType.CallState.Connected) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.VISIBLE);
                descTextView.setVisibility(View.GONE);
                durationTextView.setVisibility(View.VISIBLE);
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
                session.joinHome(false);
            } else {
                activity.finish();
            }
        }
        // 挂断电话
        if (id == R.id.incomingHangupImageView || id == R.id.outgoingHangupImageView) {
            CallSession session = gEngineKit.getCurrentSession();
            if (session != null) {
                AVEngineKit.Instance().endCall();
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
                muteImageView.setSelected(!micEnabled);
            }
        }
        // 扬声器
        if (id == R.id.speakerImageView) {
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (isSpeakerOn) {
                isSpeakerOn = false;
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                isSpeakerOn = true;
                audioManager.setMode(AudioManager.MODE_NORMAL);

            }
            speakerImageView.setSelected(isSpeakerOn);
            audioManager.setSpeakerphoneOn(isSpeakerOn);
        }

        if (id == R.id.minimizeImageView) {
            activity.showFloatingView();
        }

    }
}
