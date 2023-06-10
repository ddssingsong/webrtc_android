package com.dds.temple2.nsd;

import android.net.nsd.NsdServiceInfo;

public class NsdServiceInfoEntity {
    private NsdServiceInfo nsdServiceInfo;
    private ServiceInfoBean serviceInfoBean;
    private int state = 0;
    private long timestamp;

    public NsdServiceInfo getNsdServiceInfo() {
        return this.nsdServiceInfo;
    }

    public ServiceInfoBean getServiceInfoBean() {
        return this.serviceInfoBean;
    }

    public int getState() {
        return this.state;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setNsdServiceInfo(NsdServiceInfo nsdServiceInfo) {
        this.nsdServiceInfo = nsdServiceInfo;
    }

    public void setServiceInfoBean(ServiceInfoBean serviceInfoBean) {
        this.serviceInfoBean = serviceInfoBean;
    }

    public void setState(int i) {
        this.state = i;
    }

    public void setTimestamp(long j) {
        this.timestamp = j;
    }
}
