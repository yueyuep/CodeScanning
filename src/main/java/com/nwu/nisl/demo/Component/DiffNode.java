package com.nwu.nisl.demo.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
@Component
public class DiffNode {
    private List<String> diff=new ArrayList<>();
    @Value("${com.nwu.nisl.demo.url}")
    private String path;
    public DiffNode(){}
    public DiffNode(String path){
        this.path=path;
    }
    public List<String>ToList() {
        try {
            File file=new File(this.path);
            FileInputStream fileInputStream=new FileInputStream(file);
            InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String text;
            while ((text=bufferedReader.readLine())!=null){
                this.diff.add(text);

            }
            return this.diff;
        }
        catch (Exception e){
            e.printStackTrace();

        }
        return this.diff;
    }

    public List<String> getDiff() {
        return diff;
    }
}
