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
    private String nodeType="method";
    private String num;
    @Relationship(type = "hasNode", direction = Relationship.OUTGOING)
    private List<HasNode> hasNodes = new ArrayList<>();

    public Method() {
    }

    public Method(String fileMethodName, String version) {
        this.fileMethodName = fileMethodName;
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public long getId() {
        return id;
    }


    public String getNodeType() {
        return nodeType;
    }

    public String getNum() {
        return num;
    }

    public String getFileMethodName() {
        return fileMethodName;
    }

    public List<HasNode> getHasNodes() {
        return hasNodes;
    }
}
