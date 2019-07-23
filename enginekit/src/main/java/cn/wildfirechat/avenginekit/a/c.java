package cn.wildfirechat.avenginekit.a;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class c implements Parcelable.Creator<d> {
    c() {
    }

    public d createFromParcel(Parcel var1) {
        return new d(var1);
    }

    public d[] newArray(int var1) {
        return new d[var1];
    }
}