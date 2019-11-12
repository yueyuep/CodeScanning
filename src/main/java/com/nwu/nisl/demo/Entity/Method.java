package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity(label = "method")
public class Method {
    @Id
    @GeneratedValue
    private Long id;
    private String fileMethodName;
    private String version;
    private String num;
    private String nodeType = "method";

    @Relationship(type = "hasNode", direction = Relationship.OUTGOING)
    private List<HasNode> hasNodes = new ArrayList<>();
    @Relationship(type = "methodCallMethod", direction = Relationship.OUTGOING)
    private List<MethodCallMethod> methodCallMethods = new ArrayList<>();

    public Method() {
    }

    public Method(String fileMethodName, String version, String num) {
        this.fileMethodName = fileMethodName;
        this.version = version;
        this.num = num;
    }

    public long getId() {
        return id;
    }

    public String getFileMethodName() {
        return fileMethodName;
    }

    public String getVersion() {
        return version;
    }

    public String getNum() {
        return num;
    }

    public String getNodeType() {
        return nodeType;
    }

    public List<HasNode> getHasNodes() {
        return hasNodes;
    }

    public List<MethodCallMethod> getMethodCallMethods() {
        return methodCallMethods;
    }
}
