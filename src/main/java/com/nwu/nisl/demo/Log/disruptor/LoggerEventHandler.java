package com.nwu.nisl.demo.Log.disruptor;

import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Create by lp on 2019/12/11
 */
@Component
public class LoggerEventHandler implements EventHandler<LoggerEvent> {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onEvent(LoggerEvent loggerEvent, long l, boolean b) throws Exception {
        simpMessagingTemplate.convertAndSend("/log/pulllog",loggerEvent.getLoggerMessage());

    }
}
