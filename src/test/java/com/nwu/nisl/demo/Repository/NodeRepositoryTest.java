package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional()
public class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepository;
    @Test
    public void graph() {
        // a.size() = 155230
//        Collection<Node> a = nodeRepository.findNodesByVersion("0.9.22");
//
        // b.size() = 106128
//        Collection<Node> b =nodeRepository.findNodesWithNodeByVersion("0.9.22");

//         c.size = 1827  实际=1825，
        Collection<Node> c = nodeRepository.findNodesWithCallByVersion("0.9.22"); // 7285
        int count = 0; // 1825
        for (Node node: c){
            if (!node.getNodeCallMethods().isEmpty()){
                count++;
            }
        }

//
//        Node d = nodeRepository.findNodeByFileMethodNameAndVersion("android-demo/src/androidTest/java/com/example/myapplication/ExampleInstrumentedTest.java-useAppContext-ExampleInstrumentedTest--19",
//                "0.9.22");
        System.out.println("Done");
    }

}