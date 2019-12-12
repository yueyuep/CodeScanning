package com.nwu.nisl.demo.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.*;

/**
 * Author:lp on 2019/12/9 9:54
 * Param:
 * return:
 * Description:删除默认数据库、自动存储、启动数据库
 */
@Component
public class BatchSaveNeo4j {
    private static Logger logger = LoggerFactory.getLogger(BatchSaveNeo4j.class);

    @Value("${neo4j.servicebat.location}")
    private String batPath;
    @Value("${neo4j.install.location}")
    private String neo4jlocation;
    @Value("${com.nwu.nisl.data.csv}")
    private String csvdata;

    public void start() {
        //
        String[] args = new String[]{batPath, neo4jlocation, csvdata};
        File batFile = new File(batPath);
        boolean batFileExist = batFile.exists();
        System.out.println("batFileExist:" + batFileExist);
        if (batFileExist) {
            callCmd(args);
        }
    }

    private static void callCmd(String[] locationCmd) {
        StringBuilder sb = new StringBuilder();
        try {
            java.lang.Process child = Runtime.getRuntime().exec(locationCmd);
            InputStream in = child.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "GBK"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line + "\n");
                logger.info(line + "\n");
            }
            in.close();
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            //logger.info("sb:" + sb.toString());
        } catch (IOException e) {
            System.out.println(e);
        }
    }


}
