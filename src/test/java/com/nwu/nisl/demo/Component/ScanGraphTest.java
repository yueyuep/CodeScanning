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
 * Create by lp on 2019/11/29
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ScanGraphTest {
    private ScanGraph scanGraph;

    @Autowired
    public ScanGraphTest(ScanGraph scanGraph) {
        this.scanGraph = scanGraph;
    }


    @Test
    void initInstance() {
        //测试分析的层次
        int level = 2;
        Map<String, Map<String, List<Object>>> result = scanGraph.initInstance(level);
        System.out.println("跨层分析的测试结果");
    }

    @Test
    void levelNode() {

    }

    @Test
    void handle() {
    }

    @Test
    void getAdjacent() {
    }
}