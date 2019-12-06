import networkx as nx
from Utils import *
class ParseGraph:
    node_num = 0
    succs = []
    attribute = {}
    methodName = ""
    Version = ""
    callMethodNameReferTo = {}
    g = nx.DiGraph()

    def __init__(self, method):  # json格式
        self.node_num = method["num"]
        self.succs = method["succs"]
        self.attribute = method["attribute"]
        self.callMethodNameReferTo = method["callMethodNameReferTo"]
        # TODO 这个直接加上MethodName字段的名字
        self.methodName = method["methodName"]
        self.Version = method["version"]

    def Parse1(self):
        """
        #  处理单个函数的节点关系，使用整数来命名结点名字

        """

        g = nx.DiGraph()
        # 添加节点间前后继关系，单个文件内
        for i, node in zip(range(self.node_num), self.succs):
            for j in node:
                if i == 0:
                    # 添加函数名字指向下属节点的边关系
                    methodNode = self.methodName  # 文件名
                    u = str(i) + "_" + self.methodName
                    v = str(j) + "_" + self.methodName
                    g.add_edge(methodNode, u, connection="include")  # 函数名包含属下节点关系
                    g.add_edge(u, v, connection="include")  # 后继节点类型
                else:
                    # 函数内各种节点的关系
                    u = str(i) + "_" + self.methodName
                    v = str(j) + "_" + self.methodName
                    g.add_edge(u, v, connection="include")  # 后继节点类型
                    # g.add_node()

        # TODO 添加函数调用节点，用文件名节点代替表示
        for callnode in self.callMethodNameReferTo.keys():
            calledmethodname = self.callMethodNameReferTo[callnode]  # 被调用函数的名字
            recallnode = str(callnode) + "_" + self.methodName
            g.add_edge(recallnode, calledmethodname, connection="call")
        self.g = g
        return g

    def Parse(self):
        """
        #  处理单个函数的节点关系
        #  普通结点----------节点号_版本——所在函数名
        #  函数结点----------函数名_版本号_所在文件名
        #  文件结点----------文件名_版本号
        :return:
        """
        g = nx.DiGraph()
        # 添加节点间前后继关系，单个文件内
        for i, node in zip(range(self.node_num), self.succs):
            for j in node:
                if i == 0:
                    # 添加函数名字指向下属节点的边关系
                    methodNode = self.Version + "_" + self.methodName  # 文件名+版本名来统一节点
                    u = str(i) + "_" + self.Version + "_" + self.methodName
                    v = str(j) + "_" + self.Version + "_" + self.methodName
                    g.add_edge(methodNode, u, {"connection": "include"})  # 函数名包含属下节点关系
                    g.add_edge(u, v, {"connection": "succes"})  # 后继节点类型
                else:
                    # 函数内各种节点的关系
                    u = str(i) + "_" + self.Version + "_" + self.methodName
                    v = str(j) + "_" + self.Version + "_" + self.methodName
                    g.add_edge(u, v, {"connection": "succes"})  # 后继节点类型
                    # g.add_node()

        # 添加函数调用节点，用文件名节点代替表示
        # TODO {"1"："HelloWord.java_HelloWord.Innerclass_main_String[]"}
        for callnode in self.callMethodNameReferTo.keys():
            callednamedic = self.callMethodNameReferTo[callnode]
            calledfilename = list(callednamedic.keys())[0]  # 被调用函数所在的文件名
            calledmethodname = callednamedic[calledfilename] + "_" + self.Version + "_" + self.methodName  # 被调用函数的名字
            recallnode = str(callnode) + "_" + self.Version + "_" + self.methodName
            g.add_edge(recallnode, calledmethodname, {"connection": "call"})
        self.g = g
        # 画图测试
        return g


if __name__ == '__main__':
    pass
