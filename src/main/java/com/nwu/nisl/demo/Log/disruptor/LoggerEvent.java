package com.nwu.nisl.demo.Log.disruptor;

import com.nwu.nisl.demo.Log.LoggerMessage;

/**
 *Author:lp on 2019/12/11 17:31
 *Param:
 *return:
 *Description:进程日志消息
*/
public class LoggerEvent {
    private LoggerMessage loggerMessage;

    public LoggerMessage getLoggerMessage() {
        return loggerMessage;
    }

    public void setLoggerMessage(LoggerMessage loggerMessage) {
        this.loggerMessage = loggerMessage;
    }
}
