package com.nwu.nisl.demo.Services;

import com.nwu.nisl.demo.Repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpreadNodeServices {
    @Autowired
    private NodeRepository nodeRepository;

    public SpreadNodeServices() {
    }

    public SpreadNodeServices(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

}
