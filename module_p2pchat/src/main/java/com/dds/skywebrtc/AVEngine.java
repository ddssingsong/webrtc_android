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
    public void joinChannel() {
        if(iEngine==null){
            return;
        }
        iEngine.joinChannel();
    }

    @Override
    public void leaveChannel() {
        if(iEngine==null){
            return;
        }
        iEngine.leaveChannel();
    }

    @Override
    public void startPreview() {
        if(iEngine==null){
            return;
        }
        iEngine.startPreview();
    }

    @Override
    public void stopPreview() {
        if(iEngine==null){
            return;
        }
        iEngine.stopPreview();
    }

    @Override
    public void peerIn() {
        if(iEngine==null){
            return;
        }
        iEngine.peerIn();
    }
}
