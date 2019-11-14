package com.nwu.nisl.demo.Component;

import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DiffNode {
    private List<String> diff=new ArrayList<>();

    private Map<String, List<String>> normalDiff = new HashMap<>();
    private Map<String, Map<String, List<String>>> deletedDiff = new HashMap<>();
    private Map<String, Map<String, List<String>>> addDiff = new HashMap<>();

    @Value("${com.nwu.nisl.demo.url}")
    private String path;

    public DiffNode(){}

    public DiffNode(String path){
        this.path=path;
    }

    private void clear(){
        normalDiff.clear();
        deletedDiff.clear();
        addDiff.clear();
    }

    public List<Object> ToList() {
        List<Object> list = new ArrayList<>();

        clear();
        try {
            File file=new File(this.path);
            FileInputStream fileInputStream=new FileInputStream(file);
            InputStreamReader inputStreamReader=new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);

            String text;
            while ((text=bufferedReader.readLine())!=null){
                String[] res = text.split("&");
                if (res[0].equals("normaldiff")) {
                    if (!normalDiff.keySet().contains(res[2])){
                        normalDiff.put(res[2], new ArrayList<>());
                    }
                    normalDiff.get(res[2]).add(res[1]);
                } else if (res[1].equals("deletediff")) {
                    handleDeleteAndAdd(deletedDiff, res);
                } else if (res[1].equals("adddiff")) {
                    handleDeleteAndAdd(addDiff, res);
                }
            }
            list.add(normalDiff);
            list.add(deletedDiff);
            list.add(addDiff);
            return list;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    private void handleDeleteAndAdd(Map<String, Map<String, List<String>>> diff,
                                    String[] res){
        String version = res[res.length - 1];
        if (!diff.keySet().contains(version)){
            diff.put(version, new HashMap<>());
        }
        if (res.length == 3){
            if (!diff.get(version).keySet().contains("file")){
                diff.get(version).put("file", new ArrayList<>());
            }
            diff.get(version).get("file").add(res[1]);
        } else if (res.length == 4){
            if (!diff.get(version).keySet().contains("method")){
                diff.get(version).put("method", new ArrayList<>());
            }
            diff.get(version).get("method").add(res[1].concat("-").concat(res[2]));
        }
    }

    public List<String> getDiff() {
        return diff;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, List<String>> getNormalDiff() {
        return this.normalDiff;
    }

    public Map<String, Map<String, List<String>>> getDeletedDiff() {
        return this.deletedDiff;
    }

    public Map<String, Map<String, List<String>>> getAddDiff() {
        return this.addDiff;
    }

}
