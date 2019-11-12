//单选一：

document.write("<script type='text/javascript' src='index.js'></script>>");
var distanceType = "0"; //默认value值
$("#routeType_0").css("background-color", "#05A5F9");
$("#routeType_0").css("color", "#FFFFFF");

function checkrouteType(id) {
    distanceType = $("#routeType_" + id)[0].attributes.value.nodeValue;
    $("#routeType li").css("background-color", "rgba(255,255,255,0)");
    $("#routeType li").css("color", "#595959");
    $("#routeType_" + id).css("background-color", "#05A5F9");
    $("#routeType_" + id).css("color", "#FFFFFF");
    //根据选择的版本号进行可视化数据显示。
    if (0 == id) {
        //TODO 查询版本v1.0的代码
        requestData(0);


    } else {
        //TODO 查询版本V.1的代码
        requestData(1);

    }
}


//点击按钮的样式表
//单选二
var planeTime = "0"; //默认value值
$("#planeTime_0").css("background-color", "#05A5F9");
$("#planeTime_0").css("color", "#FFFFFF");

function checkplaneTime(id) {
    planeTime = $("#planeTime_" + id)[0].attributes.value.nodeValue;
    $("#planeTime li").css("background-color", "rgba(255,255,255,0)");
    $("#planeTime li").css("color", "#595959");
    $("#planeTime_" + id).css("background-color", "#05A5F9");
    $("#planeTime_" + id).css("color", "#FFFFFF");
}