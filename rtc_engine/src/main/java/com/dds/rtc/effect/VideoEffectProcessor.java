package com.dds.rtc.effect;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;

public class VideoEffectProcessor implements VideoProcessor {
    private static final String TAG = "VideoEffectProcessor";
    private VideoSink mSink;
    private final RTCVideoEffector rtcVideoEffector;

    public VideoEffectProcessor(SurfaceTextureHelper helper, RTCVideoEffector rtcVideoEffector) {
        this.rtcVideoEffector = rtcVideoEffector;
        final Handler handler = helper.getHandler();
        ThreadUtils.invokeAtFrontUninterruptibly(handler, () -> {
            rtcVideoEffector.init(helper);
        });

    }

    @Override
    public void onCapturerStarted(boolean success) {
        Log.d(TAG, "onCapturerStarted: " + success);
    }

    @Override
    public void onCapturerStopped() {
        Log.d(TAG, "onCapturerStopped: ");
    }

    @Override
    public void onFrameCaptured(VideoFrame frame) {
        VideoFrame newFrame = handleVideoFilter(frame);
        mSink.onFrame(newFrame);
    }

    @Override
    public void setSink(@Nullable VideoSink sink) {
        mSink = sink;
    }


    private VideoFrame handleVideoFilter(VideoFrame frame) {
        VideoFrame.Buffer buffer = frame.getBuffer();
        if (buffer instanceof VideoFrame.TextureBuffer) {
            if (rtcVideoEffector.needToProcessFrame()) {
                VideoFrame.Buffer buffer1 = rtcVideoEffector.processTextureBufferFrame((VideoFrame.TextureBuffer) buffer);
                return new VideoFrame(buffer1, frame.getRotation(), frame.getTimestampNs());
            }
        } else if (buffer instanceof VideoFrame.I420Buffer) {
            // nop
            if (rtcVideoEffector.needToProcessFrame()) {
                VideoFrame.I420Buffer originalI420Buffer = buffer.toI420();
                VideoFrame.I420Buffer effectedI420Buffer = rtcVideoEffector.processByteBufferFrame(originalI420Buffer, frame.getRotation(), frame.getTimestampNs());
                VideoFrame effectedVideoFrame = new VideoFrame(
                        effectedI420Buffer, frame.getRotation(), frame.getTimestampNs());
                if (originalI420Buffer != null) {
                    originalI420Buffer.release();
                }
                return effectedVideoFrame;
            }
        }
        return frame;
    }

    public void dispose() {
        if (rtcVideoEffector != null) {
            rtcVideoEffector.dispose();
        }

    }

}
