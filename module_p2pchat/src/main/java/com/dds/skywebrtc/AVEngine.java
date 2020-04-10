package com.dds.skywebrtc;

import com.dds.skywebrtc.engine.IEngine;

public class AVEngine implements IEngine {

    private IEngine iEngine;
    private static volatile AVEngine instance;

    public AVEngine(IEngine engine) {
        iEngine = engine;
    }

    public static AVEngine createEngine(IEngine engine) {
        if (null == instance) {
            synchronized (AVEngine.class) {
                if (null == instance) {
                    instance = new AVEngine(engine);
                }
            }
        }

        return instance;
    }


    @Override
    public void init() {
        if (iEngine == null) {
            return;
        }
        iEngine.init();
    }

    @Override
    public void joinRoom() {
        if (iEngine == null) {
            return;
        }
        iEngine.joinRoom();
    }

    @Override
    public void leaveRoom() {
        if (iEngine == null) {
            return;
        }
        iEngine.leaveRoom();
    }

    @Override
    public void startPreview() {
        if (iEngine == null) {
            return;
        }
        iEngine.startPreview();
    }

    @Override
    public void stopPreview() {
        if (iEngine == null) {
            return;
        }
        iEngine.stopPreview();
    }

    @Override
    public void startStream() {
        if (iEngine == null) {
            return;
        }
        iEngine.startStream();
    }

    @Override
    public void stopStream() {
        if (iEngine == null) {
            return;
        }
        iEngine.stopStream();
    }

    @Override
    public void setupRemoteVideo() {
        if (iEngine == null) {
            return;
        }
        iEngine.setupRemoteVideo();
    }

    @Override
    public void stopRemoteVideo() {
        if (iEngine == null) {
            return;
        }
        iEngine.stopRemoteVideo();
    }

}
