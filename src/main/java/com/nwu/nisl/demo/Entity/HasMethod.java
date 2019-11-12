package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "hasMethod")
public class HasMethod {

    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private File startFile;
    @EndNode
    private Method endMethod;

    public HasMethod() {
    }

    public Long getId() {
        return id;
    }

    public File getStartFile() {
        return startFile;
    }

    public Method getEndMethod() {
        return endMethod;
    }
}
