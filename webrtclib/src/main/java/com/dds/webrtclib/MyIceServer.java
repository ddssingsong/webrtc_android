package com.dds.webrtclib;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dds on 2019/1/9.
 * android_shuai@163.com
 */
public class MyIceServer implements Parcelable {
    public final String urls;
    public final String username;
    public final String credential;

    public MyIceServer(String uri) {
        this(uri, "", "");
    }

    public MyIceServer(String uri, String username, String password) {
        this.urls = uri;
        this.username = username;
        this.credential = password;
    }


    protected MyIceServer(Parcel in) {
        urls = in.readString();
        username = in.readString();
        credential = in.readString();
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
        dest.writeString(urls);
        dest.writeString(username);
        dest.writeString(credential);
    }
}