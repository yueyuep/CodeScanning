package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Component.Utils;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.demo.Entity.SuccNode;
import com.nwu.nisl.demo.Repository.NodeRepository;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.python.icu.impl.number.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/*
通过点击函数节点来显示函数节点的具体信息
 */
@Service
public class NodeServices {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private ParseData parseData;

    public NodeServices(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

//    private Map<String, Object> toD3Format(Collection<Node> normalNode) {
//        List<Map<String, Object>> nodes = new ArrayList<>();
//        List<Map<String, Object>> rels = new ArrayList<>();
//        int i = 0;
//        Iterator<Node> results = normalNode.iterator();
//        while (results.hasNext()) {
//            Node pnode = results.next();
//            //添加不存在的结点
//            int source;
//            Map<String, Object> nodeMap = utils.getNodeAttribute(pnode);
//            if (nodes.indexOf(nodeMap) == -1) {
//                nodes.add(utils.getNodeAttribute(pnode));
//                source = i;
//                i++;
//            } else {
//                source = nodes.indexOf(nodeMap);
//            }
//            for (SuccNode succNode : pnode.getSuccNodes()) {//获取后继结点的关系
//                Map<String, Object> node = utils.getNodeAttribute(succNode);
//                //查找当前结点的后继结点，构建后继关系
//                int target = nodes.indexOf(node);
//                if (target == -1) {
//                    nodes.add(node);
//                    target = nodes.indexOf(node);
//                    i++;
//
//                }
//                rels.add(map("source", source, "target", target));
//
//            }
//
//        }
//        return map("nodes", nodes, "links", rels);
//
//
//    }
//
//    private Map<String, Object> map(String key1, Object object1, String key2, Object object2) {
//        Map<String, Object> map = new HashMap<>();
//        map.put(key1, object1);
//        map.put(key2, object2);
//        return map;
//    }

    @Transactional(readOnly = true)
    //  通过文件函数名和版本号进行查找
    public Map<String,Object> findAllByFileMethodName(String fileMethodName, int num, String version) {
        Collection<Node> nodes = new ArrayList<>();
        for (int i = 0; i< num; i++){
            nodes.addAll(nodeRepository.findNodesByFileMethodNameAndVersion(
                    fileMethodName.concat("-").concat(String.valueOf(i)), version));
        }
        return parseData.graph(null, null, nodes, Boolean.TRUE, Boolean.TRUE);
    }

}
