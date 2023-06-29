package com.dds.rtc_chat.log;

public class RTCLog {
    private static final String LOG_TAG_PREFIX = "DDS_";

    public static String createTag(String className) {
        return LOG_TAG_PREFIX + className;
    }

}
