package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import java.util.ArrayList;
import java.util.List;
@NodeEntity(label = "node")
public class Node {
    @Id
    @GeneratedValue
    private Long id;
    private String fileMethodName;
    private String version;
    private String attribute;
    private String nodeType = "node";

    @Relationship(type = "succNode", direction = Relationship.OUTGOING)
    private List<SuccNode> succNodes = new ArrayList<>();
    @Relationship(type = "nodeCallMethod", direction = Relationship.OUTGOING)
    private List<NodeCallMethod> nodeCallMethods = new ArrayList<>();

    public Node() {
    }

    public Node(String fileMethodName, String version, String attribute) {
        this.fileMethodName = fileMethodName;
        this.version = version;
        this.attribute = attribute;
    }

    public Long getId() {
        return id;
    }

    public String getFileMethodName() {
        return fileMethodName;
    }

    public String getVersion() {
        return version;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getNodeType() {
        return nodeType;
    }

    public List<SuccNode> getSuccNodes() {
        return succNodes;
    }

    public List<NodeCallMethod> getNodeCallMethods() {
        return nodeCallMethods;
    }
}

