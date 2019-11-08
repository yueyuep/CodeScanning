package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.Method;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
@RunWith(SpringRunner.class)
@SpringBootTest
class MethodRepositoryTest {
@Autowired
private MethodRepository methodRepository;
    @Test
    void findMethod() {
        Collection<Method> methods=methodRepository.findMethod(1000);
        System.out.println("Done");
    }
}