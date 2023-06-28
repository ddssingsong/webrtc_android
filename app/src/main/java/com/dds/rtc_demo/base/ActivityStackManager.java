package com.dds.rtc_demo.base;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.collection.ArrayMap;

/**
 * 应用程序Activity管理类，用于Activity管理和应用程序退出
 *
 * @author gong
 */
public class ActivityStackManager {
    private static final String TAG = "ActivityStackManager";
    private static volatile ActivityStackManager sInstance;

    private final ArrayMap<String, Activity> mActivitySet = new ArrayMap<>();

    /**
     * 当前 Activity 对象标记
     */
    private String mCurrentTag;

    private ActivityStackManager() {
    }

    public static ActivityStackManager getInstance() {
        // 加入双重校验锁
        if (sInstance == null) {
            synchronized (ActivityStackManager.class) {
                if (sInstance == null) {
                    sInstance = new ActivityStackManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取 Application 对象
     */
    public Application getApplication() {
        return getTopActivity().getApplication();
    }

    /**
     * 获取栈顶的 Activity
     */
    public Activity getTopActivity() {
        return mActivitySet.get(mCurrentTag);
    }

    /**
     * 销毁所有的 Activity
     */
    public void finishAllActivities() {
        finishAllActivities((Class<? extends Activity>) null);
    }


    /**
     * 获取栈底部的Activity
     */
    public Activity getBottomActivity() {
        Log.d(TAG, "getBottomActivity mActivitySet.size() = " + mActivitySet.size());
        if (mActivitySet.size() > 0) {
            return mActivitySet.get(mActivitySet.keyAt(0));
        } else {
            return getTopActivity();
        }

    }
    /**
     * 销毁所有的 Activity，除这些 Class 之外的 Activity
     */
    @SafeVarargs
    public final void finishAllActivities(Class<? extends Activity>... classArray) {
        String[] keys = mActivitySet.keySet().toArray(new String[]{});
        for (String key : keys) {
            Activity activity = mActivitySet.get(key);
            if (activity != null && !activity.isFinishing()) {
                boolean whiteClazz = false;
                if (classArray != null) {
                    for (Class<? extends Activity> clazz : classArray) {
                        if (activity.getClass() == clazz) {
                            whiteClazz = true;
                        }
                    }
                }
                // 如果不是白名单上面的 Activity 就销毁掉
                if (!whiteClazz) {
                    activity.finish();
                    mActivitySet.remove(key);
                }
            }
        }
    }

    /**
     * Activity 同名方法回调
     */
    public void onCreated(Activity activity) {
        mCurrentTag = getObjectTag(activity);
        mActivitySet.put(getObjectTag(activity), activity);
    }

    /**
     * Activity 同名方法回调
     */
    public void onDestroyed(Activity activity) {
        mActivitySet.remove(getObjectTag(activity));
        // 如果当前的 Activity 是最后一个的话
        if (getObjectTag(activity).equals(mCurrentTag)) {
            // 清除当前标记
            mCurrentTag = null;
        }
        if (mActivitySet.size() != 0) {
            mCurrentTag = mActivitySet.keyAt(mActivitySet.size() - 1);
        }
    }

    /**
     * 获取一个对象的独立无二的标记
     */
    private static String getObjectTag(Object object) {
        // 对象所在的包名 + 对象的内存地址
        return object.getClass().getName() + Integer.toHexString(object.hashCode());
    }

}
