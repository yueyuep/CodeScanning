package com.nwu.nisl.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

/**
 * Author:lp on 2019/12/9 16:30
 * Param:
 * return:
 * Description:修改bean的扫描范围为com.nwu.nisl包下面
 */
@SpringBootApplication
@EnableNeo4jRepositories("com.nwu.nisl.demo.Repository")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
