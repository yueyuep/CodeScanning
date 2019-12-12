package com.nwu.nisl.demo.Component;

import com.nwu.nisl.neo4j.Json2Csv;
import com.nwu.nisl.parse.neo4j.ExtractJavaFile;
import com.nwu.nisl.parse.neo4j.GraphParse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: ProcessTest
 * @Author: Kangaroo
 * @Description:
 * @Date: 2019/12/9 11:15
 * @Version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
class ProcessTest {
    private static String data;
    private static String json;
    private static String csv;
    private static String diff;

    @Value("${com.nwu.nisl.data.source}")
    public void setData(String source) {
        data = source;
    }

    @Value("${com.nwu.nisl.data.json}")
    public void setJson(String json1) {
        json = json1;
    }

    @Value("${com.nwu.nisl.data.csv}")
    public void setCsv(String csv1) {
        csv = csv1;
    }

    @Value("${com.nwu.nisl.data.diff}")
    public void setDiff(String diff1) {
        diff = diff1;
    }

    @Test
    void main() throws IOException {
        Process process = new Process();

        System.out.println("Parsing java file......");
        //process.first();
        System.out.println("The first stage is completed!");

        System.out.println("Running json to csv......");
        //process.second();
        System.out.println("The second stage is completed!");

        System.out.println("Saving the csv to neo4j database!");
        //process.third();
        System.out.println("The third stage is completed!");
    }


}