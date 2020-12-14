package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.CallGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @Author:yueyue on 2020/12/13 16:52
 * @Param: :Description:根据变化的部分分析周围的关联变化的地方,
 * getLevelNodes() 返回全部的节点信息
 * getLevelPartNodes返回部分节点信息
 */
@Service
public class LevelAnalyseServices {

    @Autowired
    private CallGraph callGraph;

    public LevelAnalyseServices() {

    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLevelNodes(String version, int level) {
        return callGraph.callGraphWithDiffAndLevel(version, level);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPartNodes(String version, int level) {


        //返回关联分析的结果
        return callGraph.getLevelPartNodes(version, level);
    }
}
