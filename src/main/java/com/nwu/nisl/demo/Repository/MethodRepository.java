package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.Method;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MethodRepository extends Neo4jRepository<Method, Long> {
    // 根据版本号，返回所有 method 类型的节点
    Collection<Method> findMethodsByVersion(@Param("version") String version);

    // TODO
    // 待完善，好像没什么用，基本每个函数都存在节点
    // 根据版本号，返回所有包含 node节点的 method 类型的节点
//    @Query("MATCH (p:method{version: {version}})-[r:hasNode]->(q:node{version:{version}}) RETURN p, r, q")
//    Collection<Method> findMethodsWithNodeByVersion(@Param("version") String version);

    // TODO
    // 查询语句有bug，会返回部分属性为空的节点
    // 待完善，目前返回有边为空的情况，所以使用时需要手动过滤下
    // 根据版本号，返回所有含有调用函数的函数节点
    @Query("MATCH (n:method{version:{version}})<-[m:methodCallMethod]-(p:method{version: {version}})" +
            "-[r:hasNode]->(q:node{version:{version}}) RETURN p, m, n, r, q ")
    Collection<Method> findMethodsWithCallByVersion(@Param("version") String version);

    // 根据版本号，返回指定函数名的节点
    Method findMethodByFileMethodNameAndVersion(@Param("fileMethodName") String fileMethodName,
                                                @Param("version") String version);

    //查找指向特点method的method节点
    @Query("MATCH (p:method)-[r:methodCallMethod]->(q:method{version:{version},fileMethodName:{fileMethodName}}) RETURN p,r,q")
    Collection<Method> findConnect(@Param("version") String version, @Param("fileMethodName") String fileMethodName);

}
