package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.DiffNode;
import com.nwu.nisl.demo.Component.ParseDiff;
import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.python.modules._collections.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Create by lp on 2019/11/15
 */
@Service
public class LevelAnalyse {
    /**
     * @Author:lp on 2019/11/15 21:24
     * @Param: level diff
     * @return:
     * @Description:根据变化的部分分析周围的关联变化的地方
     */
    private DiffNode diffNode;
    private ParseDiff parseDiff;
    private FileRepository fileRepository;
    private MethodRepository methodRepository;

    @Autowired
    public LevelAnalyse(DiffNode diffNode, ParseDiff parseDiff, MethodRepository methodRepository, FileRepository fileRepository) {
        this.diffNode = diffNode;
        this.parseDiff = parseDiff;
        this.methodRepository = methodRepository;
        this.fileRepository = fileRepository;

    }

    public Map<String, List<Object>> initInstance(int level) {
        List<Object> diffList = diffNode.diffToList();
        Map<String, Object> diffMap = new HashMap<>();
        diffMap.put("addDiff", diffNode.getAddDiff());
        diffMap.put("deleteDiff", diffNode.getDeletedDiff());
        diffMap.put("normalDiff", diffNode.getNormalDiff());
        //得到不同变化类型的文件函数节点，供我们进行查找
        // diffMap: {"difftype":"",<version,flag,[method/file]>} 其中flag标记我们的变化部分是函数节点还是文件节点
        Map<String, List<Object>> leveltmp = handle(level, diffMap);

        // TODO @1:直接把数据传送到前台，在本地数据的基础上添加标识符,不需要再重新在读取数据
        // TODO @2:对这部分数据直接在在数据库中进行更改，type、change属性进行更改
        // TODO 需要比较两种的执行效率。
        return leveltmp;


    }

    public Map<String, List<Object>> handle(int level, Map<String, Object> diffMap) {
        //对不同的变化节点做不同的处理
        // String:diffType
        //list[0]:Collection<File>
        //List[1]:Collection<Method>
        Map<String, List<Object>> diffCollections = new HashMap<>();
        for (String diffType : diffMap.keySet()) {
            // TODO 预留后续3种不同类型变换的处理
            if (diffType == "addDiff") {
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("addDiff"));
                //全部是adddiff(Method、file)，通过这些节点找到附近相邻的节点。
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("adddiff", adjFileMethodCollections);


            } else if (diffType == "deleteDiff") {
                //全部是deleteDiff(Method、file)
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("deleteDiff"));
                //全部是adddiff(Method、file)
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("deleteDiff", adjFileMethodCollections);

            } else {
                //全部是modify(Method、file)
                List<Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get("normalDiff"));
                List<Object> adjFileMethodCollections = getadjcent(level, fileMethodCollections);
                diffCollections.put("normalDiff", adjFileMethodCollections);

            }
        }
        //{"diffType":"",[[File],[Method]]}
        return diffCollections;
    }

    //根据层次寻找改变函数被其他节点调用的地方
    public List<Object> getadjcent(int level, List<Object> fileMethodCollections) {
        //根据file、method求出这些所在level的关系节点
        Collection<File> connectFiles = (Collection<File>) fileMethodCollections.get(0);
        Collection<Method> connectMethods = (Collection<Method>) fileMethodCollections.get(1);
        // fileMethodCollections:diffType-> <fileDiff,methodDiff>
        for (int i = 0; i < level; i++) {
            Collection<File> tmpConnectFiles = new ArrayList<>();
            Collection<Method> tmpConnectMethods = new ArrayList<>();
            //Collection<File>、Collection<Method>查找上层关系
            for (File file : connectFiles) {
                //文件的变化只有删除、增加,通过查询结果来显示。

                tmpConnectMethods.addAll(file.getMethods().stream().map(hasMethod -> hasMethod.getEndMethod()).collect(Collectors.toList()));


            }
            Collection<Method> test = new ArrayList<>();
            for (Method method : connectMethods) {
                //寻找被人调用他的函数节点
                String version = method.getVersion();
                String fileMethodName = method.getFileMethodName();
                tmpConnectMethods.addAll(methodRepository.findConnect(version, fileMethodName));

            }
            //clear data
            connectFiles.clear();
            connectMethods.clear();
            //set data
            connectFiles.addAll(tmpConnectFiles);
            connectMethods.addAll(tmpConnectMethods);

        }
        List<Object> adjFileMethodCollections = new ArrayList<>();
        adjFileMethodCollections.add(connectFiles);
        adjFileMethodCollections.add(connectMethods);
        return adjFileMethodCollections;
    }

    public Map<String, Object> tomap(String diffType, Object object) {
        Map<String, Object> map = new HashMap<>();
        map.put(diffType, object);
        return map;
    }

}
