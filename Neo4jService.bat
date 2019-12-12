@echo off
echo "====welcome to batch to Neo4j database===="
::输入我们的neo4j的安装主目录
echo Neo4jDir %1
::输入我们的csv数据目录
echo csvSOurce %2
set pwd=%1%
set csvroot=%2%


:: neo4j目录
cd "/d %pwd%


::关闭服务器
echo "===============stop neo4j service......==============="
call neo4j stop


::删除数据库文件(如果存在)
echo "===============delete exist db file......==============="
if exist %pwd%\data\databases\graph.db (
   del  /f /s /q %pwd%\data\databases\graph.db\*.*
   rd %pwd%\data\databases\graph.db

)


::读取数据库进行存储,进入bin目录
cd %pwd%\bin


::存储数据库
echo "===============saving csv to Neo4j......==============="
 call neo4j-admin import --mode csv --database graph.db ^
 --nodes:file "%csvroot%/file_header.csv,%csvroot%/file.csv"^
 --nodes:method "%csvroot%/method_header.csv,%csvroot%/method.csv"^
 --nodes:node "%csvroot%/node_header.csv,%csvroot%/node.csv"^
 --relationships:hasMethod "%csvroot%/file_method_header.csv,%csvroot%/file_method.csv"^
 --relationships:hasNode "%csvroot%/method_node_header.csv,%csvroot%/method_node.csv"^
 --relationships:succNode "%csvroot%/node_node_header.csv,%csvroot%/node_node.csv"^
 --relationships:nodeCallMethod "%csvroot%/node_method_header.csv,%csvroot%/node_method.csv"^
 --relationships:methodCallMethod "%csvroot%/method_method_header.csv,%csvroot%/method_method.csv"^
 --ignore-duplicate-nodes true


::启动数据库服务
echo "===============start neo4j service......==============="
 neo4j start


