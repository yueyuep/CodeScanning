package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.Node;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
//@RepositoryRestResource(collectionResourceRel = "movies", path = "movies")
@Repository
public interface NodeRepository extends Neo4jRepository<Node, Long> {
    @Query("MATCH (p)-[r]->(q) RETURN p,r,q LIMIT {limit}")
    Collection<Node> graph(@Param("limit") int limit);//限制返回结果的数目
    Collection<Node> findAll();




}
