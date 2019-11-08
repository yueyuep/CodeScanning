package com.nwu.nisl.demo.Services;
import com.nwu.nisl.demo.Component.DiffNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
public class DiffNodeTest {
    @Autowired
    private DiffNode diffNode;
    @Test
    public void test1(){
        diffNode.ToList();
        if (diffNode ==null)
            System.out.println("注入失败");
        else
            System.out.println("注入成功，文件行数："+ diffNode.getDiff());
        }

}