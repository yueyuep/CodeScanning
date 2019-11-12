package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Component.DiffNode;
import com.nwu.nisl.demo.Repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/*
 * 解析python传过来的相似性度量文件，然后对这部分变化文件进行标注
 *
 *
 *
 * */

@Service
public class SpreadNodeServices {
    @Autowired
    private NodeRepository nodeRepository;
    @Autowired
    private DiffNode diffNode;

    public SpreadNodeServices() {
    }

    public SpreadNodeServices(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

}
