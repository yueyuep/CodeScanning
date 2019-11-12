package com.nwu.nisl.demo.Entity;
import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "succNode")
public class SuccNode {
    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private Node startNode;
    @EndNode
    private Node endNode;

    public SuccNode() {
    }

    public SuccNode(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public Long getId() {
        return id;
    }

    public Node getEndNode() {
        return endNode;
    }

    public Node getStartNode() {
        return startNode;
    }

}
