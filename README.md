# CodeScanning

代码增量学习
- 针对语言：Java
- 图数据库：neo4j
- 可视化显示项目的具体信息（版本号、.java文件数目）
- 设计add、delete、modify三种变化
- 基于graphKernel以函数为单位，计算相同项目跨版本项目的变化情况
- 跨层关联分析变化节点相关联的部分
#### tools模块

基于图核函数以函数为单位，计算两个函数的相似性，输出3种类型的函数变化

- addDiff：添加文件

- deleteDiff：删除文件

- normalDiff：修改文件

#### Requirments

```

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

#### 安装依赖

```bash
#tools\requirements.txt
pip install -r requirements.txt
```

#### 配置python环境变量
```
CodeScanning\src\main\resources\application.properties修改python的路径
```





