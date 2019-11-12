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
        // a.size() = 1488
//        Collection<Method> a = methodRepository.findMethodsByVersion("0.9.22");

        // b.size() = 1488
//        Collection<Method> b = methodRepository.findMethodsWithNodeByVersion("0.9.22");

        // 理论上 c.size() = 591
        // 但实际上为 700 多，存在 methodCallMethods 为空的情况
        Collection<Method> c = methodRepository.findMethodsWithCallByVersion("0.9.22");
        int count = 0;
        for (Method method: c){
            if (!method.getMethodCallMethods().isEmpty()){
                count++;
            }
        }
        

//        Collection<Method> e = methodRepository.test("0.9.22");


//        Method d = methodRepository.findMethodByFileMethodNameAndVersion("android-demo/src/main/java/jsoniter_codegen/cfg1173796797/decoder/com/example/myapplication/User.java-decode_-User-com.jsoniter.JsonIterator",
//                "0.9.22");
        System.out.println("Done");
    }
}