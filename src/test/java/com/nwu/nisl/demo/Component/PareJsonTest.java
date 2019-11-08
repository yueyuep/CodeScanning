package com.nwu.nisl.demo.Component;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PareJsonTest {
    @Autowired
    private PareJson pareJson;

    @Test
    public void test1() {
        List<HashMap<String, Object>> hashMapList = pareJson.Pare();
        System.out.println("done!");
    }


}