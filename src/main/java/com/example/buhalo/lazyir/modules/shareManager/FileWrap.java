package com.example.buhalo.lazyir.modules.shareManager;

import java.io.File;

/**
 * Created by buhalo on 05.03.17.
 */

public class FileWrap {
    private boolean received; // 0 false 1 true;
    private boolean file; // 0 folder 1 file;
    private String path;

    public FileWrap(boolean received, boolean file, String path) {
        this.received = received;
        this.file = file;
        this.path = path;
    }

    public FileWrap(String path) {
        this.path = path;
        this.received = false;
        this.file = false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
