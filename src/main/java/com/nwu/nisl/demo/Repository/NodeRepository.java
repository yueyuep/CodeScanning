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
    Node findByNodeType(@Param("nodeType") String nodeType);//按照结点类型进行查找
    Collection<Node> findByNodeTypeLike(@Param("modeType") String nodeType);
    @Query("MATCH (p)-[r]->(q) RETURN p,r,q LIMIT {limit}")
    Collection<Node> graph(@Param("limit") int limit);//限制返回结果的数目
    @Query("MATCH (p:Node{version={param1}})")
    Collection<Node> findAllByVersion(@Param("param1") String version);


}
