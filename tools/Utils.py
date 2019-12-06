import itertools
import os
from ParseFile import *
import pandas as pd
import numpy as np
import networkx as nx
from grakel import Graph
from grakel import GraphKernel


def reNodeName(baseGraph, graph):
    basenodename_List = baseGraph.nodes()
    basenodename_number_List = []
    for element in basenodename_List:
        if isinstance(element, int):
            basenodename_number_List.append()
    maxnum = max(basenodename_number_List)  # 找到最大节点的数，在这个基础上进行增加


def getfilePath(path, fileList):
    for item in os.listdir(path):
        current = os.path.join(path, item)
        if os.path.isfile(current):
            fileList.append(current)
        else:
            getfilePath(current, fileList)


def getpairFile(basefileList, targetfileList):
    # 只处理文件数目没有变化
    pairfile = []
    for base in basefileList:
        base_split = base.split("\\")
        base_name = base_split[len(base_split) - 1]
        for target in targetfileList:
            target_split = target.split("\\")
            target_name = target_split[len(target_split) - 1]
            if base_name == target_name:
                list = [base, target]
                pairfile.append(list)
    # 删除文件,当前文件中添加空白文件
    if len(basefileList) > len(targetfileList):
        pass

    else:
        pass
    # 添加文件

    return pairfile


# 修复文件数目不一样的情况
def getpairFile1(basefileList, targetfileList):
    # [base:target]文件对
    # TODO 2019/10/29 待完成任务
    diff = []
    flag = 0
    # 2019/11/14 修复函数对的匹配问题
    # 两次便利
    for base in basefileList:
        base_split = base.split("\\")
        # 这里的使用函数名匹配存在问题，一般而言，项目的位置不会发生变化，因此我们可以把项目的路径加上，去版本后的路径进行比较
        # 'H:\\GraphSimWeb\\jsondata\\0.9.23\\android-demo\\src\\androidTest\\java\\com\\example\\myapplication\\ExampleInstrumentedTest.java.txt'
        # TODO 取 s0.9.23后面的内容进行比较,start是开始的位置
        start = len((os.path.join(os.path.split(os.path.realpath(__file__))[0], "jsondata\s0.9.222")).split("\\"))
        base_name = base_split[start:len(base_split)]
        for target in targetfileList:
            target_split = target.split("\\")
            target_name = target_split[start:len(target_split)]
            # TODO 这个不影响，我们函数名增加了更多的字段信息，但是还是字符串比较
            if base_name == target_name:
                flag = 1
                break
        if flag == 0:
            pair = (base, "")
            diff.append(pair)
        else:
            diff.append((base, target))
            flag = 0
    for base in targetfileList:
        base_split = base.split("\\")
        base_name = base_split[start:len(base_split)]
        # TODO 这个函数对的匹配有点问题
        for target in basefileList:
            target_split = target.split("\\")
            # TODO 比较对象
            target_name = target_split[start:len(target_split)]
            if base_name == target_name:
                # 找到函数匹配对
                flag = 1
                break
        if flag == 0:
            pair = ("", base)
            diff.append(pair)
        else:
            diff.append((target, base))
            flag = 0

    # 两次便利肯定存在相同元素，集合去除相同的元素。
    diffset = list(set(diff))
    return diffset


def getpairMethodGraph(basefileList, targetfileList):
    # {文件名：{（函数名_v1.0,函数名_v1.1）:[methodgraph1,methodgraph2.......}}
    pairfileList = getpairFile1(basefileList, targetfileList)
    pairMethod = {}
    for paifile in pairfileList:

        base = paifile[0]
        target = paifile[1]
        # 需要判断路径是否存在（可能存在空文件对）
        if base != "" and target != "":
            # 默认以当前版本的文件名字作为我们额文件名字
            filename = paifile[1]
            # ====================
            baseMethodGraph = ParseFile(base)
            targetMethodGraph = ParseFile(target)
            # 得到当前base和target文件的函数数目
            baseMethodGraphList = baseMethodGraph.getMethodGraph()
            targetMethodGraphList = targetMethodGraph.getMethodGraph()
            # 两个文件的函数数目一样的时候(可能函数数目都为0)
            if len(baseMethodGraphList) == len(targetMethodGraphList):

                if (len(baseMethodGraphList) == 0):
                    filename = paifile[1]
                    # TODO 不存在函数这部分是接口类
                    pairMethod[filename] = {"change": "nomethod"}
                    continue
                tempdic = {}
                for B, T in itertools.product(baseMethodGraphList, targetMethodGraphList):
                    # 为对应的函数名
                    if B[0] == T[0]:
                        filename1 = B[0] + "&" + baseMethodGraph.getVersion()
                        filename2 = B[0] + "&" + targetMethodGraph.getVersion()
                        Bgraph = B[2]

                        Tgraph = T[2]

                        key = (filename1, filename2)
                        value = [Bgraph, Tgraph]
                        tempdic[key] = value
                if len(tempdic) != 0:

                    pairMethod[filename] = tempdic
                else:
                    # 存在对应的文件，但是没有函数对匹配，因此都需要进行扫描。
                    pairMethod[filename] = {"change": "nomatch"}

            # 两个文件存在文件对，但是函数的数量不相同，工程扫描应该是以文件为单位，函数变化分布，可以作为文件扫描程度的依据
            else:

                tempdic = diffMethodNum(baseMethodGraphList, targetMethodGraphList, baseMethodGraph, targetMethodGraph,
                                        filename)
                pairMethod[filename] = tempdic

        # ==============================================================
        # 当不存在文件对情况下，只需要记录文件的名字即可，不需要添加文件中含有的函数名
        elif base == "":
            filename = paifile[1]
            pairMethod[filename] = {"change": "addfile"}
        else:
            filename = paifile[0]
            pairMethod[filename] = {"change": "deletefile"}

    return pairMethod


def diffMethodNum(baseMethodGraphList, targetMethodGraphList, baseMethodGraph, targetMethodGraph, filename):
    """
    处理文件中函数数目不相等的情况
    :param baseMethodGraphList:
    :param targetMethodGraphList:
    :return:
    """
    pairMethod = {}
    tempdic = {}
    flag = 0
    if len(baseMethodGraphList) > len(targetMethodGraphList):
        for B in baseMethodGraphList:
            filename1 = B[0] + "&" + baseMethodGraph.getVersion()
            for T in targetMethodGraphList:
                if B[0] == T[0]:
                    filename2 = T[0] + "&" + targetMethodGraph.getVersion()
                    Bgraph = B[2]

                    Tgraph = T[2]

                    key = (filename1, filename2)
                    value = [Bgraph, Tgraph]
                    tempdic[key] = value
                    flag = 1
                    break
            if flag == 0:
                key = (filename1, "")
                value = []
                tempdic[key] = value
            else:
                flag = 0




    else:
        for B in targetMethodGraphList:
            filename1 = B[0] + "&" + targetMethodGraph.getVersion()
            for T in baseMethodGraphList:
                if B[0] == T[0]:
                    filename2 = T[0] + "&" + baseMethodGraph.getVersion()
                    Bgraph = B[2]

                    Tgraph = T[2]

                    key = (filename2, filename1)
                    value = [Tgraph, Bgraph]
                    tempdic[key] = value
                    flag = 1
                    break
            if flag == 0:
                key = ("", filename1)
                value = []
                tempdic[key] = value
            else:
                flag = 0
    return tempdic


def addRoot(pairList):
    """
    #为文件添加根结点root，将两个版本对应得文件用root连接起来
    :param pairList:
    :return:
    """
    graph2graph = []
    root = "Root"
    for pair in pairList:
        g = nx.DiGraph()
        g1 = ParseFile(pair[0])
        g2 = ParseFile(pair[1])
        basefileGraph = g1.connectFile()
        targetfileGraph = g2.connectFile()
        basefileGraph_node = g1.getfileName() + "_" + g1.getVersion()  # v1.0的文件节点
        targetfileGraph_node = g2.getfileName() + "_" + g2.getVersion()  # v1.1的文件节点
        g.add_edges_from(basefileGraph.edges(data=True))
        g.add_edges_from(targetfileGraph.edges(data=True))
        g.add_edge(root, basefileGraph_node, {"connecting": "include"})
        g.add_edge(root, targetfileGraph_node, {"connecting": "include"})
    return g, basefileGraph_node, targetfileGraph_node


def writeCSV(G, sim, resiltUrl):
    label = list(G.nodes())
    dataframe = pd.DataFrame(sim, columns=label, index=label)
    dataframe.to_csv(resiltUrl)


def unDirection(G):
    # 将图中的节点进行反向处理
    # (node ,node ,eges)
    for edge in G.edges(data=True):
        u = edge[0]
        v = edge[1]
        e = G.get_edge_data(u, v)
        G.remove_edge(u, v)
        G.add_edge(v, u, e)
    return G


def list2dic(list):
    dic = {}
    for tup in list:
        pnode = tup[0]
        indic = tup[1]
        dic[pnode] = indic
    return dic


def getadjlist(graph):
    num = len(graph.nodes())  # 结点的个数
    tabel = []
    # 建立索引表
    for node in graph.nodes():
        tabel.append(node)
    # 建立邻接矩阵
    adjlist = np.zeros(shape=(num, num))
    node_name = {}
    edge_label = {}
    # 返回3元组（u,v,attri）
    for dic1 in graph.edges(data=True):
        u = dic1[0]
        u_index = tabel.index(u)
        v = dic1[1]
        v_index = tabel.index(v)
        # 邻接矩阵添1
        adjlist[u_index][v_index] = 1
        # 添加结点结点名字标签
        node_name[u_index] = u
        node_name[v_index] = v
        # 添加边关系:connecting
        attr = dic1[2]
        edgename = [item for item in attr.values()][0]
        edge_label[(u_index, v_index)] = edgename
    return adjlist, node_name, edge_label


def getMethodSim(pairMethodGraph):
    sim = {}
    for filekey in pairMethodGraph.keys():
        _sim = {}
        file = pairMethodGraph[filekey]
        # 不存在文件对的情况
        keytype = [key for key in file.keys()]
        if keytype[0] == "change":
            if file["change"] == "nomethod":
                # 接口，无函数匹配
                sim[filekey] = {"sim": "1.0"}
            elif file["change"] == "nomatch":
                # 没有函数匹配，均需要扫描
                sim[filekey] = {"sim": "0.0"}
            elif file["change"] == "addfile":
                # 增加函数、或者文件
                sim[filekey] = {"sim": "2.0"}
            else:
                # 删除文件或者函数
                sim[filekey] = {"sim": "-1.0"}


        else:
            # 存在文件对的情况

            for keytupe in file.keys():
                # keytupe:(good_1.0，good_1.1函数对)
                keytupe = tuple(keytupe)
                # 如果不存在函数对
                if keytupe[0] != "" and keytupe[1] != "":
                    # method:函数graph:method[0]:base、method[1]:target
                    method = file[keytupe]
                    basegraph = method[0]
                    targetgraph = method[1]
                    adj1, node_label1, edge_label1 = getadjlist(basegraph)
                    adj2, node_label2, edge_label2 = getadjlist(targetgraph)
                    # 如果存在空结点：
                    if adj2.shape[0] == 0 or adj1.shape[0] == 0:
                        _sim[keytupe] = [[1.0]]
                    # 两个图的邻接矩阵均是非空矩阵
                    else:
                        sp_kernal = GraphKernel(kernel=["weisfeiler_lehman", "subtree_wl"], normalize=True)
                        g1 = Graph(adj1, node_label1, edge_label1)
                        g2 = Graph(adj2, node_label2, edge_label2)
                        tp = sp_kernal.fit_transform([g1])
                        tsim = sp_kernal.transform([g2])
                        _sim[keytupe] = tsim.tolist()
                else:
                    # 不存在函数对，直接令相似度为0
                    _sim[keytupe] = [[0.0]]
            sim[filekey] = _sim
    return sim


def extractFile(Root, fileList):
    # 根据项目路径返回所有的文件列表，包含文件的路径
    for item in os.listdir(Root):
        # file
        if os.path.isfile(os.path.join(Root, item)):
            fileList.append(os.path.join(Root, item))
        else:
            fileList = extractFile(os.path.join(Root, item), fileList)
    return fileList


def toText(dic, url, name, oldversion, newversion):
    """

    :param content: 写入的内容
    :param url:  保存的路径
    :param name: 函数的名字
    """
    # 在给定的路径下写入文件
    mode = ""
    path = os.path.join(url, name)
    #path = os.path.join("H:\\tools\\jsondata", name)
    print("Path:" + path)
    with open(path, mode="w") as file:
        for type in dic.keys():
            for line in dic[type]:
                line=line.replace("\\", "/")
                if type == "deleteDiff":
                    file.write(type + "&" + oldversion + "?" + line + "\n")
                else:
                    file.write(type + "&" + newversion + "?" + line + "\n")


def getVersion(url):
    cat = os.listdir(url)
    oldversion = ""
    newversion = ""
    # 获取版本号
    results1 = cat[0].split(".")
    results2 = cat[1].split(".")
    for i in range(len(results1)):
        if results1[i] > results2[i]:
            oldversion = cat[1]
            newversion = cat[0]
            break
        elif results1[i] < results2[i]:
            oldversion = cat[0]
            newversion = cat[1]
            break
        else:
            pass
    return oldversion, newversion


if __name__ == '__main__':
    # 测试函数
    Root = "H:\GraphSimWeb\jsondata\s0.9.22"
    fileList = []
    extractFile(Root, fileList)
    print("Test extractFile")
