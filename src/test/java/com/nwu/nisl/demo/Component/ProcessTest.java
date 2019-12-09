package com.nwu.nisl.demo.Component;

import com.nwu.nisl.neo4j.Json2Csv;
import com.nwu.nisl.parse.neo4j.ExtractJavaFile;
import com.nwu.nisl.parse.neo4j.GraphParse;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
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
        ProcessTest process = new ProcessTest();


        System.out.println("Parsing java file......");
        process.first();
        System.out.println("The first stage is completed!");

        System.out.println("Running json to csv......");
        process.second();
        System.out.println("The second stage is completed!");

        System.out.println("Analyzing the json file to generate the corresponding diff file......");
        process.third();
        System.out.println("The third stage is completed!");
    }

    public void first(){
        int i = 2;
        while (i-- > 0) {
        System.out.println("Please enter the version number: ");
//            Scanner scanner = new Scanner(System.in);
        String version = "0.9.22";//scanner.next();
        if (i == 0) {
            version = "0.9.23";
        }
        System.out.println("Parsing:");
        String sourcePath = data + File.separator + version;
        String targetPath = json + File.separator + version ;
        File dir = new File(sourcePath);
        ExtractJavaFile javaFile = new ExtractJavaFile(dir);
        javaFile.getFileList(dir);
        File[] fileList = javaFile.getFile();
        GraphParse.ProcessMultiFile(fileList, targetPath);
        System.out.println("End Parsing\n");
    }
}

    public void second() throws IOException {
        System.out.println("Please enter the version number: ");
//        Scanner scanner = new Scanner(System.in);
        String version = "0.9.22";//scanner.next();

        String sourcePath = json + File.separator + version;
        String targetPath = csv;
        Json2Csv json2Csv = new Json2Csv(sourcePath, targetPath);
        json2Csv.clear();
        json2Csv.generateCsv();

        System.out.println("Please enter the version number: ");
//        Scanner scanner1 = new Scanner(System.in);
        String version1 = "0.9.23";//scanner1.next();
        json2Csv.setSourcePath(json + File.separator + version1);
        json2Csv.generateCsv();
    }

    public void third() {

    }
}