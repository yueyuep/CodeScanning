package com.nwu.nisl.demo.Services;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Create by lp on 2019/12/2
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class LevelAnalyseTest {
    private LevelAnalyse levelAnalyse;

    @Autowired
    public LevelAnalyseTest(LevelAnalyse levelAnalyse) {
        this.levelAnalyse = levelAnalyse;

    }

    @Test
    void getPartNodes() {
        String version = "0.9.23";
        int level = 2;
        Map<String, Object> testResult = levelAnalyse.getPartNodes(version, level);
        System.out.println("测试结果");
    }
}