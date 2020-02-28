package com.dds.webrtclib.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/1/9.
 * android_shuai@163.com
 */
public class MyIceServer implements Parcelable {
    public final String uri;
    public final String username;
    public final String password;

    public MyIceServer(String uri) {
        this(uri, "", "");
    }

    public MyIceServer(String uri, String username, String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }


    protected MyIceServer(Parcel in) {
        uri = in.readString();
        username = in.readString();
        password = in.readString();
    }

    public static final Creator<MyIceServer> CREATOR = new Creator<MyIceServer>() {
        @Override
        public MyIceServer createFromParcel(Parcel in) {
            return new MyIceServer(in);
        }

        @Override
        public MyIceServer[] newArray(int size) {
            return new MyIceServer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uri);
        dest.writeString(username);
        dest.writeString(password);
    }
}