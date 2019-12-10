package com.moredian.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/11/08
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class UserInfoBean implements Parcelable {
    private final long orgId;
    private final long roomConstructId;
    private final String mobile;
    private final String fromName;
    private final String fromNum;
    private final String toName;
    private final String toNum;
    private final int VideoFlag;
    private final int callDeviceType;

    public UserInfoBean(long orgId, long roomConstructId,String mobile, String fromName, String fromNum, String toName, String toNum, int videoFlag,int callDeviceType) {
        this.orgId = orgId;
        this.roomConstructId = roomConstructId;
        this.mobile = mobile;
        this.fromName = fromName;
        this.fromNum = fromNum;
        this.toName = toName;
        this.toNum = toNum;
        VideoFlag = videoFlag;
        this.callDeviceType = callDeviceType;
    }


    protected UserInfoBean(Parcel in) {
        orgId = in.readLong();
        roomConstructId = in.readLong();
        mobile = in.readString();
        fromName = in.readString();
        fromNum = in.readString();
        toName = in.readString();
        toNum = in.readString();
        VideoFlag = in.readInt();
        callDeviceType = in.readInt();
    }

    public static final Creator<UserInfoBean> CREATOR = new Creator<UserInfoBean>() {
        @Override
        public UserInfoBean createFromParcel(Parcel in) {
            return new UserInfoBean(in);
        }

        @Override
        public UserInfoBean[] newArray(int size) {
            return new UserInfoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(orgId);
        dest.writeLong(roomConstructId);
        dest.writeString(mobile);
        dest.writeString(fromName);
        dest.writeString(fromNum);
        dest.writeString(toName);
        dest.writeString(toNum);
        dest.writeInt(VideoFlag);
        dest.writeInt(callDeviceType);
    }

    public long getOrgId() {
        return orgId;
    }

    public String getToName() {
        return toName;
    }

    public String getToNum() {
        return toNum;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromNum() {
        return fromNum;
    }

    public int getVideoFlag() {
        return VideoFlag;
    }

    public String getMobile() {
        return mobile;
    }

    public int getCallDeviceType() {
        return callDeviceType;
    }

    public long getRoomConstructId() {
        return roomConstructId;
    }
}
