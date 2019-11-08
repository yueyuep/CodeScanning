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
    @Relationship(type = "succNode", direction = Relationship.OUTGOING)
    private List<SuccNode> SuccNodes = new ArrayList<>();
    @Relationship(type = "callMethod", direction = Relationship.OUTGOING)
    private List<CallMethod> callMethods = new ArrayList<>();
    public Node() {
    }

    public Node(String fileMethodName, String version, String attribute) {
        this.fileMethodName = fileMethodName;
        this.version = version;
        this.attribute = attribute;
    }

    public String getFileMethodName() {
        return fileMethodName;
    }

    public List<SuccNode> getSuccNodes() {
        return SuccNodes;
    }

    public Long getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public List<CallMethod> getCallMethods() {
        return callMethods;
    }


    public String getAttribute() {
        return attribute;
    }

}
