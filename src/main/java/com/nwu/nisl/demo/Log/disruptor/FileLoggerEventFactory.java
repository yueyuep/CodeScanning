package com.nwu.nisl.demo.Log.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * Create by lp on 2019/12/11
 */
public class FileLoggerEventFactory implements EventFactory<FileLoggerEvent> {

    @Override
    public FileLoggerEvent newInstance() {
        return new FileLoggerEvent();
    }
}
