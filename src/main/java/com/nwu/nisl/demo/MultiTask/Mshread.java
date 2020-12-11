package com.nwu.nisl.demo.MultiTask;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Create by yueyue on 2020/12/3
 */
@Service
public class Mshread {

    // 指定使用beanname为doSomethingExecutor的线程池
    @Async("asyc")
    public String doSomething(String message) {
        System.out.println("do something, message={}" + message);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("do something error: " + message);
        }
        return message;
    }

    public String something() {
        int count = 10;
        for (int i = 0; i < count; i++) {
            doSomething("index = " + i);
        }

        return "success";
    }


}
