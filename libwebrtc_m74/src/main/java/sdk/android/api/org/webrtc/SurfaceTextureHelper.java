/*
 *  Copyright 2015 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;


import java.util.concurrent.Callable;

import org.webrtc.VideoFrame.TextureBuffer;

/**
 * Helper class for using a SurfaceTexture to create WebRTC VideoFrames. In order to create WebRTC
 * VideoFrames, render onto the SurfaceTexture. The frames will be delivered to the listener. Only
 * one texture frame can be in flight at once, so the frame must be released in order to receive a
 * new frame. Call stopListening() to stop receiveing new frames. Call dispose to release all
 * resources once the texture frame is released.
 */
public class SurfaceTextureHelper {
  private static final String TAG = "SurfaceTextureHelper";
  /**
   * Construct a new SurfaceTextureHelper sharing OpenGL resources with |sharedContext|. A dedicated
   * thread and handler is created for handling the SurfaceTexture. May return null if EGL fails to
   * initialize a pixel buffer surface and make it current. If alignTimestamps is true, the frame
   * timestamps will be aligned to rtc::TimeNanos(). If frame timestamps are aligned to
   * rtc::TimeNanos() there is no need for aligning timestamps again in
   * PeerConnectionFactory.createVideoSource(). This makes the timestamps more accurate and
   * closer to actual creation time.
   */
  public static SurfaceTextureHelper create(final String threadName,
      final EglBase.Context sharedContext, boolean alignTimestamps,
      final YuvConverter yuvConverter) {
    final HandlerThread thread = new HandlerThread(threadName);
    thread.start();
    final Handler handler = new Handler(thread.getLooper());

    // The onFrameAvailable() callback will be executed on the SurfaceTexture ctor thread. See:
    // http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/5.1.1_r1/android/graphics/SurfaceTexture.java#195.
    // Therefore, in order to control the callback thread on API lvl < 21, the SurfaceTextureHelper
    // is constructed on the |handler| thread.
    return ThreadUtils.invokeAtFrontUninterruptibly(handler, new Callable<SurfaceTextureHelper>() {

      @Override
      public SurfaceTextureHelper call() {
        try {
          return new SurfaceTextureHelper(sharedContext, handler, alignTimestamps, yuvConverter);
        } catch (RuntimeException e) {
          Logging.e(TAG, threadName + " create failure", e);
          return null;
        }
      }
    });
  }

  /**
   * Same as above with alignTimestamps set to false and yuvConverter set to new YuvConverter.
   *
   * @see #create(String, EglBase.Context, boolean, YuvConverter)
   */
  public static SurfaceTextureHelper create(
      final String threadName, final EglBase.Context sharedContext) {
    return create(threadName, sharedContext, /* alignTimestamps= */ false, new YuvConverter());
  }

  /**
   * Same as above with yuvConverter set to new YuvConverter.
   *
   * @see #create(String, EglBase.Context, boolean, YuvConverter)
   */
  public static SurfaceTextureHelper create(
      final String threadName, final EglBase.Context sharedContext, boolean alignTimestamps) {
    return create(threadName, sharedContext, alignTimestamps, new YuvConverter());
  }

  private final Handler handler;
  private final EglBase eglBase;
  private final SurfaceTexture surfaceTexture;
  private final int oesTextureId;
  private final YuvConverter yuvConverter;
    private final TimestampAligner timestampAligner;

  // These variables are only accessed from the |handler| thread.
    private VideoSink listener;
  // The possible states of this class.
  private boolean hasPendingTexture;
  private volatile boolean isTextureInUse;
  private boolean isQuitting;
  private int frameRotation;
  private int textureWidth;
  private int textureHeight;
  // |pendingListener| is set in setListener() and the runnable is posted to the handler thread.
  // setListener() is not allowed to be called again before stopListening(), so this is thread safe.
    private VideoSink pendingListener;
  final Runnable setListenerRunnable = new Runnable() {
    @Override
    public void run() {
      Logging.d(TAG, "Setting listener to " + pendingListener);
      listener = pendingListener;
      pendingListener = null;
      // May have a pending frame from the previous capture session - drop it.
      if (hasPendingTexture) {
        // Calling updateTexImage() is neccessary in order to receive new frames.
        updateTexImage();
        hasPendingTexture = false;
      }
    }
  };

  private SurfaceTextureHelper(EglBase.Context sharedContext, Handler handler,
      boolean alignTimestamps, YuvConverter yuvConverter) {
    if (handler.getLooper().getThread() != Thread.currentThread()) {
      throw new IllegalStateException("SurfaceTextureHelper must be created on the handler thread");
    }
    this.handler = handler;
    this.timestampAligner = alignTimestamps ? new TimestampAligner() : null;
    this.yuvConverter = yuvConverter;

    eglBase = EglBase.create(sharedContext, EglBase.CONFIG_PIXEL_BUFFER);
    try {
      // Both these statements have been observed to fail on rare occasions, see BUG=webrtc:5682.
      eglBase.createDummyPbufferSurface();
      eglBase.makeCurrent();
    } catch (RuntimeException e) {
      // Clean up before rethrowing the exception.
      eglBase.release();
      handler.getLooper().quit();
      throw e;
    }

    oesTextureId = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
    surfaceTexture = new SurfaceTexture(oesTextureId);
    setOnFrameAvailableListener(surfaceTexture, (SurfaceTexture st) -> {
      hasPendingTexture = true;
      tryDeliverTextureFrame();
    }, handler);
  }

  @TargetApi(21)
  private static void setOnFrameAvailableListener(SurfaceTexture surfaceTexture,
      SurfaceTexture.OnFrameAvailableListener listener, Handler handler) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      surfaceTexture.setOnFrameAvailableListener(listener, handler);
    } else {
      // The documentation states that the listener will be called on an arbitrary thread, but in
      // pratice, it is always the thread on which the SurfaceTexture was constructed. There are
      // assertions in place in case this ever changes. For API >= 21, we use the new API to
      // explicitly specify the handler.
      surfaceTexture.setOnFrameAvailableListener(listener);
    }
  }

  /**
   * Start to stream textures to the given |listener|. If you need to change listener, you need to
   * call stopListening() first.
   */
  public void startListening(final VideoSink listener) {
    if (this.listener != null || this.pendingListener != null) {
      throw new IllegalStateException("SurfaceTextureHelper listener has already been set.");
    }
    this.pendingListener = listener;
    handler.post(setListenerRunnable);
  }

  /**
   * Stop listening. The listener set in startListening() is guaranteded to not receive any more
   * onFrame() callbacks after this function returns.
   */
  public void stopListening() {
    Logging.d(TAG, "stopListening()");
    handler.removeCallbacks(setListenerRunnable);
    ThreadUtils.invokeAtFrontUninterruptibly(handler, () -> {
      listener = null;
      pendingListener = null;
    });
  }

  /**
   * Use this function to set the texture size. Note, do not call setDefaultBufferSize() yourself
   * since this class needs to be aware of the texture size.
   */
  public void setTextureSize(int textureWidth, int textureHeight) {
    if (textureWidth <= 0) {
      throw new IllegalArgumentException("Texture width must be positive, but was " + textureWidth);
    }
    if (textureHeight <= 0) {
      throw new IllegalArgumentException(
          "Texture height must be positive, but was " + textureHeight);
    }
    surfaceTexture.setDefaultBufferSize(textureWidth, textureHeight);
    handler.post(() -> {
      this.textureWidth = textureWidth;
      this.textureHeight = textureHeight;
    });
  }

  /** Set the rotation of the delivered frames. */
  public void setFrameRotation(int rotation) {
    handler.post(() -> this.frameRotation = rotation);
  }

  /**
   * Retrieve the underlying SurfaceTexture. The SurfaceTexture should be passed in to a video
   * producer such as a camera or decoder.
   */
  public SurfaceTexture getSurfaceTexture() {
    return surfaceTexture;
  }

  /** Retrieve the handler that calls onFrame(). This handler is valid until dispose() is called. */
  public Handler getHandler() {
    return handler;
  }

  /**
   * This function is called when the texture frame is released. Only one texture frame can be in
   * flight at once, so this function must be called before a new frame is delivered.
   */
  private void returnTextureFrame() {
    handler.post(() -> {
      isTextureInUse = false;
      if (isQuitting) {
        release();
      } else {
        tryDeliverTextureFrame();
      }
    });
  }

  public boolean isTextureInUse() {
    return isTextureInUse;
  }

  /**
   * Call disconnect() to stop receiving frames. OpenGL resources are released and the handler is
   * stopped when the texture frame has been released. You are guaranteed to not receive any more
   * onFrame() after this function returns.
   */
  public void dispose() {
    Logging.d(TAG, "dispose()");
    ThreadUtils.invokeAtFrontUninterruptibly(handler, () -> {
      isQuitting = true;
      if (!isTextureInUse) {
        release();
      }
    });
  }

  /**
   * Posts to the correct thread to convert |textureBuffer| to I420.
   *
   * @deprecated Use toI420() instead.
   */
  @Deprecated
  public VideoFrame.I420Buffer textureToYuv(final TextureBuffer textureBuffer) {
    return textureBuffer.toI420();
  }

  private void updateTexImage() {
    // SurfaceTexture.updateTexImage apparently can compete and deadlock with eglSwapBuffers,
    // as observed on Nexus 5. Therefore, synchronize it with the EGL functions.
    // See https://bugs.chromium.org/p/webrtc/issues/detail?id=5702 for more info.
    synchronized (EglBase.lock) {
      surfaceTexture.updateTexImage();
    }
  }

  private void tryDeliverTextureFrame() {
    if (handler.getLooper().getThread() != Thread.currentThread()) {
      throw new IllegalStateException("Wrong thread.");
    }
    if (isQuitting || !hasPendingTexture || isTextureInUse || listener == null) {
      return;
    }
    isTextureInUse = true;
    hasPendingTexture = false;

    updateTexImage();

    final float[] transformMatrix = new float[16];
    surfaceTexture.getTransformMatrix(transformMatrix);
    long timestampNs = surfaceTexture.getTimestamp();
    if (timestampAligner != null) {
      timestampNs = timestampAligner.translateTimestamp(timestampNs);
    }
    if (textureWidth == 0 || textureHeight == 0) {
      throw new RuntimeException("Texture size has not been set.");
    }
    final VideoFrame.Buffer buffer =
        new TextureBufferImpl(textureWidth, textureHeight, TextureBuffer.Type.OES, oesTextureId,
            RendererCommon.convertMatrixToAndroidGraphicsMatrix(transformMatrix), handler,
            yuvConverter, this ::returnTextureFrame);
    final VideoFrame frame = new VideoFrame(buffer, frameRotation, timestampNs);
    ((VideoSink) listener).onFrame(frame);
    frame.release();
  }

  private void release() {
    if (handler.getLooper().getThread() != Thread.currentThread()) {
      throw new IllegalStateException("Wrong thread.");
    }
    if (isTextureInUse || !isQuitting) {
      throw new IllegalStateException("Unexpected release.");
    }
    yuvConverter.release();
    GLES20.glDeleteTextures(1, new int[] {oesTextureId}, 0);
    surfaceTexture.release();
    eglBase.release();
    handler.getLooper().quit();
    if (timestampAligner != null) {
      timestampAligner.dispose();
    }
  }
}
