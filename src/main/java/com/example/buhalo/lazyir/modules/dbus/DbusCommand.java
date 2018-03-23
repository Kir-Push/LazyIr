package com.example.buhalo.lazyir.modules.dbus;

/**
 * Created by buhalo on 05.10.17.
 */

class DbusCommand {
    private int code;
    private String command;
    private int arg;
    private String sArg;
    private String whom;


    public DbusCommand(int code, String command, int arg, String sArg, String whom) {
        this.code = code;
        this.command = command;
        this.arg = arg;
        this.sArg = sArg;
        this.whom = whom;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }

    public String getsArg() {
        return sArg;
    }

    public void setsArg(String sArg) {
        this.sArg = sArg;
    }

    public String getWhom() {
        return whom;
    }

    public void setWhom(String whom) {
        this.whom = whom;
    }

    @Override
    public String toString() {
        return "DbusCommand{" +
                "code=" + code +
                ", command='" + command + '\'' +
                ", arg=" + arg +
                ", sArg='" + sArg + '\'' +
                ", whom='" + whom + '\'' +
                '}';
    }
}
