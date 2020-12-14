package com.nwu.nisl.demo.Component;

import com.nwu.nisl.neo4j.Json2Csv;
import com.nwu.nisl.parse.neo4j.ExtractJavaFile;
import com.nwu.nisl.parse.neo4j.GraphParse;
import org.python.antlr.ast.Str;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: Process
 * @Author: Kangaroo
 * @Description: 自动化执行代码解析，diff、csv的生成
 * @Date: 2019/12/9 9:00
 * @Version: 1.0
 */
@Component
public class Process {
    /* 静态变量赋值，需使用set()方法（加 @Value 注解），且类上加入 @Component 注解
     * 直接给静态变量添加 @Value 注解是无效的， 都为null */

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

    @Autowired
    private BatchSaveNeo4j batchSaveNeo4j;
    private Logger logger = LoggerFactory.getLogger(Process.class);

    @Autowired
    private Utils utils;

    public Process() {

    }

    public void start() throws IOException {
        System.out.println("Parsing java file (Including two versions of project)......");
        //first();
        System.out.println("The first stage is completed!\n");

        System.out.println("Running json to csv......");
        //second();
        System.out.println("The second stage is completed!\n");

        System.out.println("Saving the csv to Neo4j database");
        //third();
        System.out.println("The third stage is completed!");
    }

    public void first() {
        int i = 2;
        while (i-- > 0) {
            System.out.println("Please enter the version number: ");
            Scanner scanner = new Scanner(System.in);
            String version = scanner.next();

            System.out.println("Parsing:");

            String sourcePath = data + File.separator + version;
            String targetPath = json + File.separator + version;
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
        Scanner scanner = new Scanner(System.in);
        String version = scanner.next();

        String sourcePath = json + File.separator + version;
        String targetPath = csv;
        Json2Csv json2Csv = new Json2Csv(sourcePath, targetPath);
        json2Csv.clear();
        json2Csv.generateCsv();

        System.out.println("Please enter the version number: ");
        Scanner scanner1 = new Scanner(System.in);
        String version1 = scanner1.next();
        json2Csv.setSourcePath(json + File.separator + version1);
        json2Csv.generateCsv();
    }


    public void third() throws Exception {
        //启动服务器

        batchSaveNeo4j.start();

    }


    /**
     * Author:lp on 2019/12/10 18:53
     * Param:
     * return:
     * Description:带参重载上述函数
     */
    public void first(String oldversion, String newversion) throws Exception {
        String[] parame = new String[]{oldversion, newversion};
        for (String version : parame) {
            logger.info("===============Start Parsing:" + version + "===============");
            String sourcePath = data + File.separator + version;
            String targetPath = json + File.separator + version;
            logger.info("sourcepath" + sourcePath);
            logger.info("targetpath" + targetPath);
            File dir = new File(sourcePath);
            ExtractJavaFile javaFile = new ExtractJavaFile(dir);
            javaFile.getFileList(dir);
            File[] fileList = javaFile.getFile();
            GraphParse.ProcessMultiFile(fileList, targetPath);
            logger.info("===============End Parsing:" + version + "===============");
        }

    }

    /**
     * Author:lp on 2019/12/13 17:03
     * Param:
     * return:
     * Description:确保csv文件存在
     */
    public void second(String oldversion, String newversion) throws IOException {

        logger.info("===============Running" + oldversion + " json to csv......===============");
        String sourcePath = json + File.separator + oldversion;
        String targetPath = csv;
        Json2Csv json2Csv = new Json2Csv(sourcePath, targetPath);
        json2Csv.clear();
        json2Csv.generateCsv();
        logger.info("===============End " + oldversion + " save2csv！......===============");
        logger.info("===============Running" + newversion + " json to csv......===============");
        sourcePath = json + File.separator + newversion;
        targetPath = csv;
        json2Csv = new Json2Csv(sourcePath, targetPath);
        json2Csv.generateCsv();
        logger.info("===============End " + newversion + " save2csv！......===============");

    }


}
