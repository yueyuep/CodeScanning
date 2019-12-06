/**
 *@Author:lp on 2019/11/20 16:49
 *@Param:
 *@return:
 *@Description:给本地的数据添加connectDiif类型
 */
function adddiff(callGraphJson, diffJson) {
    //我们只对nodes节点数据进行修改
    var links = callGraphJson["links"];
    var nodes = callGraphJson["nodes"];
    nodes.forEach(function (data) {
        diffJson.forEach(function (diffType) {
            if (diffType == "addDiff") {
                if (containNode(data, diffJson["addDiff"])) {
                    // 添加不同的变化标识，主要是Type、change

                }

            } else if (diffType == "deleteDiff") {
                if (containNode(data, diffJson["deleteDiff"])) {

                }

            } else {
                if (containNode(data, diffJson["normalDiff"])) {

                }

            }

        })

    })

}

function containNode(node, diffNodeList) {
    diffNodeList.forEach(function (targetNode) {
        if (node.fileMethodName == targetNode.fileMethodName && node.version == targetNode.version && node.nodeType == targetNode.nodeType) {
            return true;
        } else {
            return false;
        }

    })

}