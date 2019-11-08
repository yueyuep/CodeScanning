package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional()
public class FileRepositoryTest {
    @Autowired
    FileRepository fileRepository;
    @Test
    void findFiles() {
        Collection<File> files=fileRepository.findFiles(50);
        System.out.println("Done!");
    }
}