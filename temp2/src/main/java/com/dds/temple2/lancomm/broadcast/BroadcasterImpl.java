package com.dds.temple2.lancomm.broadcast;

import com.dds.temple2.lancomm.LanCommManager;


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
