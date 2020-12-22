package com.dds.core.voip;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.dds.core.util.OSUtils;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

import org.webrtc.SurfaceViewRenderer;


/**
 * Created by dds on 2018/7/26.
 * android_shuai@163.com
 * 视频通话控制界面
 */
public class FragmentVideo extends Fragment implements CallSession.CallSessionCallback, View.OnClickListener {
    private static final String TAG = "FragmentVideo";
    private FrameLayout fullscreenRenderer;
    private FrameLayout pipRenderer;
    private LinearLayout inviteeInfoContainer;
    private ImageView portraitImageView;
    private TextView nameTextView;
    private TextView descTextView;
    private ImageView minimizeImageView;
    private ImageView outgoingAudioOnlyImageView;
    private ImageView outgoingHangupImageView;
    private LinearLayout audioLayout;
    private ImageView incomingAudioOnlyImageView;
    private LinearLayout hangupLinearLayout;
    private ImageView incomingHangupImageView;
    private LinearLayout acceptLinearLayout;
    private ImageView acceptImageView;
    private Chronometer durationTextView;
    private ImageView connectedAudioOnlyImageView;
    private ImageView connectedHangupImageView;
    private ImageView switchCameraImageView;
    private TextView tvStatus;
    private RelativeLayout lytParent;

    private View incomingActionContainer;
    private View outgoingActionContainer;
    private View connectedActionContainer;


    private CallSingleActivity activity;
    private SkyEngineKit gEngineKit;
    private boolean isOutgoing;
    private boolean isFromFloatingView;
    private SurfaceViewRenderer localSurfaceView;
    private SurfaceViewRenderer remoteSurfaceView;
    private EnumType.CallState currentState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        initView(view);
        init();

        return view;
    }


    private void initView(View view) {
        lytParent = view.findViewById(R.id.lytParent);
        fullscreenRenderer = view.findViewById(R.id.fullscreen_video_view);
        pipRenderer = view.findViewById(R.id.pip_video_view);
        inviteeInfoContainer = view.findViewById(R.id.inviteeInfoContainer);
        portraitImageView = view.findViewById(R.id.portraitImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        descTextView = view.findViewById(R.id.descTextView);
        minimizeImageView = view.findViewById(R.id.minimizeImageView);
        outgoingAudioOnlyImageView = view.findViewById(R.id.outgoingAudioOnlyImageView);
        outgoingHangupImageView = view.findViewById(R.id.outgoingHangupImageView);
        audioLayout = view.findViewById(R.id.audioLayout);
        incomingAudioOnlyImageView = view.findViewById(R.id.incomingAudioOnlyImageView);
        hangupLinearLayout = view.findViewById(R.id.hangupLinearLayout);
        incomingHangupImageView = view.findViewById(R.id.incomingHangupImageView);
        acceptLinearLayout = view.findViewById(R.id.acceptLinearLayout);
        acceptImageView = view.findViewById(R.id.acceptImageView);
        durationTextView = view.findViewById(R.id.durationTextView);
        connectedAudioOnlyImageView = view.findViewById(R.id.connectedAudioOnlyImageView);
        connectedHangupImageView = view.findViewById(R.id.connectedHangupImageView);
        switchCameraImageView = view.findViewById(R.id.switchCameraImageView);

        incomingActionContainer = view.findViewById(R.id.incomingActionContainer);
        outgoingActionContainer = view.findViewById(R.id.outgoingActionContainer);
        connectedActionContainer = view.findViewById(R.id.connectedActionContainer);
        tvStatus = view.findViewById(R.id.tvStatus);

        outgoingHangupImageView.setOnClickListener(this);
        incomingHangupImageView.setOnClickListener(this);
        connectedHangupImageView.setOnClickListener(this);
        acceptImageView.setOnClickListener(this);
        switchCameraImageView.setOnClickListener(this);
        pipRenderer.setOnClickListener(this);
        outgoingAudioOnlyImageView.setOnClickListener(this);
        incomingAudioOnlyImageView.setOnClickListener(this);
        connectedAudioOnlyImageView.setOnClickListener(this);

        minimizeImageView.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || OSUtils.isMiui() || OSUtils.isFlyme()) {

            lytParent.post(() -> {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) inviteeInfoContainer.getLayoutParams();
                params.topMargin = (int) (com.dds.core.util.Utils.getStatusBarHeight() * 1.2);
                inviteeInfoContainer.setLayoutParams(params);

                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) minimizeImageView.getLayoutParams();
                params1.topMargin = com.dds.core.util.Utils.getStatusBarHeight();
                minimizeImageView.setLayoutParams(params1);
            });

            pipRenderer.post(() -> {
                FrameLayout.LayoutParams params2 = (FrameLayout.LayoutParams) pipRenderer.getLayoutParams();
                params2.topMargin = (int) (com.dds.core.util.Utils.getStatusBarHeight() * 1.2);
                pipRenderer.setLayoutParams(params2);
            });
        }
//        if(isOutgoing){ //测试崩溃对方是否会停止
//            lytParent.postDelayed(() -> {
//                int i = 1 / 0;
//            }, 10000);
//        }
    }

    private void init() {
        gEngineKit = activity.getEngineKit();
        CallSession session = gEngineKit.getCurrentSession();
        currentState = session.getState();
        Log.d(TAG, "init currentState = " + currentState);
        if (session == null || EnumType.CallState.Idle == session.getState()) {
            activity.finish();
        } else if (EnumType.CallState.Connected == session.getState()) {
            incomingActionContainer.setVisibility(View.GONE);
            outgoingActionContainer.setVisibility(View.GONE);
            connectedActionContainer.setVisibility(View.VISIBLE);
            inviteeInfoContainer.setVisibility(View.GONE);
            minimizeImageView.setVisibility(View.VISIBLE);
            startRefreshTime();
        } else {
            if (isOutgoing) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.VISIBLE);
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_waiting);
            } else {
                incomingActionContainer.setVisibility(View.VISIBLE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.GONE);
                descTextView.setText(R.string.av_video_invite);
                if (currentState == EnumType.CallState.Incoming) {
                    View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(false);
                    Log.d(TAG, "init surfaceView != null is " + (surfaceView != null) + "; isOutgoing = " + isOutgoing + "; currentState = " + currentState);
                    if (surfaceView != null) {
                        localSurfaceView = (SurfaceViewRenderer) surfaceView;
                        localSurfaceView.setZOrderMediaOverlay(false);
                        fullscreenRenderer.addView(localSurfaceView);
                    }

                }
            }
        }

        if (isFromFloatingView) {
            didCreateLocalVideoTrack();
            if (session != null) {
                didReceiveRemoteVideoTrack(session.mTargetId);
            }
        }


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (CallSingleActivity) getActivity();
        if (activity != null) {
            isOutgoing = activity.isOutgoing();
            isFromFloatingView = activity.isFromFloatingView();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void didCallEndWithReason(EnumType.CallEndReason var1) {
        switch (var1) {
            case Busy:
                tvStatus.setText("对方忙线中");
                break;
            case SignalError:
                tvStatus.setText("信号差");
                break;
            case Hangup:
                tvStatus.setText("挂断");
                break;
            case MediaError:
                tvStatus.setText("媒体错误");
                break;
            case RemoteHangup:
                tvStatus.setText("对方挂断");
                break;
            case OpenCameraFailure:
                tvStatus.setText("打开摄像头错误");
                break;
            case Timeout:
                tvStatus.setText("超时");
                break;
            case AcceptByOtherClient:
                tvStatus.setText("在其它设备接听");
                break;
        }
        incomingActionContainer.setVisibility(View.GONE);
        outgoingActionContainer.setVisibility(View.GONE);
        connectedActionContainer.setVisibility(View.GONE);
        tvStatus.postDelayed(() -> {
            if (activity != null) {
                activity.finish();
            }
        }, 1500);
    }

    @Override
    public void didChangeState(EnumType.CallState state) {
        currentState = state;
        Log.d(TAG, "didChangeState, state = " + state);
        runOnUiThread(() -> {
            if (state == EnumType.CallState.Connected) {
                incomingActionContainer.setVisibility(View.GONE);
                outgoingActionContainer.setVisibility(View.GONE);
                connectedActionContainer.setVisibility(View.VISIBLE);
                inviteeInfoContainer.setVisibility(View.GONE);
                descTextView.setVisibility(View.GONE);
                minimizeImageView.setVisibility(View.VISIBLE);
                // 开启计时器
                startRefreshTime();
            } else {
                // do nothing now
            }
        });
    }

    @Override
    public void didChangeMode(boolean isAudio) {
        runOnUiThread(() -> activity.switchAudio());

    }

    @Override
    public void didCreateLocalVideoTrack() {
        if (localSurfaceView == null) {
            View surfaceView = gEngineKit.getCurrentSession().setupLocalVideo(true);
            localSurfaceView = (SurfaceViewRenderer) surfaceView;
        } else {
            localSurfaceView.setZOrderMediaOverlay(true);
        }
        Log.d(TAG, "didCreateLocalVideoTrack localSurfaceView != null is " + (localSurfaceView != null) + "; remoteSurfaceView == null = " + (remoteSurfaceView == null));
        if (localSurfaceView.getParent() != null) {
            ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
        }
        if (isOutgoing && remoteSurfaceView == null) {
            if (fullscreenRenderer.getChildCount() != 0)
                fullscreenRenderer.removeAllViews();
            fullscreenRenderer.addView(localSurfaceView);
        } else {
            if (pipRenderer.getChildCount() != 0)
                pipRenderer.removeAllViews();
            pipRenderer.addView(localSurfaceView);
        }
    }

    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        pipRenderer.setVisibility(View.VISIBLE);
        localSurfaceView.setZOrderMediaOverlay(true);
        if (isOutgoing && localSurfaceView != null) {
            if (localSurfaceView.getParent() != null) {
                ((ViewGroup) localSurfaceView.getParent()).removeView(localSurfaceView);
            }
            pipRenderer.addView(localSurfaceView);
        }
        View surfaceView = gEngineKit.getCurrentSession().setupRemoteVideo(userId, false);
        if (surfaceView != null) {
            fullscreenRenderer.setVisibility(View.VISIBLE);
            remoteSurfaceView = (SurfaceViewRenderer) surfaceView;
            fullscreenRenderer.removeAllViews();
            if (remoteSurfaceView.getParent() != null) {
                ((ViewGroup) remoteSurfaceView.getParent()).removeView(remoteSurfaceView);
            }
            fullscreenRenderer.addView(remoteSurfaceView);
        }


    }

    @Override
    public void didUserLeave(String userId) {

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
    public void onDestroyView() {
        super.onDestroyView();
        fullscreenRenderer.removeAllViews();
        pipRenderer.removeAllViews();

        if (durationTextView != null) {
            durationTextView.stop();
        }
    }

//    public void onBackPressed() {
//        CallSession session = gEngineKit.getCurrentSession();
//        if (session != null) {
//            SkyEngineKit.Instance().endCall();
//            activity.finish();
//        } else {
//            activity.finish();
//        }
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // 接听
        CallSession session = gEngineKit.getCurrentSession();
        if (id == R.id.acceptImageView) {
            if (session != null && session.getState() == EnumType.CallState.Incoming) {
                session.joinHome(session.getRoomId());
            } else {
                activity.finish();
            }
        }
        // 挂断电话
        if (id == R.id.incomingHangupImageView || id == R.id.outgoingHangupImageView ||
                id == R.id.connectedHangupImageView) {
            if (session != null) {
                SkyEngineKit.Instance().endCall();
                activity.finish();
            } else {
                activity.finish();
            }
        }

        // 切换摄像头
        if (id == R.id.switchCameraImageView) {
            if (session != null) {
                session.switchCamera();
            }
        }

        if (id == R.id.pip_video_view) {
            boolean isFullScreenRemote = fullscreenRenderer.getChildAt(0) == remoteSurfaceView;

            fullscreenRenderer.removeAllViews();
            pipRenderer.removeAllViews();
            if (isFullScreenRemote) {
                remoteSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(remoteSurfaceView);
                localSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(localSurfaceView);
            } else {
                localSurfaceView.setZOrderMediaOverlay(true);
                pipRenderer.addView(localSurfaceView);
                remoteSurfaceView.setZOrderMediaOverlay(false);
                fullscreenRenderer.addView(remoteSurfaceView);
            }
        }

        // 切换到语音拨打
        if (id == R.id.outgoingAudioOnlyImageView || id == R.id.incomingAudioOnlyImageView ||
                id == R.id.connectedAudioOnlyImageView) {
            if (session != null) {
                if (activity != null)
                    activity.isAudioOnly = true;
                session.switchToAudio();
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
