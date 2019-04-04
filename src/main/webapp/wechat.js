$(function () {
    var code = getUrlParam('code');
    $('#submit').click(function () {
        var username = $('#username').val();
        var password = $('#password').val();
        $.ajax({
            type: "get",
            contentType: 'application/json',
            url: "./weChatSystem/getAccessTokenForOpenId",
            data: {
                code: code
            },
            dataType: 'json',
            async: false,
            success: function (data) {
                var openid = data.openid;
                $.ajax({
                    type: "post",
                    contentType: 'application/json',
                    url: "./weChatSystem/bindWeChatForOpenId",
                    data: JSON.stringify({
                        username: username,
                        password: password,
                        weChatIdForSendMsg: openid
                    }),
                    dataType: 'json',
                    success: function (data) {
                        if (data.res == "success") {
                            alert("绑定成功")
                        } else {
                            alert(data.msg)
                        }
                    }
                });
            }
        });
    });
});

//获取url中的参数
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]);
    return null; //返回参数值
}