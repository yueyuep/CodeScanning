package com.nwu.nisl.demo.Entity;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "succNode")
public class SuccNode {
    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private Node startnode;
    @EndNode
    private Node endnode;

    public SuccNode() {
    }
    public SuccNode(Node startnode, Node endnode) {
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
