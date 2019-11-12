package com.nwu.nisl.demo.Services;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class NodeServicesTest {
    @Autowired
    private NodeServices nodeServices;
    @Test
    public void findAllByFileMethodName() {
        String fileMethodName="android-demo/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.java-useAppContext-ExampleInstrumentedTest--15";
        // 去除函数名的尾部数字节点
        String end=fileMethodName.split("-")[fileMethodName.split("-").length-1];
        fileMethodName=fileMethodName.substring(0,fileMethodName.length()-end.length());
        String version="0.9.22";
        Map<String,Object> results= nodeServices.findAllByFileMethodName(fileMethodName,version);
        System.out.println("Done!");

    }
}