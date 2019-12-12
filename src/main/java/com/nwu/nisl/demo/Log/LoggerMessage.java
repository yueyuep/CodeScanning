package com.nwu.nisl.demo.Log;

/**
 * Author:lp on 2019/12/11 17:27
 * Param:
 * return:
 * Description:定义log消息的格式
 */
public class LoggerMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;

    public LoggerMessage(String body, String timestamp, String threadName, String className, String level) {
        this.body = body;
        this.timestamp = timestamp;
        this.threadName = threadName;
        this.className = className;
        this.level = level;
    }

    public LoggerMessage() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
