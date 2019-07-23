package cn.wildfirechat.avenginekit.a;

import android.os.Parcel;
import android.os.Parcelable.Creator;
/**
 * Created by dds on 2019/7/23.
 * android_shuai@163.com
 */
public class a implements Creator<b> {
    a() {
    }

    public b createFromParcel(Parcel var1) {
        return new b(var1);
    }

    public b[] newArray(int var1) {
        return new b[var1];
    }
}
