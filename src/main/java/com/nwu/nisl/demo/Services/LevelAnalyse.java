package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.DiffNode;
import com.nwu.nisl.demo.Component.ParseDiff;
import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import org.graalvm.compiler.nodes.calc.ObjectEqualsNode;
import org.python.antlr.ast.Str;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Create by lp on 2019/11/15
 */
public class LevelAnalyse {
    /**
     * @Author:lp on 2019/11/15 21:24
     * @Param: level diff
     * @return:
     * @Description:根据变化的部分分析周围的关联变化的地方
     */
    private DiffNode diffNode;
    private ParseDiff parseDiff;
    private Map<Integer, Collection<Object>> levelResults = new HashMap<>();

    @Autowired
    public LevelAnalyse(DiffNode diffNode, ParseDiff parseDiff) {
        this.diffNode = diffNode;
        this.parseDiff = parseDiff;
    }

    public void initInstance(int level) {
        List<Object> diffList = diffNode.diffToList();
        Map<String, Object> diffMap = new HashMap<>();
        diffMap.put("addDiff", diffNode.getAddDiff());
        diffMap.put("deleteDiff", diffNode.getDeletedDiff());
        diffMap.put("normalDiff", diffNode.getNormalDiff());
        //得到不同变化类型的文件函数节点，供我们进行查找
        Map<String, List<Object>> leveltmp = handle(level, diffMap);


    }

    public Map<String, List<Object>> handle(int level, Map<String, Object> diffMap) {
        //list[0]:Collection<File>
        //List[1]:Collection<Method>
        Map<String, List<Object>> diffCollections = new HashMap<>();
        for (String diffType : diffMap.keySet()) {
            if (diffType == "addDiff") {
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("addDiff"));
                //全部是adddiff(Method、file)，通过这些节点找到附近相邻的节点。
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("adddiff", fileMethodCollections);


            } else if (diffType == "deleteDiff") {
                //全部是deleteDiff(Method、file)
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("deleteDiff"));
                //全部是adddiff(Method、file)
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("deleteDiff", fileMethodCollections);

            } else {
                //全部是modify(Method、file)
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("modify"));
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("modify", fileMethodCollections);

            }
        }

        return null;
    }

    //根据层次寻找改变函数被其他节点调用的地方
    public List<Object> getadjcent(int level, List<Object> fileMethodCollections) {
        Collection<File> connectFiles = new ArrayList<>();
        Collection<Method> connectMethods = new ArrayList<>();
        // fileMethodCollections:diffType-> <fileDiff,methodDiff>
        for (int i = 0; i < level; i++) {
            //查找上层关系
            for (File file : (Collection<File>) fileMethodCollections.get(0)) {
                //

            }
            for (Method method : (Collection<Method>) fileMethodCollections.get(1)) {

            }
        }
        return null;
    }

    public Map<String, Object> tomap(String diffType, Object object) {
        Map<String, Object> map = new HashMap<>();
        map.put(diffType, object);
        return map;
    }

    public Map<Integer, Collection<Object>> getLevelResults() {
        return levelResults;
    }
}
