package com.dds.temple2.lancomm.sender;


public class Command {

    private String destIp;

    private byte[] data;

    private Callback callback;


    public String getDestIp() {
        return destIp;
    }

    public Command setDestIp(String destIp) {
        this.destIp = destIp;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public Command setData(byte[] data) {
        this.data = data;
        return this;
    }

    public Callback getCallback() {
        return callback;
    }

    public Command setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 发送消息的回调
     */
    public interface Callback {

        /**
         * 发送成功
         */
        void onSuccess();

        /**
         * 发送失败
         *
         * @param code
         */
        void onError(int code);

        /**
         * 对方已接收到消息
         */
        void onReceived();
    }

}
