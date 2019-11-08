package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.Node;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional()
public class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepository;
    @Test
    public void graph() {
        Collection<Node> nodes=nodeRepository.graph(300);
        System.out.println("Done:"+nodes.size());

    }

}