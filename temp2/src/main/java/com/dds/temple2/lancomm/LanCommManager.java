package com.dds.temple2.lancomm;

import com.dds.temple2.lancomm.broadcast.Broadcaster;
import com.dds.temple2.lancomm.broadcast.BroadcasterImpl;
import com.dds.temple2.lancomm.sender.Communicator;
import com.dds.temple2.lancomm.sender.CommunicatorImpl;
import com.dds.temple2.lancomm.receive.Receiver;
import com.dds.temple2.lancomm.receive.ReceiverImpl;
import com.dds.temple2.lancomm.search.Searcher;
import com.dds.temple2.lancomm.search.SearcherImpl;
import com.dds.temple2.lancomm.utils.Dispatcher;

import java.util.concurrent.ThreadPoolExecutor;


public class LanCommManager {

    public static final ThreadPoolExecutor thread_pool = Dispatcher.newThreadPool("LanTask");

    // broadcast
    public static Broadcaster getBroadcaster() {
        return BroadcasterImpl.getImpl();
    }

    // search
    public static Searcher getSearcher() {
        return SearcherImpl.getImpl();
    }

    // receiver
    public static Receiver getReceiver() {
        return ReceiverImpl.getImpl();
    }

    // sender
    public static Communicator getCommunicator() {
        return CommunicatorImpl.getImpl();
    }

}
