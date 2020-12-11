import numpy as np
from Utils import *
from Graph import *
import sys


class SimResult:
    sim = {}
    # 误差值
    eps = 0.1
    # 通过简单比较得到相似性改变的函数
    normaldiff = []
    # 文件或者函数的删除
    deletediff = []
    # 文件或者函数的增加
    adddiff = []
    # 关联分析得到的改变的函数
    connectdiff = []
    # 没有匹配的函数，文件中的内容全部被重写
    nomatch = []
    version = ""

    # 传入sim值进行分析
    def __init__(self, sim, version):
        # 文件名存的是base的文件名
        self.sim = sim
        self.version = version

    def PareFileResult(self):
        # 遍历文件对
        for file in self.sim:
            if self.sim[file] != "":
                dic = self.sim[file]
                for methodDictkey in self.sim[file].keys():
                    # 人为标记"sim"字段
                    if methodDictkey == 'sim':
                        # TODO 这部分数据可能有问题
                        start = len(
                            os.path.join(os.path.split(os.path.realpath(__file__))[0], "jsondata//" + self.version))
                        pfile = file.replace(".txt", "")
                        filename = pfile[start:len(pfile)]
                        if dic["sim"] == '0.0':
                            # 无匹配,函数均发生变化
                            self.nomatch.append(filename)
                        elif dic["sim"] == "-1.0":
                            # sim="-1.0"删除
                            self.deletediff.append(filename)
                        elif dic["sim"] == "2.0":
                            # 增加文件
                            self.adddiff.append(filename)
                        else:
                            # 接口文件，没有函数，不需要扫描
                            pass


                    # 传入所在的文件名 + 两个对应的函数对的相似度
                    else:
                        self.PareMethodResult(file, methodDictkey, dic)
            else:
                # TODO 存在文件残缺对,直接扫描扫描文件即可,无法分清函数的增加还是删除，这部分不会执行，上面使用change进行标注，已经解决了。
                # self.normaldiff.append(file.replace(".txt", ""))
                # print("是否进入")
                pass
        return self.normaldiff, self.connectdiff, self.deletediff, self.adddiff, self.nomatch

    def PareMethodResult(self, file, methodDictkey, dic):
        # 比较的两个函数对
        methodTupeKey = methodDictkey
        # 函数的相似度
        # TODO 这里得拆分可能有问题[文件全名_版本号]
        candiate_filename1 = methodDictkey[0]
        candiate_filename2 = methodDictkey[1]
        methodTupeValue = dic[methodDictkey]
        # 开始的下表
        start = len(os.path.join(os.path.split(os.path.realpath(__file__))[0], "jsondata//" + self.version))
        pfile = file.replace(".txt", "")
        filename = pfile[start:len(pfile)]
        # 只要元组键值缺少元素，则认为是函数发生了增删操作
        if methodTupeKey[0] == '' or methodTupeKey[1] == '':
            if methodTupeKey[0] == '':
                self.adddiff.append(filename + "&" + methodTupeKey[1])
            else:
                self.deletediff.append(filename + "&" + methodTupeKey[0])
        # 只是函数的内容发生了变化
        else:
            s = methodTupeValue[0][0]
            if np.allclose(s, 1.0, self.eps):
                # 0.1的误差范围内，则认为是相似的，没有发生变化
                print("simMethod:{name}".format(name=methodTupeKey[1]))
            else:
                # 发生了变化，需要根据程度进行判断，分析相关的部分
                # TODO 这个需要把前面的路径去掉。
                pfile = file.replace(".txt", "")
                filename = pfile[start:len(pfile)]
                #############################################
                print("diffMethod：{name}".format(name=file.replace(".txt", "") + "&" + candiate_filename2))
                self.normaldiff.append(filename + "&" + candiate_filename2)
                # 关联分析得到的函数变化
                self.Node2NodeConnect(file, methodTupeKey[1])

    def connectionParesing(self, diff, level):
        """
        通过自定义层次分析上下文。
        分析代码的相关和不相关的地方：
        1、normaldiff
        2、connectdiff
        3、deletediff
        4、adddiff
        5、nomatch
        通过函数调用图分析
        """

    def Node2NodeConnect(self, fieUrl, diffmethodName):
        # TODO 新函数的函数调用和旧函数的函数的调用的callreferto信息均需要参考
        with open(fieUrl, 'rt', encoding='utf-8') as file:
            lines = file.readlines()
            methodLines = lines[1:]
            for methodLine in methodLines:
                method = json.loads(methodLine)
                # 该文件下:函数名+版本号
                methodName = method["methodName"] + "&" + method["version"]
                if methodName == diffmethodName:
                    # 找到变化的函数
                    callMethodNameReferT = method["callMethodNameReferTo"]
                    for referFile in callMethodNameReferT.values():
                        # TODO referFile可能需要进行文件名的解析
                        self.connectdiff.append(referFile)
                        print("callReferMethod：{name}".format(name=referFile))


if __name__ == '__main__':
    # current path
    # 传入保存的地址
    saveurl = sys.argv[1]

    ospwd = os.path.split(os.path.realpath(__file__))[0]
    print("pwd:" + ospwd)

    oldversion = sys.argv[2]
    newversion = sys.argv[3]
    print("oldV:"+oldversion+"newV"+newversion)
    low_version = os.path.join(ospwd, os.path.join("jsondata", oldversion))
    high_version = os.path.join(ospwd, os.path.join("jsondata", newversion))
    base_file_list = []
    target_file_list = []
    pairfileList = []
    getfilePath(low_version, base_file_list)
    getfilePath(high_version, target_file_list)
    PairMethodGraph = getpairMethodGraph(base_file_list, target_file_list)
    SIm = getMethodSim(PairMethodGraph)
    sim = SimResult(SIm, oldversion)
    # version 用来统计版本号的长度
    normaldiff, connectdiff, deletediff, adddiff, nomatch = sim.PareFileResult()

    dic = {};
    dic["normaldiff"] = normaldiff
    # dic["connectdiff"] = connectdiff
    dic["deletediff"] = deletediff
    dic["adddiff"] = adddiff
    dic["nomatch"] = nomatch
    toText(dic, saveurl, "result.txt", oldversion, newversion)
    # print("done!")
