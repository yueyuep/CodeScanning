package com.nwu.nisl.demo.Entity;

import org.neo4j.ogm.annotation.*;
@RelationshipEntity(type = "hasMethod")
public class HasMethodRel {

    @Id
    @GeneratedValue
    private Long id;
    @StartNode
    private File startfile;
    @EndNode
    private Method endMethod;

    public Long getId() {
        return id;
    }

    public File getStartfile() {
        return startfile;
    }

    public Method getEndMethod() {
        return endMethod;
    }
}
