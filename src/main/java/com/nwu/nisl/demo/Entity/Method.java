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
    private String fileName;
    private String version;
    private String methodName;
    private String nodeType;
    @Relationship(type = "succs", direction = Relationship.OUTGOING)
    private List<IncludeRel> includeRels = new ArrayList<>();

    public Method() {
    }

    public Method(String fileName, String version, String methodName) {
        this.fileName = fileName;
        this.version = version;
        this.methodName = methodName;
    }

    public String getVersion() {
        return version;
    }

    public long getId() {
        return id;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getNodeType() {
        return nodeType;
    }


    public String getFileName() {
        return fileName;
    }

    public List<IncludeRel> getIncludeRels() {
        return includeRels;
    }
}
