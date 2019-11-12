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
}
