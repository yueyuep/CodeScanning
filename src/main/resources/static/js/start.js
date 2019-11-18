//document.write("<script type='text/javascript' src='./index.js'></script>");
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
        pareurl(parame, url)

    } else if ($(this).text() == "Version 1.1") {

        //显示版本1.1的图
        parame = {"version": "0.9.23"};
        url = "/callMethod";
        pareurl(parame, url)


    } else if ($(this).text() == "Level one") {
        //显示版l1的图



    } else if ($(this).text() == "Level two") {
        //显示版l2的图



    } else {
        //显示版l3的图
    }


});

function pareurl(parame, url) {
    //根据字典参数和请求的url获得数据
    $.ajax({
        async: true,
        type: "GET",
        url: url,
        data: parame,
        dataType: "json",
        success: function (result) {
            requestData(result);

        },
        error: function (result) {
            alert("Data Error")
        }

    })

}