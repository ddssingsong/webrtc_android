package com.dds.skywebrtc.inter;

/**
 * call records
 * Created by dds on 2020/2/10.
 * <p>
 * 去电  来电  通话时长  通话类型  是否接听
 */
public interface IRecordEvent {

    void onRecordOutGoing();

    void onRecordInComing();


}
