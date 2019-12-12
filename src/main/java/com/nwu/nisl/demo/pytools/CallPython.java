package com.nwu.nisl.demo.pytools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;

import java.io.IOException;

import java.io.InputStreamReader;

@Component
public class CallPython {
    @Value("${tools.python.url}")
    private String pythonurl;
    @Value("${tools.python.main}")
    private String mainstarturl;
    @Value("${tools.python.result}")
    private String resulturl;
    private Logger logger = LoggerFactory.getLogger(CallPython.class);

    public void execute(String oldversion, String newversion) {

        // TODO Auto-generated method stub

        Process proc;

        try {

            logger.info("=======caculating similarity cross version!=======");
            String[] args = new String[]{pythonurl, mainstarturl, resulturl, oldversion, newversion};

            proc = Runtime.getRuntime().exec(args);// 执行py文件

            //用输入输出流来截取结果

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line = null;

            while ((line = in.readLine()) != null) {

                logger.info(line);

            }

            in.close();

            proc.waitFor();

        } catch (IOException e) {

            e.printStackTrace();

        } catch (InterruptedException e) {

            e.printStackTrace();

        }

    }


}
