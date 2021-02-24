package com.dds.core.voip;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

/**
 * Created by dds on 2020/5/24.
 * ddssingsong@163.com
 */
public class FragmentMeeting extends Fragment implements CallSession.CallSessionCallback, View.OnClickListener {
    private SkyEngineKit gEngineKit;
    private CallMultiActivity activity;
    private NineGridView grid_view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CallMultiActivity) getActivity();
        if (activity != null) {

        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        initView(view);
        init();
        return view;
    }

    private void initView(View view) {
        grid_view = view.findViewById(R.id.grid_view);

    }


    private void init() {
        gEngineKit = activity.getEngineKit();
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void didCallEndWithReason(EnumType.CallEndReason var1) {

    }

    @Override
    public void didChangeState(EnumType.CallState var1) {

    }

    @Override
    public void didChangeMode(boolean isAudioOnly) {

    }

    @Override
    public void didCreateLocalVideoTrack() {
        View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(true);
        if (surfaceView != null) {
            CallSession callSession = SkyEngineKit.Instance().getCurrentSession();
            grid_view.addView(callSession.mMyId, surfaceView);


        }

    }

    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        View surfaceView = gEngineKit.getCurrentSession().setupRemoteVideo(userId, true);
        if (surfaceView != null) {
            grid_view.addView(userId, surfaceView);
        }
    }

    @Override
    public void didUserLeave(String userId) {
        grid_view.removeView(userId);
    }

    @Override
    public void didError(String error) {

    }

    @Override
    public void didDisconnected(String userId) {

    }


}
