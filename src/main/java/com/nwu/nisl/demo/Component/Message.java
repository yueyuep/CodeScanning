package com.nwu.nisl.demo.Component;

/**
 * Create by yueyue on 2020/12/11
 */
public class Message {
    private int status;
    private String oldversion;
    private String newversion;

    public Message(int status, String oldversion, String newversion) {
        this.status = status;
        this.oldversion = oldversion;
        this.newversion = newversion;
    }

    public int getStatus() {
        return status;
    }

    public String getOldversion() {
        return oldversion;
    }

    public String getNewversion() {
        return newversion;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setOldversion(String oldversion) {
        this.oldversion = oldversion;
    }

    public void setNewversion(String newversion) {
        this.newversion = newversion;
    }
}
