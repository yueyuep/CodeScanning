package com.nwu.nisl.demo.Component;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import com.nwu.nisl.demo.Repository.FileRepository;
import com.nwu.nisl.demo.Repository.MethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ProjectName: demo
 * @Package: com.nwu.nisl.demo.Component
 * @ClassName: ScanGraph
 * @Author: Kangaroo
 * @Description: 按层确定callGraph中待扫描的节点
 * @Date: 2019/11/27 20:19
 * @Version: 1.0
 */
@Component
public class ScanGraph {

    private DiffNode diffNode;
    private ParseDiff parseDiff;
    private MethodRepository methodRepository;

    @Autowired
    public ScanGraph(DiffNode diffNode, ParseDiff parseDiff, MethodRepository methodRepository) {
        this.diffNode = diffNode;
        this.parseDiff = parseDiff;
        this.methodRepository = methodRepository;
    }

    public Map<String, Map<String, List<Object>>> initInstance(int level) {
        // TODO
        // 1. 传入diff文件所在位置
        // 2. diff文件一直保存在固定的位置
//        diffNode.setPath(path);

        Map<String, Object> diffMap = diffNode.parseDiff();
        //得到不同变化类型的文件函数节点，供我们进行查找
        // diffMap: {"difftype":"",<version,flag,[method/file]>} 其中flag标记我们的变化部分是函数节点还是文件节点

        // TODO @1:直接把数据传送到前台，在本地数据的基础上添加标识符,不需要再重新在读取数据
        // TODO @2:对这部分数据直接在在数据库中进行更改，type、change属性进行更改
        // TODO 需要比较两种的执行效率。
        return handle(level, diffMap);
    }

    public Map<String, Collection<Object>> levelNode(int level) {
        Map<String, Map<String, List<Object>>> levelMap = initInstance(level);

        Map<String, Collection<Object>> res = new HashMap<>();
        res.put(NodeType.FILE, new ArrayList<>());
        res.put(NodeType.METHOD, new ArrayList<>());

        for (Map<String, List<Object>> map: levelMap.values()) {
            for (Map.Entry<String, List<Object>> entry: map.entrySet()) {
                res.get(entry.getKey()).addAll(entry.getValue());
            }
        }

        return res;
    }

    public Map<String, Map<String, List<Object>>> handle(int level, Map<String, Object> diffMap) {
        // 变化类型， 文件/函数  对象
        Map<String, Map<String, List<Object>>> diffCollections = new HashMap<>();

        for(Map.Entry<String, Object> entry: diffMap.entrySet()) {
            Map<String, Object> fileMethodCollections = parseDiff.getFileAndMethodInstance((Map<String, Map<String, List<String>>>) diffMap.get(entry.getKey()));
            Map<String, List<Object>> adjFileMethodCollections = getAdjacent(level, fileMethodCollections);
            diffCollections.put(entry.getKey(), adjFileMethodCollections);

        }

        return diffCollections;
    }

    // TODO
    // level扫描到的节点，不应该包含变化的节点

    //根据层次寻找改变函数被其他节点调用的地方
    public Map<String, List<Object>> getAdjacent(int level, Map<String, Object> fileMethodCollections) {
        //根据file、method求出这些所在level的关系节点
        Collection<File> connectFiles = (Collection<File>) fileMethodCollections.get(NodeType.FILE);
        Collection<Method> connectMethods = (Collection<Method>) fileMethodCollections.get(NodeType.METHOD);
        // fileMethodCollections:diffType-> <fileDiff,methodDiff>
        for (int i = 0; i < level; i++) {
            Collection<File> tmpConnectFiles = new ArrayList<>();
            Collection<Method> tmpConnectMethods = new ArrayList<>();
            //Collection<File>、Collection<Method>查找上层关系
            for (File file : connectFiles) {
                //文件的变化只有删除、增加,通过查询结果来显示。

                tmpConnectMethods.addAll(file.getMethods().stream().map(hasMethod -> hasMethod.getEndMethod()).collect(Collectors.toList()));


            }
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
        Map<String, List<Object>> adjFileMethod  = new HashMap<>();
        adjFileMethod.put(NodeType.FILE, new ArrayList<>());
        adjFileMethod.get(NodeType.FILE).addAll(connectFiles);

        adjFileMethod.put(NodeType.METHOD, new ArrayList<>());
        adjFileMethod.get(NodeType.METHOD).addAll(connectMethods);

        return adjFileMethod;
    }


}
