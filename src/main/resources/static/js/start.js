//document.write("<script type='text/javascript' src='./d3Show.js'></script>");
$("#header li").click(function (e) {
    alert($(this).text());
    // make sure we cannot click the slider
    if ($(this).hasClass('slider')) {
        return;
    }
    /* Add the slider movement */

    // what tab was pressed
    var whatTab = $(this).index();

    // Work out how far the slider needs to go
    var howFar = 380 * whatTab;

    $(".slider").css({
        left: howFar + "px"
    });

    /* Add the ripple */

    // Remove olds ones
    $(".ripple").remove();

    // Setup
    var posX = $(this).offset().left,
        posY = $(this).offset().top,
        buttonWidth = $(this).width(),
        buttonHeight = $(this).height();

    // Add the element
    $(this).prepend("<span class='ripple'></span>");

    // Make it round!
    if (buttonWidth >= buttonHeight) {
        buttonHeight = buttonWidth;
    } else {
        buttonWidth = buttonHeight;
    }

    // Get the center of the element
    var x = e.pageX - posX - buttonWidth / 2;
    var y = e.pageY - posY - buttonHeight / 2;

    // Add the ripples CSS and start the animation
    $(".ripple").css({
        width: buttonWidth,
        height: buttonHeight,
        top: y + 'px',
        left: x + 'px'
    }).addClass("rippleEffect");
    if ($(this).text() == "Version 1.0") {

        //显示版本1.0的图
        parame = {"version": "0.9.22"};
        url = "/callMethod";
        flag = 1;
        pareurl(parame, url, flag)

    } else if ($(this).text() == "Version 1.1") {

        //显示版本1.1的图
        parame = {"version": "0.9.23"};
        flag = 1;
        url = "/callMethod";
        pareurl(parame, url, flag)


    } else if ($(this).text() == "Diff show") {
        //显示版l1的图
        //测试版本
        flag = 2;
        parame = {};
        url = "/testDiff";
        pareurl(parame, url, flag);


    } else if ($(this).text() == "Level One") {
        //显示版l1层次的图
        parame = {"level": "One"};
        url = "/analyse/test";
        LevePareUrl(parame, url);


    } else {
        //显示版l2层次的图
        parame = {"level": "Two"};
        url = "/analyse/test";
        LevePareUrl(parame, url);
    }


});

function LevePareUrl(parame, url, flag) {
    //请求analyse得数据
    $.ajax({
        async: true,
        type: "GET",
        url: url,
        data: parame,
        dataType: "json",
        success: function (result) {
            diffData(result);

        },
        error: function (result) {
            alert("Data Error")
        }

    })

}

function pareurl(parame, url, flag) {
    //根据字典参数和请求的url获得数据
    $.ajax({
        async: true,
        type: "GET",
        url: url,
        data: parame,
        dataType: "json",
        success: function (result) {
            //0号位置代表我们的nodes、links数据
            if (flag == 1) {
                //初始化我们的textarea数据
                projectInfoClear();
                projectInfo(result);
            }
            if (flag == 2) {
                projectInfo(result);
                diffInfo(result);
            }
            requestData(result);
            //显示项目的信息


        },
        error: function (result) {
            alert("Data Error")
        }

    })

}

/**
 *@Author:lp on 2019/11/25 15:14
 *@Param: <DiffType,List<DiffMethod>>
 *@return: <nodes,link>  d3的数据格式
 *@Description:后台的解析结果，然后对部分数据添加变化的部分
 */
function diffData(result) {
    //获得变化的这部分数据，需要将其运用到我们的最新callgraph图上
    // TODO 如果本地含有callgraph图，我们直接从缓存中读取，不会再次请求，一方面我们的callgraph(0.9.23已经请求过了)
    //重新亲求一次数据
}

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