package cn.wildfirechat.avenginekit.a;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class g implements Parcelable.Creator<h> {
    g() {
    }

    public h createFromParcel(Parcel var1) {
        return new h(var1);
    }

    public h[] newArray(int var1) {
        return new h[var1];
    }
}

