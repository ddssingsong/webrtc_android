package com.dds.core.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dds.core.ui.event.MsgEvent;
import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * <pre>
 *     author : Jasper
 *     e-mail : 229605030@qq.com
 *     time   : 2021/02/01
 *     desc   :
 * </pre>
 */
public abstract class SingleCallFragment extends Fragment {
    private static final String TAG = "SingleCallFragment";
    ImageView minimizeImageView;
    ImageView portraitImageView;// 用户头像
    TextView nameTextView; // 用户昵称
    TextView descTextView;  // 状态提示用语
    Chronometer durationTextView; // 通话时长

    ImageView outgoingHangupImageView;
    ImageView incomingHangupImageView;
    ImageView acceptImageView;
    TextView tvStatus;
    View outgoingActionContainer;
    View incomingActionContainer;
    View connectedActionContainer;

    View lytParent;

    boolean isOutgoing = false;

    SkyEngineKit gEngineKit;


    CallSingleActivity callSingleActivity;

    CallHandler handler;
    boolean endWithNoAnswerFlag = false;
    boolean isConnectionClosed = false;

    public static final int WHAT_DELAY_END_CALL = 0x01;

    public static final int WHAT_NO_NET_WORK_END_CALL = 0x02;

    EnumType.CallState currentState;
    HeadsetPlugReceiver headsetPlugReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        handler = new CallHandler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        initView(view);
        init();
        return view;
    }

    @Override
    public void onDestroyView() {
        if (durationTextView != null)
            durationTextView.stop();
        refreshMessage(true);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }


    abstract int getLayout();


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MsgEvent<Object> messageEvent) {
        int code = messageEvent.getCode();
        Log.d(TAG, "onEvent code = $code; endWithNoAnswerFlag = $endWithNoAnswerFlag");
        if (code == MsgEvent.CODE_ON_CALL_ENDED) {
            if (endWithNoAnswerFlag) {
                didCallEndWithReason(EnumType.CallEndReason.Timeout);
            } else if (isConnectionClosed) {
                didCallEndWithReason(EnumType.CallEndReason.SignalError);
            } else {
                if (callSingleActivity != null) {
                    callSingleActivity.finish();
                }
            }
        } else if (code == MsgEvent.CODE_ON_REMOTE_RING) {
            descTextView.setText("对方已响铃");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callSingleActivity = (CallSingleActivity) getActivity();
        if (callSingleActivity != null) {
            isOutgoing = callSingleActivity.isOutgoing();
            gEngineKit = callSingleActivity.getEngineKit();
            headsetPlugReceiver = new HeadsetPlugReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            callSingleActivity.registerReceiver(headsetPlugReceiver, filter);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callSingleActivity.unregisterReceiver(headsetPlugReceiver);  //注销监听
        callSingleActivity = null;
    }


    public void initView(View view) {
        lytParent = view.findViewById(R.id.lytParent);
        minimizeImageView = view.findViewById(R.id.minimizeImageView);
        portraitImageView = view.findViewById(R.id.portraitImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        descTextView = view.findViewById(R.id.descTextView);
        durationTextView = view.findViewById(R.id.durationTextView);
        outgoingHangupImageView = view.findViewById(R.id.outgoingHangupImageView);
        incomingHangupImageView = view.findViewById(R.id.incomingHangupImageView);
        acceptImageView = view.findViewById(R.id.acceptImageView);
        tvStatus = view.findViewById(R.id.tvStatus);
        outgoingActionContainer = view.findViewById(R.id.outgoingActionContainer);
        incomingActionContainer = view.findViewById(R.id.incomingActionContainer);
        connectedActionContainer = view.findViewById(R.id.connectedActionContainer);

        durationTextView.setVisibility(View.GONE);
//        nameTextView.setText();
//        portraitImageView.setImageResource(R.mipmap.icon_default_header);
        if (isOutgoing) {
            handler.sendEmptyMessageDelayed(WHAT_DELAY_END_CALL, 60 * 1000);//1分钟之后未接通，则挂断电话
        }
    }

    public void init() {
    }

    // ======================================界面回调================================
    public void didCallEndWithReason(EnumType.CallEndReason callEndReason) {
        switch (callEndReason) {
            case Busy: {
                tvStatus.setText("对方忙线中");
                break;
            }
            case SignalError: {
                tvStatus.setText("连接断开");
                break;
            }
            case RemoteSignalError: {
                tvStatus.setText("对方网络断开");
                break;
            }
            case Hangup: {
                tvStatus.setText("挂断");
                break;
            }
            case MediaError: {
                tvStatus.setText("媒体错误");
                break;
            }
            case RemoteHangup: {
                tvStatus.setText("对方挂断");
                break;
            }
            case OpenCameraFailure: {
                tvStatus.setText("打开摄像头错误");
                break;
            }
            case Timeout: {
                tvStatus.setText("对方未接听");
                break;
            }
            case AcceptByOtherClient: {
                tvStatus.setText("在其它设备接听");
                break;
            }
        }
        incomingActionContainer.setVisibility(View.GONE);
        outgoingActionContainer.setVisibility(View.GONE);
        if (connectedActionContainer != null)
            connectedActionContainer.setVisibility(View.GONE);
        refreshMessage(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (callSingleActivity != null) {
                callSingleActivity.finish();
            }

        }, 1500);
    }

    public void didChangeState(EnumType.CallState state) {

    }

    public void didChangeMode(Boolean isAudio) {
    }

    public void didCreateLocalVideoTrack() {
    }

    public void didReceiveRemoteVideoTrack(String userId) {
    }

    public void didUserLeave(String userId) {
    }

    public void didError(String error) {
    }

    public void didDisconnected(String error) {
        handler.sendEmptyMessage(WHAT_NO_NET_WORK_END_CALL);
    }

    private void refreshMessage(Boolean isForCallTime) {
        if (callSingleActivity == null) {
            return;
        }
        // 刷新消息; demo中没有消息，不用处理这儿快逻辑
    }

    public void startRefreshTime() {
        CallSession session = SkyEngineKit.Instance().getCurrentSession();
        if (session == null) return;
        if (durationTextView != null) {
            durationTextView.setVisibility(View.VISIBLE);
            durationTextView.setBase(SystemClock.elapsedRealtime() - (System.currentTimeMillis() - session.getStartTime()));
            durationTextView.start();
        }
    }

    void runOnUiThread(Runnable runnable) {
        if (callSingleActivity != null) {
            callSingleActivity.runOnUiThread(runnable);
        }
    }

    class CallHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == WHAT_DELAY_END_CALL) {
                if (currentState != EnumType.CallState.Connected) {
                    endWithNoAnswerFlag = true;
                    if (callSingleActivity != null) {
                        SkyEngineKit.Instance().endCall();
                    }
                }
            } else if (msg.what == WHAT_NO_NET_WORK_END_CALL) {
                isConnectionClosed = true;
                if (callSingleActivity != null) {
                    SkyEngineKit.Instance().endCall();
                }
            }
        }

    }


    class HeadsetPlugReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                CallSession session = SkyEngineKit.Instance().getCurrentSession();
                if (session == null) {
                    return;
                }
                if (intent.getIntExtra("state", 0) == 0) { //拔出耳机
                    session.toggleHeadset(false);
                } else if (intent.getIntExtra("state", 0) == 1) { //插入耳机
                    session.toggleHeadset(true);
                }
            }
        }
    }
}
