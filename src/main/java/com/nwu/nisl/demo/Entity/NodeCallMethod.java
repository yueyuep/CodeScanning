package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "nodeCallMethod")
public class NodeCallMethod {
    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private Node startNode;
    @EndNode
    private Method endMethod;

    public NodeCallMethod(){

    }

    public Long getId(){
        return id;
    }

    public Node getStartNode(){
        return startNode;
    }

    public Method getEndMethod(){
        return endMethod;
    }
}
