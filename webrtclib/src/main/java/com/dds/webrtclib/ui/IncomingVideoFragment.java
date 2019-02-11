package com.dds.webrtclib.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dds.webrtclib.R;

/**
 *  视频来电界面
 * Created by dds on 2019/1/16.
 * android_shuai@163.com
 */
public class IncomingVideoFragment extends Fragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wr_fragment_incoming_video, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {

    }
}
