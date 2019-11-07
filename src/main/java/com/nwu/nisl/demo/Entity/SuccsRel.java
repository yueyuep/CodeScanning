package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "succs")
public class SuccsRel {
    @Id
    @GeneratedValue
    private Long id;
    //private List<String> SuccsRel=new ArrayList<>();
    @StartNode
    private Node startnode;
    @EndNode
    private Node endnode;

    public SuccsRel() {
    }

    public SuccsRel(Node startnode, Node endnode) {
        this.startnode = startnode;
        this.endnode = endnode;
    }


    public Long getId() {
        return id;
    }

    public Node getEndnode() {
        return endnode;
    }

    public Node getStartnode() {
        return startnode;
    }

}
