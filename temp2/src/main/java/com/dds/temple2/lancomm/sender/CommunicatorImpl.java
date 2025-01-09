package com.dds.temple2.lancomm.sender;

import com.dds.temple2.lancomm.LanCommManager;

public class CommunicatorImpl implements Communicator {

    private CommunicatorImpl() {
    }

    public static CommunicatorImpl getImpl() {
        return CommunicatorImplHolder.sInstance;
    }

    private static class CommunicatorImplHolder {
        private static final CommunicatorImpl sInstance = new CommunicatorImpl();
    }

    @Override
    public void sendCommand(Command command) {
        LanCommManager.thread_pool.execute(new CommandRunnable(command));
    }
}
