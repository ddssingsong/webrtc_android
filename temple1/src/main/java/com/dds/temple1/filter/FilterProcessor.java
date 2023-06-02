package com.dds.temple1.filter;

import android.util.Log;

import androidx.annotation.Nullable;

import org.webrtc.VideoFrame;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;

public class FilterProcessor implements VideoProcessor {
    private static final String TAG = "FilterProcessor";
    private VideoSink mSink;

    @Override
    public void onCapturerStarted(boolean success) {

    }

    @Override
    public void onCapturerStopped() {

    }

    @Override
    public void onFrameCaptured(VideoFrame frame) {
        VideoFrame newFrame = addVideoFilter(frame);
        mSink.onFrame(newFrame);
    }


    @Override
    public void setSink(@Nullable VideoSink sink) {
        mSink = sink;
    }


    private VideoFrame addVideoFilter(VideoFrame frame) {
        int rotatedWidth = frame.getRotatedWidth();
        int rotatedHeight = frame.getRotatedHeight();
        int rotation = frame.getRotation();
        long timestampNs = frame.getTimestampNs();
        VideoFrame.Buffer buffer = frame.getBuffer();

        VideoFrame.TextureBuffer textureBuffer = null;
        if (buffer instanceof VideoFrame.TextureBuffer) {
            Log.d(TAG, "addVideoFilter: buffer is TextureBuffer");
            textureBuffer = (VideoFrame.TextureBuffer) buffer;
            int textureId = textureBuffer.getTextureId();

        }


        return frame;
    }

}
