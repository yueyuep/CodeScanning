package com.nwu.nisl.demo.Component;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by lp on 2019/12/13
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ScanGraphTest {
    @Autowired
    private ScanGraph scanGraph;

    @Test
    public void initInstance() {
        int level = 2;
        Map<String, Map<String, List<Object>>> result = scanGraph.initInstance(level);
        System.out.println("跨层分析的测试结果");
    }
}