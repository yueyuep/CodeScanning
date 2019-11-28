package com.nwu.nisl.demo.Component;

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
    private List<String> diff = new ArrayList<>();

    private Map<String, Map<String, List<String>>> normalDiff = new HashMap<>();
    private Map<String, Map<String, List<String>>> deletedDiff = new HashMap<>();
    private Map<String, Map<String, List<String>>> addDiff = new HashMap<>();

    @Value("${com.nwu.nisl.demo.diffResult}")
    private String path;

    public DiffNode() {
    }

    public DiffNode(String path) {
        this.path = path;
    }

    private void clear() {
        normalDiff.clear();
        deletedDiff.clear();
        addDiff.clear();
    }

    /**
     * @return java.util.List<java.lang.Object>
     * @Author Kangaroo
     * @Description 解析 diff 文件，并将不同类型的变化信息（版本号，文件/文件函数名）保存到对应的全局变量中
     * @Date 2019/11/16 9:59
     * @Param []
     **/
    public Map<String, Object> parseDiff() {
        Map<String, Object> map = new HashMap<>();
        clear();

        try {
            File file = new File(this.path);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String text;
            while ((text = bufferedReader.readLine()) != null) {
                String[] res = text.split("&");
                if (res[0].equals("normaldiff")) {
                    handleDeleteAndAdd(normalDiff, res);
                } else if (res[0].equals("deletediff")) {
                    handleDeleteAndAdd(deletedDiff, res);
                } else if (res[0].equals("adddiff")) {
                    handleDeleteAndAdd(addDiff, res);
                }
            }
            map.put(NodeType.ADD_NODE, addDiff);
            map.put(NodeType.MODIFY_NODE, normalDiff);
            map.put(NodeType.DELETE_NODE, deletedDiff);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * @return void
     * @Author Kangaroo
     * @Description 解析diff文件中 adddiff、deleteddiff行，并将解析结果保存到对应的全局变量中
     * @Date 2019/11/16 10:02
     * @Param [diff, res]
     **/
    private void handleDeleteAndAdd(Map<String, Map<String, List<String>>> diff,
                                    String[] res) {
        String version = res[res.length - 1];
        if (!diff.keySet().contains(version)) {
            diff.put(version, new HashMap<>());
        }
        if (res.length == 3) {
            if (!diff.get(version).keySet().contains(NodeType.FILE)) {
                diff.get(version).put(NodeType.FILE, new ArrayList<>());
            }
            diff.get(version).get(NodeType.FILE).add(res[1]);
        } else if (res.length == 4) {
            if (!diff.get(version).keySet().contains(NodeType.METHOD)) {
                diff.get(version).put(NodeType.METHOD, new ArrayList<>());
            }
            diff.get(version).get(NodeType.METHOD).add(res[1].concat("-").concat(res[2]));
        }
    }

    public List<String> getDiff() {
        return diff;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, Map<String, List<String>>> getNormalDiff() {
        return this.normalDiff;
    }

    public Map<String, Map<String, List<String>>> getDeletedDiff() {
        return this.deletedDiff;
    }

    public Map<String, Map<String, List<String>>> getAddDiff() {
        return this.addDiff;
    }

}
