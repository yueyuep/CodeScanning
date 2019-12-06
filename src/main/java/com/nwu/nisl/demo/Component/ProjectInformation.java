package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kangaroo
 */
@Component
public class ProjectInformation {
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private DiffNode diffNode;

    private String oldVersion;
    private String newVersion;

    private int fileNumber;
    private int addFileNumber;
    private int deleteFileNumber;

    private List<String> normalDiff = new ArrayList<>();
    private List<String> addDiff = new ArrayList<>();
    private List<String> deleteDiff = new ArrayList<>();
    private List<String> connectDiff = new ArrayList<>();

    public ProjectInformation() {

    }

    public void setAttribute(String oldVersion, String newVersion) {
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }

    public void addAttribute(Map<String, Object> res) {
        res.put("version", newVersion);
        res.put("fileNumber", fileNumber);
        res.put("addFileNumber", addFileNumber);
        res.put("deleteFileNumber", deleteFileNumber);
        res.put("normalDiff", normalDiff);
        res.put("addDiff", addDiff);
        res.put("deleteDiff", deleteDiff);
        res.put("connectDiff", connectDiff);
    }

    public void clean() {
        fileNumber = 0;
        addFileNumber = 0;
        deleteFileNumber = 0;
        normalDiff.clear();
        addDiff.clear();
        deleteDiff.clear();
        connectDiff.clear();
    }

    public Map<String, Object> getProjectInformation() {
        clean();
        Map<String, Object> res = new HashMap<>();
        fileNumber = fileRepository.getFileNumber(newVersion);

        if (!oldVersion.equals(newVersion)) {
//            diffNode.setPath(diffPath);
            // Map<type, Map<String, Map<String, List<String>>>>
            // 节点类型，版本号，file/method，对应diff中文本内容
            // type -> NodeType.ADD_NODE, NodeType.DELETE_NODE, NodeType.MODIFY_NODE
            Map<String, Object> map = diffNode.parseDiff();

            if (!map.isEmpty()) {
                /**
                 *Author:lp on 2019/12/2 21:15
                 *Param: []
                 *return: java.util.Map<java.lang.String,java.lang.Object>
                 *Description:修改空指针异常
                 */
                if (((Map<String, Map<String, List<String>>>) map.get(NodeType.ADD_NODE)).containsKey(newVersion)){
                    if (((Map<String, Map<String, List<String>>>) map.get(NodeType.ADD_NODE)).get(newVersion).containsKey(NodeType.FILE)) {
                        addFileNumber = ((Map<String, Map<String, List<String>>>) map.get(NodeType.ADD_NODE)).get(newVersion).get(NodeType.FILE).size();
                    }
                }
                if (((Map<String, Map<String, List<String>>>) map.get(NodeType.DELETE_NODE)).containsKey(oldVersion)) {
                    if (((Map<String, Map<String, List<String>>>) map.get(NodeType.DELETE_NODE)).get(oldVersion).containsKey(NodeType.FILE)) {
                        deleteFileNumber = ((Map<String, Map<String, List<String>>>) map.get(NodeType.DELETE_NODE)).get(oldVersion).get(NodeType.FILE).size();
                    }
                }

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    List<String> temp = new ArrayList<>();
                    for (Map<String, List<String>> stringListMap : ((Map<String, Map<String, List<String>>>) entry.getValue()).values()) {
                        for (List<String> str : stringListMap.values()) {
                            temp.addAll(str);
                        }
                    }
                    if (entry.getKey().equals(NodeType.ADD_NODE)) {
                        addDiff.addAll(temp);
                    }
                    if (entry.getKey().equals(NodeType.DELETE_NODE)) {
                        deleteDiff.addAll(temp);
                    }
                    if (entry.getKey().equals(NodeType.MODIFY_NODE)) {
                        normalDiff.addAll(temp);
                    }
                }
            }

        }
        addAttribute(res);
        return res;
    }

}
