package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.HasMethod;
import org.junit.Test;
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
    public void findFiles() {
        // a.size() = 213
        Collection<File> a = fileRepository.findFilesByVersion("0.9.22");

        // b.size() = 189 this is right
        Collection<File> b = fileRepository.findFilesWithMethodByVersion("0.9.22");
        int count = 0;
        for (File file: b){
            if (!file.getMethods().isEmpty()){
                count++;
            }
        }

        File c = fileRepository.findFileByFileNameAndVersion("demo/src/main/java/jsoniter_codegen/cfg1173796797/encoder/com/jsoniter/demo/User.java", "0.9.22");
        System.out.println("Done");
    }
}