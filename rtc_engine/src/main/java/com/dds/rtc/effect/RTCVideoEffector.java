package com.dds.rtc.effect;


import android.opengl.GLES20;
import android.util.Log;

import com.dds.rtc.effect.filter.FrameImageFilter;
import com.dds.rtc.effect.filter.GPUImageFilter;
import com.dds.rtc.effect.filter.GPUImageFilterWrapper;
import com.dds.rtc.effect.filter.SkinSmoothFilter;
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
    private SkinSmoothFilter smoothFilter;

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
        smoothFilter = new SkinSmoothFilter();

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

        // 视频帧图像可能会旋转
        // 对于统一应用于整个图像的效果，例如灰度和棕褐色滤镜，这不是问题。
        // 需要指定坐标的效果很难使用。

        // 所以有些情况下需要在过滤前后做一些旋转校正
        // 但是，纹理之间的复制会发生两次
        // 我希望能够打开/关闭此功能，以便在不需要时不使用它

        Log.d(TAG, "processByteBufferFrame: stepTextureId = " + stepTextureId);
        if (context.getFrameInfo().isRotated()) {
            // TODO
        }

        for (FrameImageFilter filter : filters) {
            if (filter.isEnabled()) {
                stepTextureId = filter.filter(context, stepTextureId);
            }
        }

        if (context.getFrameInfo().isRotated()) {
            // TODO
        }

        return yuvBytesDumper.dump(stepTextureId, width, height, strideY, strideU, strideV);
    }

    public VideoFrame.Buffer processTextureBufferFrame(VideoFrame.TextureBuffer buffer) {
        int textureId = buffer.getTextureId();
        float[] finalGlMatrix = RendererCommon.convertMatrixFromAndroidGraphicsMatrix(buffer.getTransformMatrix());
        int width = buffer.getWidth();
        int height = buffer.getHeight();

        frameBuffer.setSize(width, height);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer.getFrameBufferId());
        // draw OES
        textureDrawer.drawOes(textureId, finalGlMatrix, width, height, 0, 0, width, height);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        textureId = frameBuffer.getTextureId();


        if (needToProcessFrame()) {
            frameBuffer1.setSize(width, height);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer1.getFrameBufferId());
            smoothFilter.prepare();
            smoothFilter.draw(textureId, finalGlMatrix, width, height);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            textureId = frameBuffer1.getTextureId();
        }
        return new TextureBufferImpl(width, height, VideoFrame.TextureBuffer.Type.RGB, textureId,
                RendererCommon.convertMatrixToAndroidGraphicsMatrix(finalGlMatrix), helper.getHandler(), null, null);
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
        yuvBytesReader.dispose();
        yuvBytesDumper.dispose();
        if (frameBuffer1 != null) {
            frameBuffer1.release();
        }
        if (smoothFilter != null) {
            smoothFilter.release();
        }
    }

}
