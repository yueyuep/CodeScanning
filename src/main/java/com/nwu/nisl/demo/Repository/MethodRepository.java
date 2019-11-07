package com.nwu.nisl.demo.Repository;
import com.nwu.nisl.demo.Entity.Method;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MethodRepository extends Neo4jRepository<Method,Long> {
    @Query("MATCH(p)-[r]->(q) RETURN p,r,q LIMIT{param1}")
    Collection<Method> findMethod(@Param("param1") int param1);
    //查找特定版本的数据
    @Query("MATCH(p:Method{version={param1}})-[r]->(q) RETURN p,r,q")
    Collection<Method> findAllByVersion(@Param("param1") String version);
}
