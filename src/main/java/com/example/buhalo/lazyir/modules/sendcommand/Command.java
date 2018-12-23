package com.example.buhalo.lazyir.modules.sendcommand;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

@Data
public class Command implements Comparable<Command>, Parcelable {
    private String commandName;
    private String cmd;
    private int id;

    public Command(String commandName, String cmd, int id) {
        this.commandName = commandName;
        this.cmd = cmd;
        this.id = id;
    }

    @Override
    public int compareTo(Command o) {
        return Integer.compare(getId(),o.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(commandName);
        dest.writeString(cmd);
        dest.writeInt(id);
    }

    public static final Parcelable.Creator<Command> CREATOR = new Parcelable.Creator<Command>() {

        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        public Command[] newArray(int size) {
            return new Command[size];
        }
    };

    private Command(Parcel parcel) {
        this.commandName = parcel.readString();
        this.cmd = parcel.readString();
        this.id = parcel.readInt();
    }
}
