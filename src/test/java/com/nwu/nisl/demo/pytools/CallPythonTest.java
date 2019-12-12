package com.nwu.nisl.demo.pytools;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by lp on 2019/12/6
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class CallPythonTest {
    @Autowired
    private CallPython callPython;

    @Test
    void main() {
        String oldversion = "0.9.22";
        String newversion = "0.9.23";
        callPython.execute(oldversion, newversion);
    }
}