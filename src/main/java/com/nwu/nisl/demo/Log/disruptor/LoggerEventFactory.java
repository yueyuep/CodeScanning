package com.nwu.nisl.demo.Log.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * Create by lp on 2019/12/11
 */
public class LoggerEventFactory implements EventFactory {
    @Override
    public Object newInstance() {
        return new LoggerEvent();
    }
}
