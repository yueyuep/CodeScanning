package com.nwu.nisl.demo.Services;


import com.nwu.nisl.demo.Component.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by lp on 2019/12/9
 */
@Service
public class StartProcessServices {
    Process process;

    @Autowired
    public StartProcessServices(Process process) {
        this.process = process;

    }

    public void startProcess() {
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
