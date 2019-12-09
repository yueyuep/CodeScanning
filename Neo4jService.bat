@echo off
echo "welcome to batch to Neo4j database"
::输入我们的neo4j的安装主目录
echo Neo4jDir %1
::输入我们的csv数据目录
echo csvSOurce %2
set pwd=%1%
set csvroot=%2%


:: neo4j目录
cd "/d %pwd%


::关闭服务器
call neo4j stop


::删除数据库文件(如果存在)
if exist %pwd%\data\databases\yueyue2.db (
   del  /f /s /q %pwd%\data\databases\yueyue2.db\*.*
   rd %pwd%\data\databases\yueyue2.db

)


::读取数据库进行存储,进入bin目录
cd %pwd%\bin


::存储数据库
 call neo4j-admin import --mode csv --database yueyue2.db ^
 --nodes:file "%csvroot%/Neo4jcsv/file_header.csv,%csvroot%/Neo4jcsv//file.csv"^ 
 --nodes:method "%csvroot%/Neo4jcsv/method_header.csv,%csvroot%/Neo4jcsv/method.csv"^
 --nodes:node "%csvroot%/Neo4jcsv/node_header.csv,%csvroot%/Neo4jcsv/node.csv"^
 --relationships:hasMethod "%csvroot%/Neo4jcsv/file_method_header.csv,%csvroot%/Neo4jcsv/file_method.csv"^
 --relationships:hasNode "%csvroot%/Neo4jcsv/method_node_header.csv,%csvroot%/Neo4jcsv/method_node.csv"^
 --relationships:succNode "%csvroot%/Neo4jcsv/node_node_header.csv,%csvroot%/Neo4jcsv/node_node.csv"^
 --relationships:nodeCallMethod "%csvroot%/Neo4jcsv/node_method_header.csv,%csvroot%/Neo4jcsv/node_method.csv"^
 --relationships:methodCallMethod "%csvroot%/Neo4jcsv/method_method_header.csv,%csvroot%/Neo4jcsv/method_method.csv"^
 --ignore-duplicate-nodes true


::启动数据库服务
 neo4j start

