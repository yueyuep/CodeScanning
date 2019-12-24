package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

@NodeEntity(label = "file")
public class File {
    @Id
    @GeneratedValue
    private Long id;
    private String fileName;
    private String version;
    private String nodeType = "file";
    private int level = 0;

    @Relationship(type = "hasMethod", direction = Relationship.OUTGOING)
    private List<HasMethod> methods = new ArrayList<>();

    public File() {
    }

    public File(String fileName, String version) {
        this.fileName = fileName;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getVersion() {
        return version;
    }

    public List<HasMethod> getMethods() {
        return methods;
    }

    public String getNodeType() {
        return nodeType;
    }

    public int getLevel() {
        return level;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMethods(List<HasMethod> methods) {
        this.methods = methods;
    }
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
