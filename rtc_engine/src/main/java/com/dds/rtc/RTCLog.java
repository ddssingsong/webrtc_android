package com.dds.rtc;

public class RTCLog {
    private static final String LOG_PREFIX = "RTC_";

    public static String createTag(String className) {
        return LOG_PREFIX + className;
    }

}
