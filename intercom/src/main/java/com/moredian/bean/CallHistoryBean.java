package com.moredian.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/24
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CallHistoryBean implements Parcelable {
    //判断是拨打还是接听,1拨打，2接听
    private int callType;
    //判断是语音拨打还是视频拨打
    private int callMode;
    private String fromName;
    private String fromNum;
    private String toName;
    private String toNum;
    private int callDeviceType;
    //通话开始时间
    private long callStartTime;
    //通话结束时间
    private long callEndTime;
    //接听状态
    private int callStatus;
    //通话时长，单位秒
    private int callDuringTime;
    private long roomConstructId;

    public CallHistoryBean() {
    }

    protected CallHistoryBean(Parcel in) {
        callType = in.readInt();
        callMode = in.readInt();
        fromName = in.readString();
        fromNum = in.readString();
        toName = in.readString();
        toNum = in.readString();
        callDeviceType = in.readInt();
        callStartTime = in.readLong();
        callEndTime = in.readLong();
        callStatus = in.readInt();
        callDuringTime = in.readInt();
        roomConstructId = in.readLong();
    }

    public static final Creator<CallHistoryBean> CREATOR = new Creator<CallHistoryBean>() {
        @Override
        public CallHistoryBean createFromParcel(Parcel in) {
            return new CallHistoryBean(in);
        }

        @Override
        public CallHistoryBean[] newArray(int size) {
            return new CallHistoryBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(callType);
        dest.writeInt(callMode);
        dest.writeString(fromName);
        dest.writeString(fromNum);
        dest.writeString(toName);
        dest.writeString(toNum);
        dest.writeInt(callDeviceType);
        dest.writeLong(callStartTime);
        dest.writeLong(callEndTime);
        dest.writeInt(callStatus);
        dest.writeInt(callDuringTime);
        dest.writeLong(roomConstructId);
    }

    public void readFromParcel(Parcel parcel) {
        callType = parcel.readInt();
        callMode = parcel.readInt();
        fromName=parcel.readString();
        fromNum=parcel.readString();
        toName=parcel.readString();
        toNum=parcel.readString();
        callDeviceType=parcel.readInt();
        callStartTime = parcel.readLong();
        callEndTime = parcel.readLong();
        callStatus=parcel.readInt();
        callDuringTime = parcel.readInt();
        roomConstructId = parcel.readLong();
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromNum() {
        return fromNum;
    }

    public void setFromNum(String fromNum) {
        this.fromNum = fromNum;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getToNum() {
        return toNum;
    }

    public void setToNum(String toNum) {
        this.toNum = toNum;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public int getCallDeviceType() {
        return callDeviceType;
    }

    public void setCallDeviceType(int callDeviceType) {
        this.callDeviceType = callDeviceType;
    }

    public long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(long callStartTime) {
        this.callStartTime = callStartTime;
    }

    public int getCallDuringTime() {
        return callDuringTime;
    }

    public void setCallDuringTime(int callDuringTime) {
        this.callDuringTime = callDuringTime;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(long callEndTime) {
        this.callEndTime = callEndTime;
    }

    public int getCallMode() {
        return callMode;
    }

    public void setCallMode(int callMode) {
        this.callMode = callMode;
    }

    public long getRoomConstructId() {
        return roomConstructId;
    }

    public void setRoomConstructId(long roomConstructId) {
        this.roomConstructId = roomConstructId;
    }
}
