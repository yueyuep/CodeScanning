// window.load(function () {
//     var height = document.body.clientHeight;
//     $(".right").style.height = height;
//
// });
var flag = true;
$(".menu").mousedown(function () {
    $(this).toggleClass("closed");
    $(".messages.button").text("SaveData");
    $(".music.button").text("Json2csv");
    $(".home.button").text("ParsingGraph");
    $(".places.button").text("SimCompare");
    $(".bookmark.button").text("Visual");


    if ($(this).hasClass("closed")) {
        $(".main.button").text("Menu");
        // $(".messages.button").text("");
        // $(".music.button").text("");
        // $(".home.button").text("");
        // $(".places.button").text("");
        // $(".bookmark.button").text("");
    } else {
        $(".main.button").text("Close");

    }
});

$(".home.button").mousedown(function () {
    var parame;
    var url;
    var result;
    if ($(this).hasClass("closed")) {
        //点击过，已经完成

    } else {
        var input1 = prompt("STAGE1:Please enter the version of Project(0.9.22&0.9.23)!");
        if (input1 == null) return;
        $(".home.button").text("stage1");
        var twoversion = input1.split("&");
        parame = {"oldversion": twoversion[0], "newversion": twoversion[1]};
        url = "/start/stage1";
        AsyncPareurl(parame, url);
        $(".home.button").text("stage1");
        $(this).toggleClass("closed");

        if ($(this).hasClass("closed")) {
            $(".main.button").text("Menu");

        } else {
            $(".main.button").text("Close");

        }
    }
    $(".home.button").text("stage1");


});
$(".music.button").mousedown(function () {
    var parame;
    var url;
    var result;
    if ($(this).hasClass("closed")) {

    } else {
        var input1 = prompt("STAGE2:Please enter the version of (0.9.22&0.9.23)!");
        if (input1 == null) return;
        var twoversion = input1.split("&");
        parame = {"oldversion": twoversion[0], "newversion": twoversion[1]};
        url = "/start/stage2";
        AsyncPareurl(parame, url);
        $(this).toggleClass("closed");


        if ($(this).hasClass("closed")) {
            $(".main.button").text("Menu");

        } else {
            $(".main.button").text("Close");

        }
    }

});
$(".messages.button").mousedown(function () {
    if ($(this).hasClass("closed")) {

    } else {
        parame = {"oldversion": "", "newversion": ""};
        url = "/start/stage3";
        if (confirm("启动Neo4j服务")) {
            AsyncPareurl(parame, url);
            $(this).toggleClass("closed");
        } else {
            alert("你选择了取消")

        }

    }
    if ($(this).hasClass("closed")) {
        $(".main.button").text("Menu");

    } else {
        $(".main.button").text("Close");

    }

});
$(".places.button").mousedown(function () {
    if ($(this).hasClass("closed")) {

    } else {
        var input1 = prompt("STAGE4: Please enter the version of version to analyse the similarity");
        if (input1 == null) return;
        var twoversion = input1.split("&");
        parame = {"oldversion": twoversion[0], "newversion": twoversion[1]};
        url = "/start/stage4";
        AsyncPareurl(parame, url);
        $(this).toggleClass("closed");

        if ($(this).hasClass("closed")) {
            $(".main.button").text("Menu");
        } else {
            $(".main.button").text("Close");

        }
    }


});
$(".bookmark.button").mousedown(function () {
    if ($(this).hasClass("closed")) {

    } else {
        $(this).toggleClass("closed");
        parame = {"oldversion": "", "newversion": ""};
        url = "/start/stage5";
        if (confirm("调转可视化界面")) {
            $.ajax({
                async: false,
                type: "GET",
                data: parame,
                url: url,
                dataType: "json",
                success: function (result) {
                    //0号位置代表我们的nodes、links数据
                    window.location.href = "/"
                },
                error: function (result) {
                    alert("Error");
                }

            });
        } else {
            alert("你选择了取消")

        }

        if ($(this).hasClass("closed")) {
            $(".main.button").text("Menu");
        } else {
            $(".main.button").text("Close");

        }
    }


});


function AsyncPareurl(parame, url) {
    //根据字典参数和请求的url获得数据
    $.ajax({
        async: true,
        type: "GET",
        data: parame,
        url: url,
        dataType: "json",
        success: function (result) {
            //0号位置代表我们的nodes、links数据
            if (result == "versionError")
                alert("version error!");
            if (result == "succs")
                alert("finisheds")
        },
        error: function (result) {
            alert("error！");
        }

    });
}