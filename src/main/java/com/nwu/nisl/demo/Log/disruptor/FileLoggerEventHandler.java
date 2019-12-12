package com.nwu.nisl.demo.Log.disruptor;

import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Create by lp on 2019/12/11
 */
@Component
public class FileLoggerEventHandler implements EventHandler<FileLoggerEvent> {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void onEvent(FileLoggerEvent fileLoggerEvent, long l, boolean b) throws Exception {
        //消费者的消费行为,把消息发到客户端
        simpMessagingTemplate.convertAndSend("/log/pullFileLogger", fileLoggerEvent.getLog());


    }
}
