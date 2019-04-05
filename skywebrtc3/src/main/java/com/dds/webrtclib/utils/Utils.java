package com.dds.webrtclib.utils;

import android.content.Context;

/**
 * Created by dds on 2019/1/2.
 * android_shuai@163.com
 */
public class Utils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
