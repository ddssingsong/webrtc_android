package com.dds.temple2.lancomm.search;

import com.dds.temple2.lancomm.LanCommManager;
import com.dds.temple2.lancomm.broadcast.BroadcastRunnable;
import com.dds.temple2.lancomm.data.Const;
import com.dds.temple2.lancomm.inter.SearchListener;


public class SearcherImpl implements Searcher {

    private static final String TAG = "SearcherImpl";
    private boolean canBeSearched = false;

    private SearcherImpl() {
    }

    public static SearcherImpl getImpl() {
        return SearcherImplHolder.sInstance;
    }

    private static class SearcherImplHolder {
        private static final SearcherImpl sInstance = new SearcherImpl();
    }

    @Override
    public void startSearch(SearchListener searchListener) {
        LanCommManager.thread_pool.execute(new SearchRunnable(searchListener));
    }

    @Override
    public void setCanBeSearched(boolean canBeSearched) {

        if (this.canBeSearched != canBeSearched) {
            if (canBeSearched) {
                //设备上线->广播通知其他设备
                LanCommManager.thread_pool.execute(new BroadcastRunnable().setType(Const.PACKET_TYPE_DEVICE_ADD));
            } else {
                //设备下线->广播通知其他设备
                LanCommManager.thread_pool.execute(new BroadcastRunnable().setType(Const.PACKET_TYPE_DEVICE_REMOVE));
            }
        }
        this.canBeSearched = canBeSearched;
        if (canBeSearched) {
            SearchRspThread.open();
        } else {
            SearchRspThread.close();
        }
    }

    @Override
    public boolean isCanBeSearched() {
        return canBeSearched;
    }
}
