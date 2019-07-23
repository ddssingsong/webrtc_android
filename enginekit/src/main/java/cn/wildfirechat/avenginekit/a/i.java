package cn.wildfirechat.avenginekit.a;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class i implements Parcelable.Creator<j> {
    i() {
    }

    public j createFromParcel(Parcel var1) {
        return new j(var1);
    }

    public j[] newArray(int var1) {
        return new j[var1];
    }
}
