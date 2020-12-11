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
    // 根据版本号，返回所有 node 类型的节点
    Collection<Node> findNodesByVersion(@Param("version") String version);


    Collection<Node> findNodesByFileMethodNameAndVersion(@Param("fileMethodName") String fileMethodName, @Param("version") String version);


    // 根据版本号，返回所有含有调用函数的node节点
    @Query("MATCH (n:node{version:{version}})<-[m:succNode]-(p:node{version: {version}})" +
            "-[r:nodeCallMethod]->(q:method{version:{version}}) RETURN *")
    Collection<Node> findNodesWithCallByVersion(@Param("version") String version);
}
