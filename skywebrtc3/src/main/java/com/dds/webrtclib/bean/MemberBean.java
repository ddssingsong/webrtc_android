package com.dds.webrtclib.bean;

/**
 * 用户信息
 * Created by dds on 2019/5/3.
 * android_shuai@163.com
 */
public class MemberBean {
    private String id;
    private String name;
    private String avatar;


    public MemberBean(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MemberBean other = (MemberBean) obj;
        return this.getId().equals(other.getId());
    }
}
