package cn.wildfirechat.avenginekit.a;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class e implements Parcelable.Creator<f> {
    e() {
    }

    public f createFromParcel(Parcel var1) {
        return new f(var1);
    }

    public f[] newArray(int var1) {
        return new f[var1];
    }
}
