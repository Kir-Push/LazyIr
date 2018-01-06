package com.example.buhalo.lazyir.modules.shareManager;

import java.util.List;

/**
 * Created by buhalo on 03.12.17.
 */

public class PathWrapper {
    private List<String> paths;

    public PathWrapper() {
    }

    public PathWrapper(List<String> paths) {
        this.paths = paths;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String>  paths) {
        this.paths = paths;
    }
}
