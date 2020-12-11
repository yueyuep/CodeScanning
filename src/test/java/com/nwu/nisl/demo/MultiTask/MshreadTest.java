package com.nwu.nisl.demo.MultiTask;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by yueyue on 2020/12/3
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class MshreadTest {
    @Autowired
    private Mshread mshread;

    @Test
    void something() {
        mshread.something();
    }
}