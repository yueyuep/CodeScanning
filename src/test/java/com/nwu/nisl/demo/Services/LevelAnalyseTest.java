package com.nwu.nisl.demo.Services;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by lp on 2019/11/19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class LevelAnalyseTest {
    @Autowired
    private LevelAnalyse levelAnalyse;

    @Test
    void initInstance() {
        levelAnalyse.initInstance(1);
    }

    @Test
    void handle() {
    }

    @Test
    void getadjcent() {
    }

    @Test
    void tomap() {
    }

    @Test
    void getLevelResults() {
    }
}