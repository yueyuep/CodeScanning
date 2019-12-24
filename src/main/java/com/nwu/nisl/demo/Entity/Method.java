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
    private int level = 0;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setFileMethodName(String fileMethodName) {
        this.fileMethodName = fileMethodName;
    }

    public void setHasNodes(List<HasNode> hasNodes) {
        this.hasNodes = hasNodes;
    }

    public void setMethodCallMethods(List<MethodCallMethod> methodCallMethods) {
        this.methodCallMethods = methodCallMethods;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
