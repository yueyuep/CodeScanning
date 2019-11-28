package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.CallGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
    @Autowired
    private CallGraph callGraph;

    public LevelAnalyse() {

    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLevelNodes(String version, int level) {
        return callGraph.callGraphWithDiffAndLevel(version, level);
    }
}
