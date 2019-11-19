package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectInformation {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private DiffNode diffNode;

    private String oldVersion;
    private String newVersion;
    private String diffPath;

    private int fileNumber;
    private int addFileNumber;
    private int deleteFileNumber;

    private List<String> normalDiff = new ArrayList<>();
    private List<String> addDiff = new ArrayList<>();
    private List<String> deleteDiff = new ArrayList<>();
    private List<String> connectDiff = new ArrayList<>();

    public ProjectInformation() {

    }

    public void setAttribute(String oldVersion, String newVersion, String diffPath) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.diffPath = diffPath;
    }

    public void addAttribute(List<Object> res) {
        res.add(new HashMap<String, String>(){{put("version", newVersion);}});
        res.add(new HashMap<String, Integer>(){{put("fileNumber", fileNumber);}});
        res.add(new HashMap<String, Integer>(){{put("addFileNumber", addFileNumber);}});
        res.add(new HashMap<String, Integer>(){{put("deleteFileNumber", deleteFileNumber);}});
        res.add(new HashMap<String, List<String>>(){{put("normalDiff", normalDiff);}});
        res.add(new HashMap<String, List<String>>(){{put("addDiff", addDiff);}});
        res.add(new HashMap<String, List<String>>(){{put("deleteDiff", deleteDiff);}});
        res.add(new HashMap<String, List<String>>(){{put("connectDiff", connectDiff);}});
    }

    public List<Object> getProjectInformation() {
        List<Object> res = new ArrayList<>();
        fileNumber = fileRepository.getFileNumber(newVersion);

        if (!oldVersion.equals(newVersion)) {
            diffNode.setPath(diffPath);
            // list 包含的类型， 不同类型保存的顺序和后面代码逻辑关系十分密切
            // Map<String, Map<String, List<String>>> addDiff
            // Map<String, Map<String, List<String>>> normalDiff
            // Map<String, Map<String, List<String>>> deletedDiff
            List<Object> list = diffNode.diffToList();

            if (!list.isEmpty()) {
                addFileNumber = ((Map<String, Map<String, List<String>>>) list.get(0)).get(newVersion).get("file").size();
                deleteFileNumber = ((Map<String, Map<String, List<String>>>) list.get(2)).get(oldVersion).get("file").size();


                for (int i=0;i<list.size();i++) {
                    List<String> temp = new ArrayList<>();
                    for (Map<String, List<String>> map: ((Map<String, Map<String, List<String>>>) list.get(i)).values()) {
                        for (List<String> str: map.values()) {
                            temp.addAll(str);
                        }
                    }
                    if (i == 0) {
                        addDiff.addAll(temp);
                    } else if (i == 1) {
                        normalDiff.addAll(temp);
                    } else if (i == 2) {
                        deleteDiff.addAll(temp);
                    }
                }

            }
        }

        addAttribute(res);
        return res;
    }

}
