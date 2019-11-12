package com.nwu.nisl.demo.Repository;

import com.nwu.nisl.demo.Entity.File;
import com.nwu.nisl.demo.Entity.HasMethod;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface FileRepository extends Neo4jRepository<File,Long> {
    // 根据版本号，返回所有 file 类型的节点
    Collection<File> findFilesByVersion(@Param("version") String version);

    // 根据版本号，返回包含函数的 file 类型的节点 （不包含接口）
    @Query("MATCH (p:file{version: {version}})-[r:hasMethod]->(q:method{version: {version}}) RETURN p, r, q")
    Collection<File> findFilesWithMethodByVersion(@Param("version") String version);

    // 根据版本号，返回指定文件名的节点
    File findFileByFileNameAndVersion(@Param("fileName") String fileName, @Param("version") String version);
}
