package com.dds.rtc.effect;


import android.opengl.GLES20;

import com.dds.rtc.effect.filter.FrameImageFilter;
import com.dds.rtc.effect.filter.GPUImageFilter;
import com.dds.rtc.effect.filter.GPUImageFilterWrapper;
import com.dds.rtc.effect.format.YuvByteBufferDumper;
import com.dds.rtc.effect.format.YuvByteBufferReader;

import org.webrtc.GlRectDrawer;
import org.webrtc.GlTextureFrameBuffer;
import org.webrtc.GlUtil;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.TextureBufferImpl;
import org.webrtc.ThreadUtils;
import org.webrtc.VideoFrame;
import org.webrtc.VideoFrameDrawer;

import java.util.ArrayList;
import java.util.List;


public class RTCVideoEffector {

    public static final String TAG = RTCVideoEffector.class.getSimpleName();

    public RTCVideoEffector() {
    }

    private final VideoEffectorContext context = new VideoEffectorContext();
    private final List<FrameImageFilter> filters = new ArrayList<>();
    private boolean enabled = true;

    private YuvByteBufferReader yuvBytesReader;
    private YuvByteBufferDumper yuvBytesDumper;

    private SurfaceTextureHelper helper;

    private final GlRectDrawer textureDrawer = new GlRectDrawer();
    private GlTextureFrameBuffer frameBuffer;
    private GlTextureFrameBuffer frameBuffer1;

    void init(SurfaceTextureHelper helper) {

        VideoEffectorLogger.d(TAG, "init");

        this.helper = helper;

        yuvBytesReader = new YuvByteBufferReader();
        yuvBytesReader.init();

        yuvBytesDumper = new YuvByteBufferDumper();
        yuvBytesDumper.init();


        for (FrameImageFilter filter : filters) {
            filter.init();
        }

        frameBuffer = new GlTextureFrameBuffer(GLES20.GL_RGBA);
        frameBuffer1 = new GlTextureFrameBuffer(GLES20.GL_RGBA);

        GlUtil.checkNoGLES2Error("RTCVideoEffector.init");
    }


    public void addGPUImageFilter(GPUImageFilter filter) {
        VideoEffectorLogger.d(TAG, "addGPUImageFilter: " + filter.toString());
        this.filters.add(new GPUImageFilterWrapper(filter));
    }

    public void addGPUImageFilter(GPUImageFilter filter, GPUImageFilterWrapper.Listener listener) {
        VideoEffectorLogger.d(TAG, "addGPUImageFilter: " + filter.toString() +
                ", listener: " + listener);
        this.filters.add(new GPUImageFilterWrapper(filter, listener));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public VideoFrame.I420Buffer processByteBufferFrame(VideoFrame.I420Buffer i420Buffer, int rotation, long timestamp) {

        if (!needToProcessFrame()) {
            return i420Buffer;
        }

        // TODO: 还有一种方法可以转换为直接
        if (!i420Buffer.getDataY().isDirect() || !i420Buffer.getDataU().isDirect() || !i420Buffer.getDataV().isDirect()) {
            return i420Buffer;
        }
        int width = i420Buffer.getWidth();
        int height = i420Buffer.getHeight();
        int strideY = i420Buffer.getStrideY();
        int strideU = i420Buffer.getStrideU();
        int strideV = i420Buffer.getStrideV();

        context.updateFrameInfo(width, height, rotation, timestamp);

        int stepTextureId = yuvBytesReader.read(i420Buffer);

        for (FrameImageFilter filter : filters) {
            if (filter.isEnabled()) {
                stepTextureId = filter.filter(context, stepTextureId);
            }
        }

        return yuvBytesDumper.dump(stepTextureId, width, height, strideY, strideU, strideV);
    }

    public VideoFrame.Buffer processTextureBufferFrame(VideoFrame.TextureBuffer buffer, int rotation, long timestamp) {
        if (!needToProcessFrame()) {
            return buffer;
        }
        int width = buffer.getWidth();
        int height = buffer.getHeight();

        context.updateFrameInfo(width, height, rotation, timestamp);

        frameBuffer.setSize(width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.getFrameBufferId());
        // draw OES
        VideoFrameDrawer.drawTexture(textureDrawer, buffer, buffer.getTransformMatrix(), width, height, 0, 0, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        int textureId = frameBuffer.getTextureId();

        for (FrameImageFilter filter : filters) {
            if (filter.isEnabled()) {
                textureId = filter.filter(context, textureId);
            }
        }
        return new TextureBufferImpl(width, height, VideoFrame.TextureBuffer.Type.RGB, textureId,
                buffer.getTransformMatrix(), helper.getHandler(), null, null);
    }


    boolean needToProcessFrame() {
        if (!enabled) {
            return false;
        }
        if (!filters.isEmpty()) {
            for (FrameImageFilter filter : this.filters) {
                if (filter.isEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void dispose() {
        if (this.helper != null) {
            return;
        }
        ThreadUtils.invokeAtFrontUninterruptibly(this.helper.getHandler(), () ->
                disposeInternal()
        );
    }

    private void disposeInternal() {
        for (FrameImageFilter filter : filters) {
            filter.dispose();
        }
        if (frameBuffer != null) {
            frameBuffer.release();
        }
        yuvBytesReader.dispose();
        yuvBytesDumper.dispose();
        if (frameBuffer1 != null) {
            frameBuffer1.release();
        }
    }

}
