### V 1.0  介绍：

- ####  本项目需要将我们的项目代码转换成抽象语法树（AST）。

- #### 在抽象语法树的基础上加上函数之间的调用关系然后储出在Neo4j图数据中。同时基于图核函数比较，确定我们相邻版本之间的变化函数节点以及变化类型。在图数据库中进行标注出来。

- #### 对变化节点的关联部分进行跨层的分析。分析关联节点相关联的部分。

- #### 可视化显示我们代码各版本函数节点图、变更节点图、跨层分析结果图

  

#### 模块介绍：

#### 1. 代码转图格式以及图数据库的存储（parse、neo4j所在目录）

​    这个模块主要是使用javaParser对我们的java代码进行解析转换成AST，然后对于每一个函数节点，我们会遍历具体的MethodCallExp节点，也就是函数调用的节点，在AST中加入函数调用的边关系。然后以json的格式保存我们的代码图数据。为了加快我们存储图数据的效率，使用batchInsert进行批量插入，前提是需要将我们的图数据格式转换成csv格式

1. 数据库安装要求：需要安装Neo4j图数据进行数据库的存储，具体的配置位置application.properties.主要包括数据库的连接地址核数据库的安装位置。

   ```
   //数据库的连接地址：
   spring.data.neo4j.uri=bolt://localhost:7687
   spring.data.neo4j.username=neo4j
   spring.data.neo4j.password=123456

   //数据库的安装位置：
   #-----------------------Noej数据库的安装位置-----------------------------------------
   neo4j.install.location=E:\\neo4j-community-3.5.12
   #csv文件的目录 （见上）
   #neo4j服务器部署脚本目录
   neo4j.servicebat.location=${user.dir}\\Neo4jService.bat
   ```
2. 中间结果配置（代码的图数据格式.json文件+result.txt变化代码检测的结果）

   ```
   #diiff
   # source Path（项目源码所在目录）
   com.nwu.nisl.data.source=dataset\\source
   # json file （解析后的图数据所在目录）
   com.nwu.nisl.data.json=${user.dir}\\tools\\jsondata
   # csv file   （json转换成csv后文件所在目录）
   com.nwu.nisl.data.csv=${user.dir}\\tools\\csvdatas
   # diff file   （代码相似性分析后的结果）
   com.nwu.nisl.data.diff=${user.dir}\\src\\main\\java\\com\\nwu\\nisl\\demo\\Data\\result.txt
   ```
   
#### 2. tools模块（用于相似性分析，确定我们的变更函数块）

 python目录下是用于分析变更代码的模块：基于图核函数以函数为单位，计算两个函数所对应得图的相似性，进而来衡量我们两个相邻版本间代码得相似性，总共输出3种类型的函数变化。基本原理就是将我们AST转换成图然后在高纬矩阵空间进行比较。根据相似度值确定我们的函数是否发生改变。具体过程是先在两个相邻版本的代码中找是否含有对应的.java文件，然后进入到两个文件中，看是否有对应的两个函数块，如果有则进行图的相似性比较，反之则是发生了删除或者添加操作。

- addDiff：添加文件
- deleteDiff：删除文件
- normalDiff：修改文件

1. 安装要求：

   ```
   python=3.0+
   Cython==0.29.14
   decorator==4.1.2
   extension==1.0.0
   grakel-dev==0.1a6
   networkx==1.11
   numpy==1.17.4
   pandas==0.20.3
   scikit-learn==0.22
   scipy==1.3.3
   ```
2. springBoot配置过程

```
#-----------------------配置系统的python环境------------------------------------------
#输入python的运行环境，提前安装requirement.txt依赖关系
tools.python.url=E:\\anaconda3\\envs\\GraphSimWeb\\python.exe
#python程序的入口
tools.python.main=${user.dir}\\tools\\SimResult.py
#处理后的结果result.txt
tools.python.result=${user.dir}\\src\\main\\java\\com\\nwu\\nisl\\demo\\Data
```


#### 3.关联代码分析模块

目前已经完成的这块主要是分析变更代码相关联的部分，根据变化节点以及用户选择关联的层次在图数据中进行上下文的检索，现在正在做代码的关联漏洞检测，对变更代码以及变更代码相关联的部分进行漏洞检测，以确定我们代码变更后，哪些函数代码块是可能存在漏洞缺陷的，然后方便我们的测试人员进行重点测试。

主要代码：（src/main/java/com/nwu/nisl/demo/Component/ScanGraph.java）

### 使用教程：

注：需要按照上述要求安装好环境，并在application.properties进行数据库、python环境的配置。需要解析的源码文件需要放在dataset/source目录下面。

1. 启动springBoot项目，然后在浏览器输入localhost:8080/start/main

   stage介绍：

   - stage 1:输入（0.9.22&0.9.23）对'&'分割的相邻版本代码进行AST转图。
   - stage 2:输入（0.9.22&0.9.23）对'&'分割的相邻版本代码json数据转换成csv格式。
   - stage 3:输入（0.9.22&0.9.23）对'&'分割的相邻版本代码图数据（csv）存储到数据库中。
   - stage 4:输入（0.9.22&0.9.23）对'&'分割的相邻版本代码进行变更代码的检测，检测发生变更的函数节点以及类型。
   - stage 5:跳转到可视化显示的界面。

  

2. 结果可视化界面

   - 分别显示相邻版本代码的函数节点。
   - 显示两个相邻版本代码变化的节点，以及变化节点的类型
   - 一层、二层关联节点的显示。

   




