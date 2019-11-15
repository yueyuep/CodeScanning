package com.nwu.nisl.demo.Component;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional()
public class DiffNodeTest {
    @Autowired
    DiffNode diffNodeTest;

    @Test
    public void test() {
        // List<String> a = diffNodeTest.ToList();
        System.out.println("Done");
    }
}