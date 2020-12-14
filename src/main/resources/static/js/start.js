$("#header li").click(function (e) {

    if ($(this).hasClass('slider')) {
        return;
    }

    var whatTab = $(this).index();

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
    if ($(this).text() == "OldVersion") {
        //t = prompt("Please enter the older version of Project!");

        /*显示旧版本数据图*/
        var oldversion = getcookies("oldversion");
        if (oldversion == "") {
            alert("未解析数据")
            return;
        }
        parame = {"version": oldversion};
        url = "/callMethod";
        result = pareurl(parame, url);
        projectInfoClear();
        projectInfo(result);
        requestData(result, "diff");

    } else if ($(this).text() == "NewVersion") {
        //显示版本1.1的图
        //var input = prompt("Please enter the older version of Project!");
        var newversion = getcookies("newversion");
        if (newversion == "") {
            alert("未解析数据")
            return;
        }
        parame = {"version": newversion};
        url = "/callMethod";
        var result = pareurl(parame, url);
        projectInfoClear();
        projectInfo(result);
        requestData(result, "diff");


    } else if ($(this).text() == "ChangeNode") {
        //var input = prompt("Please enter the two versions of Project(0.9.22&0.9.23)!");
        var oldversion = getcookies("oldversion");
        var newversion = getcookies("newversion");
        if (oldversion == "" || newversion == "") {
            alert("未解析数据")
            return;
        }
        result = diffshow(oldversion, newversion);
        projectInfoClear();
        projectInfo(result);
        diffInfo(result);
        requestData(result, "diff")


    } else if ($(this).text() == "ConnectNode") {
        //var version = prompt("Please enter the new versions of Project!");
        var version = getcookies("newversion");
        if (version == "") {
            alert("未解析数据")
            return;
        }
        //显示版l1层次的图,需要借助diff返回的结果集
        var level = 2;
        parame = {"version": version, "level": level};
        url = "/analyse/connectLevelDiff";
        analyseResult = pareurl(parame, url);
        requestData(analyseResult, "level");


        //adddiff(diffResult, analyseResult);


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

function getcookies(cookieName) {
    var cookieStr = unescape(document.cookie);
    var arr = cookieStr.split("; ");
    var cookieValue = "";
    for (var i = 0; i < arr.length; i++) {
        var temp = arr[i].split("=");
        if (temp[0] == cookieName) {
            cookieValue = temp[1];
            break;
        }
    }
    return cookieValue;

}

function removecookies() {
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval = getcookies(name);
    document.cookie = name + "=" + cval + "; expires=" + exp.toGMTString();
}


