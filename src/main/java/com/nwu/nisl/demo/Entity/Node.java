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
    private String fileName;
    private String version;
    private String attribute;
    private String nodeType;
    @Relationship(type = "succs", direction = Relationship.OUTGOING)
    private List<SuccsRel> succsRels = new ArrayList<>();
    @Relationship(type = "call", direction = Relationship.OUTGOING)
    private List<CallRel> callMethods = new ArrayList<>();

    public Node() {
    }

    public Node(String fileName, String version, String attribute, String nodeType) {
        this.fileName = fileName;
        this.version = version;
        this.attribute = attribute;
        this.nodeType = nodeType;
    }

    public String getFileName() {
        return fileName;
    }

    public List<SuccsRel> getSuccsRels() {
        return succsRels;
    }

    public Long getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public List<CallRel> getCallMethods() {
        return callMethods;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getAttribute() {
        return attribute;
    }
}
