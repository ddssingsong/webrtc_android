package com.dds.temple1.effect;

import android.media.effect.EffectFactory;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dds.temple1.effect.filter.GPUImageBeautyFilter;

import org.webrtc.SurfaceTextureHelper;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoProcessor;
import org.webrtc.VideoSink;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageToonFilter;

public class VideoEffectProcessor implements VideoProcessor {
    private static final String TAG = "FilterProcessor";
    private VideoSink mSink;
    private final RTCVideoEffector rtcVideoEffector;


    public VideoEffectProcessor(SurfaceTextureHelper helper) {
        rtcVideoEffector = new RTCVideoEffector();
//        rtcVideoEffector.addMediaEffectFilter(EffectFactory.EFFECT_FILLLIGHT);
        rtcVideoEffector.addGPUImageFilter(new GPUImageBeautyFilter());
//        rtcVideoEffector.addGPUImageFilter(new GPUImageGrayscaleFilter());
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
        VideoFrame newFrame = addVideoFilter(frame);
        mSink.onFrame(newFrame);
    }

    @Override
    public void setSink(@Nullable VideoSink sink) {
        mSink = sink;
    }


    private VideoFrame addVideoFilter(VideoFrame frame) {
        if (rtcVideoEffector.needToProcessFrame()) {
            VideoFrame.I420Buffer originalI420Buffer = frame.getBuffer().toI420();
            VideoFrame.I420Buffer effectedI420Buffer = rtcVideoEffector.processByteBufferFrame(originalI420Buffer, frame.getRotation(), frame.getTimestampNs());
            VideoFrame effectedVideoFrame = new VideoFrame(
                    effectedI420Buffer, frame.getRotation(), frame.getTimestampNs());
            if (originalI420Buffer != null) {
                originalI420Buffer.release();
            }
            return effectedVideoFrame;
        }
        return frame;

    }


    public void dispose() {
        if (rtcVideoEffector != null) {
            rtcVideoEffector.dispose();
        }

    }

}
