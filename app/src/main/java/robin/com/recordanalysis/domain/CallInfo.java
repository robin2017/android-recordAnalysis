package robin.com.recordanalysis.domain;

import android.support.annotation.NonNull;

public class CallInfo implements Comparable<CallInfo> {
    public int cnt;
    public String number; // 号码
    public String name;
    public int duration;
    public long date;     // 日期
    public int type;      // 类型：来电、去电、未接


    public CallInfo(String number, long date, int type, int cnt,String name,int duration) {
        this.number = number;
        this.date = date;
        this.type = type;
        this.cnt = cnt;
        this.name = name;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "CallInfo{" +
                "number='" + number + '\'' +
                ", date=" + date +
                ", type=" + type +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getCnt() {
        return cnt;
    }

    public String getNumber() {
        return number;
    }

    public long getDate() {
        return date;
    }

    public int getType() {
        return type;
    }

    @Override
    public int compareTo(@NonNull CallInfo o) {


        return this.date>o.date?-1:1;
    }
}