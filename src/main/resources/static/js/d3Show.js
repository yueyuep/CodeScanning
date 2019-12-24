//从后台请求处理数据,根据版本号进行查询
function requestData(result, flag) {
    //将版本号传到后台，进行查询
    var width = document.getElementById("leftGraph").offsetWidth;
    //var height = document.getElementById("leftGraph").offsetHeight;
    var height = 1200;
    //每次进入需要刷新svg画布分数据。
    d3.select("#leftsvg").remove();
    //设置主界面的显示：
    var leftforce;
    if (flag == "diff") {
        if (result.nodes.length < 400) {
            leftforce = d3.layout.force().charge(-100).linkDistance(150).size([width, height]);

        } else {
            leftforce = d3.layout.force().charge(-25).linkDistance(50).size([width, height]);

        }

    } else
        leftforce = d3.layout.force().charge(-100).linkDistance(150).size([width, height]);


    var leftsvg = d3.select("#leftGraph").append("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("id", "leftsvg");
    show(result, leftforce, leftsvg);

}

//下面为用到的函数
function show(graph, leftforce, leftsvg) {
    var tooltip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .attr("opacity", 0.0);
    var len = graph.nodes.length;
    leftforce.nodes(graph.nodes).links(graph.links).start();
    //TODO 需要根据不同的边关系，设置不同的颜色
    var link = leftsvg.selectAll(".link")
        .data(graph.links).enter()
        .append("line").attr("class", "link")
        .style("stroke", function (link) {
            if (link.type == "hasMethod")
                return "#180EFF";
            else
                return "#c886c6";
        });
    var node = leftsvg.selectAll(".node")
        .data(graph.nodes).enter()
        .append("circle")
        .attr("r", function (d) {
            //跨层显示节点数目比较少
            if (d.type == "addConnectDiff" || d.type == "deleteConnectDiff" || d.type == "modifyConnectDiff") {
                if (d.nodeType == "file")
                    return 23;
                else if (d.nodeType == "method")
                    return 15;
                else return 7;

            } else {
                //需要根据节点的个数来设置节点的大小,400以内、1000以内、2000以内，调整节点的大小
                if (len < 400)
                    if (d.nodeType == "file")
                        return 23;
                    else if (d.nodeType == "method")
                        return 15;
                    else return 7;
                else {
                    if (d.nodeType == "file")
                        return 15;
                    else if (d.nodeType == "method")
                        return 7;
                    else return 5;
                }


            }

        })
        .style("fill", function (node) {
            if (node.changed == "no") {
                //每有发生修改
                if (node.nodeType == "node")
                    return "#239965";
                else if (node.nodeType == "method")
                    return "#997E22";
                else
                    return "#968D99";
            } else {
                //发生修改
                if (node.level == 1)
                    return "#07B4FF";
                if (node.type == "deleteConnectDiff" || node.type == "delete")
                    return "#585956";
                else if (node.type == "addConnectDiff" || node.type == "add")
                    return "#ff0c09";
                else if (node.type == "modifyConnectDiff" || node.type == "modify")
                    return "#ff7878";
                else {
                    //其他类型，还没有处理
                }
            }

        })
        .call(leftforce.drag);

    node.on("mouseover", function (d) {
        //  需要根据不同得结点类型添加信息
        var fileName = "";
        var version = "";
        var nodeType = d.nodeType;
        if (d.nodeType == "file") {
            fileName = d.fileName;
            version = d.version;
            tooltip.html("nodeType:" + d.nodeType + "</br>" + "fileName:" + fileName + "</br>" + "version:" + version)
        } else {
            fileName = d.fileMethodName;
            version = d.version;
            tooltip.html("nodeType:" + d.nodeType + "</br>" + "methodName:" + fileName + "</br>" + "version:" + version)
        }
        tooltip.style("left", d3.event.pageX + "px")
            .style("top", d3.event.pageY + "px")
            .style("opacity", 1.0);
    })
        .on("mouseout", function () {
            tooltip.style("opacity", 0.0);
        })
        .on("click", function (d) {
            if (d.nodeType == "method") {
                // 判断是否为函数节点
                var dic = {};
                dic.fileMethodName = d.fileMethodName;
                dic.version = d.version;
                postdata(dic);


            } else {
                //其他节点不可以点击

            }


        });
    // html title attribute
    var texts = leftsvg.selectAll("text")
        .data(graph.nodes)
        .enter()
        .append("text")
        .style("fill", "black")
        .attr("text-anchor", "middle")
        .attr("dx", 10)
        .attr("dy", 8)
        .text(function (d) {
        });
    // tick定时刷新坐标的值
    leftforce.on("tick", function () {
        link.attr("x1", function (d) {
            return validateXY(d.source.x, 'x');
        })
            .attr("y1", function (d) {
                return validateXY(d.source.y, 'y');
            })
            .attr("x2", function (d) {
                return validateXY(d.target.x, 'x');
            })
            .attr("y2", function (d) {
                return validateXY(d.target.y, 'y');
            });

        //更新节点坐标
        node.attr("cx", function (d) {
            return validateXY(d.x, 'x');
        })
            .attr("cy", function (d) {
                return validateXY(d.y, 'y');
            });
        texts.attr("x", function (d) {
            return d.x;
        })

            .attr("y", function (d) {
                return d.y;
            });
    });

}

function validateXY(val, type) {
    var r = 10;
    if (val < r) return r;
    if (type == 'x') {
        if (val > this.width - r) return this.width - r
    } else {
        if (val > this.height - r) return this.height - r
    }
    return val
}
