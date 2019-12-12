package com.nwu.nisl.demo.Log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.sql.Date;
import java.text.DateFormat;

/**
 * Create by lp on 2019/12/11
 *
 * 过滤特定类型的日志
 */
public class ProcessLogFilter extends Filter<ILoggingEvent> {
    @Override
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        LoggerMessage loggerMessage = new LoggerMessage(
                iLoggingEvent.getMessage()
                , DateFormat.getDateTimeInstance().format(new Date(iLoggingEvent.getTimeStamp())),
                iLoggingEvent.getThreadName(),
                iLoggingEvent.getLoggerName(),
                iLoggingEvent.getLevel().levelStr
        );
        LoggerDisruptouQueue.publishEvent(loggerMessage);
        //立即处理当前日志文件，跳过后面所有的日志文件
        return FilterReply.ACCEPT;

    }
}
