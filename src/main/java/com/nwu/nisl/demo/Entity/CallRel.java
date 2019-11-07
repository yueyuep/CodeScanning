package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;
@RelationshipEntity(type = "call")
public class CallRel {
    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private Node startnode;
    @EndNode
    private Method endmethod;

    public Node getStartnode() {
        return startnode;
    }

    public Long getId() {
        return id;
    }

    public Method getEndmethod() {
        return endmethod;
    }

}
