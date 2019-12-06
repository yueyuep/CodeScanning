
function projectInfo(result) {
    //显示我们项目的具体信息
    $(".version").val(result["version"]);
    $(".fileNumber").val(result["fileNumber"]);


}

function diffInfo(result) {
    $(".addFileNumber").val(result["addFileNumber"]);
    $(".deleteFileNumber").val(result["deleteFileNumber"]);

    //变换文件的具体显示
    $(".normalDiff").val(function () {
        var text = "";
        result["normalDiff"].forEach(function (line) {
            text = text + line

        });
        return text;

    });
    $(".addDiff").val(function () {
        var text = "";
        result["addDiff"].forEach(function (line) {
            text = text + line;

        });
        return text;

    });
    $(".deleteDiff").val(function () {
        var text = "";
        result["deleteDiff"].forEach(function (line) {
            text = text + line;

        });
        return text;

    });


}

function projectInfoClear() {
    //清空我们的textarea内中的内容
    $(".addDiff").val("");
    $(".addFileNumber").val("");
    $(".fileNumber").val("");
    $(".version").val("");
    $(".deleteDiff").val("");
    $(".deleteFileNumber").val("");
    $(".connectDiff").val("");
    $(".normalDiff").val("");

}