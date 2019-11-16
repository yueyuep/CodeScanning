package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.ParseData;
import com.nwu.nisl.demo.Entity.Node;
import com.nwu.nisl.demo.Repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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

    @Transactional(readOnly = true)
    //  通过文件函数名和版本号进行查找
    public Map<String,Object> findAllByFileMethodName(String fileMethodName, int num, String version) {
        Collection<Node> nodes = new ArrayList<>();
        for (int i = 0; i< num; i++){
            nodes.addAll(nodeRepository.findNodesByFileMethodNameAndVersion(
                    "-".join(version, fileMethodName.concat(String.valueOf(i))), version));
        }
        return parseData.graph(version, null, null, nodes, Boolean.TRUE, Boolean.TRUE);
    }

}
