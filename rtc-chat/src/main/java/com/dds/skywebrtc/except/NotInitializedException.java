package com.dds.skywebrtc.except;

/**
 * Created by dds on 17/02/2018.
 */

public class NotInitializedException extends RuntimeException {
    public NotInitializedException() {
        super("Not init!!!");
    }
}
