package com.dds.temple2.socket;

import com.dds.temple2.socket.broadcast.Broadcaster;
import com.dds.temple2.socket.broadcast.BroadcasterImpl;
import com.dds.temple2.socket.ptop.Communicator;
import com.dds.temple2.socket.ptop.CommunicatorImpl;
import com.dds.temple2.socket.receive.Receiver;
import com.dds.temple2.socket.receive.ReceiverImpl;
import com.dds.temple2.socket.search.Searcher;
import com.dds.temple2.socket.search.SearcherImpl;
import com.dds.temple2.socket.utils.Dispatcher;

import java.util.concurrent.ThreadPoolExecutor;


public class LanCommManager {

    public static final ThreadPoolExecutor thread_pool =
            Dispatcher.newThreadPool("LanCommTask");

    /**
     * 获取广播器
     *
     * @return Broadcaster
     */
    public static Broadcaster getBroadcaster() {
        return BroadcasterImpl.getImpl();
    }

    /**
     * 获取接收器

     */
    public static Receiver getReceiver() {
        return ReceiverImpl.getImpl();
    }

    /**
     * 获取搜索器
     *
     * @return Searcher
     */
    public static Searcher getSearcher() {
        return SearcherImpl.getImpl();
    }

    /**
     * 获取点对点通讯器
     *
     * @return
     */
    public static Communicator getCommunicator() {
        return CommunicatorImpl.getImpl();
    }

}
