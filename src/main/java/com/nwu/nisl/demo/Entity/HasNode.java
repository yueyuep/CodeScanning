package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "hasNode")
public class HasNode {
    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private Method startMethod;
    @EndNode
    private Node endNode;

    public HasNode(){

    }

    public Long getId() {
        return id;
    }

    public Method getStartMethod() {
        return startMethod;
    }

    public Node getEndNode() {
        return endNode;
    }

}
