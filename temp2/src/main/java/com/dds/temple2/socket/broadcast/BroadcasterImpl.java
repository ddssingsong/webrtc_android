package com.dds.temple2.socket.broadcast;

import com.dds.temple2.socket.LanCommManager;


public class BroadcasterImpl implements Broadcaster {

    private BroadcasterImpl() {
    }

    public static BroadcasterImpl getImpl() {
        return BroadcasterImplHolder.sInstance;
    }

    @Override
    public void broadcast(byte[] bytes) {
        LanCommManager.thread_pool.execute(new BroadcastRunnable().setData(bytes));
    }

    private static class BroadcasterImplHolder {
        private static final BroadcasterImpl sInstance = new BroadcasterImpl();
    }
}
