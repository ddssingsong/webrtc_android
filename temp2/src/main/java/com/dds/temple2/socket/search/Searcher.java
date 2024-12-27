package com.dds.temple2.socket.search;

import com.dds.temple2.socket.inter.SearchListener;


public interface Searcher {

    /**
     * 开始搜索设备
     *
     * @param searchListener
     */
    void startSearch(SearchListener searchListener);

    /**
     * 设置能否被其他设备搜索到
     *
     * @param canBeSearched
     */
    void setCanBeSearched(boolean canBeSearched);

    /**
     * 是否能被其他设备搜索到
     *
     * @return
     */
    boolean isCanBeSearched();

}
