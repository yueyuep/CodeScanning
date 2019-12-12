//document.write("<script type='text/javascript' src='./d3Show.js'></script>");
$("#header li").click(function (e) {
    //alert($(this).text());
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
        var input = prompt("Please enter the older version of Project!");
        //显示版本1.0的图
        parame = {"version": input};
        url = "/callMethod";
        result = pareurl(parame, url);
        projectInfoClear();
        projectInfo(result);
        requestData(result, "diff");

    } else if ($(this).text() == "Version 1.1") {

        //显示版本1.1的图
        var input = prompt("Please enter the older version of Project!");
        parame = {"version": input};
        url = "/callMethod";
        var result = pareurl(parame, url);
        projectInfoClear();
        projectInfo(result);
        requestData(result, "diff");


    } else if ($(this).text() == "Diff show") {
        var input = prompt("Please enter the two versions of Project(0.9.22&0.9.23)!");
        var version = input.split("&");
        if (version.length < 2) {
            alert("format Error!");

        }
        result = diffshow(version[0], version[1]);
        projectInfoClear();
        projectInfo(result);
        diffInfo(result);
        requestData(result, "diff")


    } else if ($(this).text() == "Level One") {
        var version = prompt("Please enter the new versions of Project!");
        if (version.length < 1) {
            alert("format Error!");

        }
        //显示版l1层次的图,需要借助diff返回的结果集
        var level = 1;
        parame = {"version": version, "level": level};
        url = "/analyse/connectLevelDiff";
        analyseResult = pareurl(parame, url);
        requestData(analyseResult, "level");


        //adddiff(diffResult, analyseResult);


    } else {
        //显示版l2层次的图
        var version = prompt("Please enter the new versions of Project!");
        if (version.length < 1) {
            alert("format Error!");

        }
        var level = 3;
        parame = {"version": version, "level": level};
        url = "/analyse/connectLevelDiff";
        analyseResult = pareurl(parame, url);
        //diffResult = diffshow();
        requestData(analyseResult, "level");
    }


});

/**
 *@Author:lp on 2019/11/25 15:14
 *@Param: <DiffType,List<DiffMethod>>
 *@return: <nodes,link>  d3的数据格式
 *@Description:后台的解析结果，然后对部分数据添加变化的部分
 */
function diffData(result) {
    //获得变化的这部分数据，需要将其运用到我们的最新callgraph图上
    // TODO 如果本地含有callgraph图，我们直接从缓存中读取，不会再次请求，一方面我们的callgraph(0.9.23已经请求过了)
    //重新请求一次数据
    //diff数据集
    var oldVersion = "0.9.22";
    var newVersion = "0.9.23";
    parame = {"oldVersion": oldVersion, "newVerion": newVersion};
    url = "/diff";
    LevePareUrl(parame, url);


}

function diffshow(oldVersion, newVersion) {
    //显示版l1的图
    //测试版本
    parame = {"oldVersion": oldVersion, "newVersion": newVersion};
    url = "/diff";
    result = pareurl(parame, url);
    return result;

}

function pareurl(parame, url) {
    //根据字典参数和请求的url获得数据
    var data;
    $.ajax({
        async: false,
        type: "GET",
        url: url,
        data: parame,
        dataType: "json",
        success: function (result) {
            //0号位置代表我们的nodes、links数据
            data = result;
        },
        error: function (result) {
            alert("Data Error")
        }

    });
    return data;

}

