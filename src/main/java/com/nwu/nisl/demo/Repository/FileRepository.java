package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.HasMethod;
import com.nwu.nisl.demo.Entity.Method;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;

@Repository
public interface FileRepository extends Neo4jRepository<File, Long> {
    // 根据版本号，返回所有 file 类型的节点
    Collection<File> findFilesByVersion(@Param("version") String version);

    // 根据版本号，返回当前项目所包含的文件数
    @Query("MATCH (p:file{version:{version}}) RETURN COUNT(p)")
    int getFileNumber(@Param("version") String version);

    // 根据版本号，返回包含函数的 file 类型的节点 （不包含接口）
    @Query("MATCH (p:file{version: {version}})-[r:hasMethod]->(q:method{version: {version}}) RETURN p, r, q")
    Collection<File> findFilesWithMethodByVersion(@Param("version") String version);

    // 根据版本号，返回指定文件名的节点
    File findFileByFileNameAndVersion(@Param("fileName") String fileName, @Param("version") String version);

    //查找指向特点method的method节点
    @Query("MATCH (p:file)-[r:hasMethod]->(q:method{version:{version},fileMethodName:{fileMethodName}}) RETURN p,r,q")
    Collection<File> findConnect(@Param("version") String version, @Param("fileMethodName") String fileMethodName);
}
