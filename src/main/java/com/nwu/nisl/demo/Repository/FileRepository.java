package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FileRepository extends Neo4jRepository<File,Long> {
    @Query("MATCH (p:File{version={param1}})-[r:]->(q) RETURN p,r,q")
    Collection<File> findAllByVersion(@Param("param1") String version );
    @Query("MATCH (p)-[r]->(q) RETURN p,r,q LIMIT{param1}")
    Collection<File> findFiles(@Param("param1") int limit);
}
