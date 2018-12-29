package com.example.buhalo.lazyir.modules.clipboard;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class ClipboardDB implements Comparable<ClipboardDB>, Parcelable {
    private String text;
    private String owner;
    private int id;

    public ClipboardDB(String text,String owner, int id) {
        this.text = text;
        this.owner = owner;
        this.id = id;
    }

    @Override
    public int compareTo(ClipboardDB o) {
        return Integer.compare(getId(),o.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(owner);
        dest.writeInt(id);
    }


    public static final Parcelable.Creator<ClipboardDB> CREATOR = new Parcelable.Creator<ClipboardDB>() {

        public ClipboardDB createFromParcel(Parcel in) {
            return new ClipboardDB(in);
        }

        public ClipboardDB[] newArray(int size) {
            return new ClipboardDB[size];
        }
    };

    private ClipboardDB(Parcel parcel) {
        this.text = parcel.readString();
        this.owner = parcel.readString();
        this.id = parcel.readInt();
    }
}
