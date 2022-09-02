package com.dds.core.voip;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dds.skywebrtc.CallSession;
import com.dds.skywebrtc.EnumType;
import com.dds.skywebrtc.SkyEngineKit;
import com.dds.webrtc.R;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by dds on 2020/5/24.
 * ddssingsong@163.com
 * 多人聊天场景
 */
public class FragmentMeeting extends Fragment implements CallSession.CallSessionCallback, View.OnClickListener {
    private SkyEngineKit gEngineKit;
    private CallMultiActivity activity;

    private FrameLayout multi_video_view;
    private int mScreenWidth;
    private LinkedHashMap<String,View> videoViews = new LinkedHashMap<>();

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
            // 设置宽高比例
            WindowManager manager = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                mScreenWidth = manager.getDefaultDisplay().getWidth();
            }
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

    @Override
    public void onDestroyView() {
        multi_video_view.removeAllViews();
        super.onDestroyView();

    }

    private void initView(View view) {

        multi_video_view = view.findViewById(R.id.multi_video_view);

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
            videoViews.put(callSession.mMyId,surfaceView);
            multi_video_view.addView(surfaceView);
            refreshView();
        }

    }

    @Override
    public void didReceiveRemoteVideoTrack(String userId) {
        View surfaceView = gEngineKit.getCurrentSession().setupRemoteVideo(userId, true);
        if (surfaceView != null) {
            videoViews.put(userId,surfaceView);
            multi_video_view.addView(surfaceView);
            refreshView();
        }
    }

    @Override
    public void didUserLeave(String userId) {
        for(Map.Entry<String, View> entry : videoViews.entrySet()) {
            String key = entry.getKey();
            if(key.equals(userId)){
                View value = entry.getValue();
                multi_video_view.removeView(value);
            }
        }
        videoViews.remove(userId);
        refreshView();
    }

    @Override
    public void didError(String error) {

    }

    @Override
    public void didDisconnected(String userId) {

    }

    private void refreshView(){
        int size = videoViews.size();
        int count = 0;
        for(Map.Entry<String, View> entry : videoViews.entrySet()) {
            View value = entry.getValue();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.height = getWidth(size);
            layoutParams.width = getWidth(size);
            layoutParams.leftMargin = getX(size, count);
            layoutParams.topMargin = getY(size, count);
            value.setLayoutParams(layoutParams);
            count++;
        }

    }


    private int getWidth(int size) {
        if (size <= 4) {
            return mScreenWidth / 2;
        } else if (size <= 9) {
            return mScreenWidth / 3;
        }
        return mScreenWidth / 3;
    }

    private int getX(int size, int index) {
        if (size <= 4) {
            if (size == 3 && index == 2) {
                return mScreenWidth / 4;
            }
            return (index % 2) * mScreenWidth / 2;
        } else if (size <= 9) {
            if (size == 5) {
                if (index == 3) {
                    return mScreenWidth / 6;
                }
                if (index == 4) {
                    return mScreenWidth / 2;
                }
            }

            if (size == 7 && index == 6) {
                return mScreenWidth / 3;
            }

            if (size == 8) {
                if (index == 6) {
                    return mScreenWidth / 6;
                }
                if (index == 7) {
                    return mScreenWidth / 2;
                }
            }
            return (index % 3) * mScreenWidth / 3;
        }
        return 0;
    }

    private int getY(int size, int index) {
        if (size < 3) {
            return mScreenWidth / 4;
        } else if (size < 5) {
            if (index < 2) {
                return 0;
            } else {
                return mScreenWidth / 2;
            }
        } else if (size < 7) {
            if (index < 3) {
                return mScreenWidth / 2 - (mScreenWidth / 3);
            } else {
                return mScreenWidth / 2;
            }
        } else if (size <= 9) {
            if (index < 3) {
                return 0;
            } else if (index < 6) {
                return mScreenWidth / 3;
            } else {
                return mScreenWidth / 3 * 2;
            }

        }
        return 0;
    }




}
