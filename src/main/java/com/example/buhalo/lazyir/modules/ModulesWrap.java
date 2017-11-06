package com.example.buhalo.lazyir.modules;

/**
 * Created by buhalo on 06.11.17.
 */

class ModulesWrap {
    private String moduleName;
    private boolean status;
    private String dvId;


    public ModulesWrap(String moduleName, boolean status, String dvId) {
        this.moduleName = moduleName;
        this.status = status;
        this.dvId = dvId;
    }

    public String getModuleName() {

        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDvId() {
        return dvId;
    }

    public void setDvId(String dvId) {
        this.dvId = dvId;
    }
}
