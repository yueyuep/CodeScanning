//从后台请求处理数据,根据版本号进行查询
function requestData(version) {

    var width = document.getElementById("leftGraph").offsetWidth;
    var height = document.getElementById("leftGraph").offsetHeight;
    //每次进入需要刷新svg画布分数据。
    d3.select("#leftsvg").remove();
    //
    d3.json("/graph", function (error, graph) {
        if (error) return;
        //设置主界面的显示：
        var leftforce = d3.layout.force().charge(-30).linkDistance(60).size([width, height]);
        var leftsvg = d3.select("#leftGraph").append("svg")
            .attr("width", width)
            .attr("height", height)
            .attr("id", "leftsvg");
        show(graph, leftforce, leftsvg);
    });
}

//下面为用到的函数
function show(graph, leftforce, leftsvg) {
    var tooltip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .attr("opacity", 0.0);
    leftforce.nodes(graph.nodes).links(graph.links).start();
    //TODO 需要根据不同的边关系，设置不同的颜色
    var link = leftsvg.selectAll(".link")
        .data(graph.links).enter()
        .append("line").attr("class", "link")
        .style("stroke", function (link) {
            if (link.edgeType == "hasMethod")
                return "#180EFF";
            else
                return "#c886c6";
        });
    var node = leftsvg.selectAll(".node")
        .data(graph.nodes).enter()
        .append("circle")
        .attr("r", function (d) {
            if (d.nodeType == "file")
                return 15;
            else if (d.nodeType == "method")
                return 7;
            else return 5;
        })
        .style("fill", function (node) {
            if (node.nodeType == "node")
                return "#239965";
            else if (node.nodeType == "method")
                return "#997E22";
            else
                return "#968D99";
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
        } else {
            fileName = d.fileName;
            version = d.version;
        }
        tooltip.html("fileName:" + fileName + "</br>" + "version:" + version + "</br>" + "nodeType:" + d.nodeType)
            .style("left", d3.event.pageX + "px")
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
            return d.source.x;
        })
            .attr("y1", function (d) {
                return d.source.y;
            })
            .attr("x2", function (d) {
                return d.target.x;
            })
            .attr("y2", function (d) {
                return d.target.y;
            });

        node.attr("cx", function (d) {
            return d.x;
        })
            .attr("cy", function (d) {
                return d.y;
            });
        texts.attr("x", function (d) {
            return d.x;
        })

            .attr("y", function (d) {
                return d.y;
            });
    });

}

function postdata(dic) {
    // 详情显示信息
    // $.ajax({
    //         async: true,
    //         type: "GET",
    //         url: "/info",
    //         data: dic,
    //         dataType: "json",
    //         success: function (data) {
    //             if (data.result == "SUCCESS") {
    //                 alert("SUCCES");
    //                 d3.select("#rightsvg").remove();
    //                 var rightforce = d3.layout.force().charge(-20).linkDistance(20).size([800 * 0.2, height]);
    //                 var rightsvg = d3.select("#rightgraph").append("svg")
    //                     .attr("id", "rightsvg")
    //                     .attr("width", "100%").attr("height", height);
    //                 show(data, rightforce, rightsvg);
    //
    //             } else
    //                 alert("FALSE");
    //         },
    //         error: function (data) {
    //             alert("return result" + data);
    //
    //         }
    //
    //
    //     }
    // );

}