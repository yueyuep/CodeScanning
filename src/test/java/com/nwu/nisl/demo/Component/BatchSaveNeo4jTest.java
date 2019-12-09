package com.nwu.nisl.demo.Component;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by lp on 2019/12/9
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class BatchSaveNeo4jTest {
    @Autowired
    private BatchSaveNeo4j batchSaveNeo4j;

    @Test
    void start() {
        batchSaveNeo4j.start();
    }
}