layui.use(['form', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;

    // 让当前iframe弹层高度适应
    admin.iframeAuto();

    var ajax = new $ax(Feng.ctxPath + "/leave/getItem?leaveId=" + Feng.getUrlParam("leaveId"));
    var result = ajax.start();
    result.data.processKey = "process_leave";
    form.val('leaveForm', result.data);

    //重新申请表单提交事件
    form.on('submit(btnSubmitStart)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/leave/editItem", function (data) {
            Feng.success("提交成功！");
            //关掉对话框
            admin.closeThisDialog();
            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);
        }, function (data) {
            Feng.error("提交失败！" + data.responseJSON.message)
        });
        data.field.approveOperate = 3;
        ajax.set(data.field);
        ajax.start();
        return false;
    });

    //取消申请表单提交事件
    form.on('submit(btnSubmitCancel)', function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/leave/editItem", function (data) {
                Feng.success("提交成功！");
                //关掉对话框
                admin.closeThisDialog();

                //传给上个页面，刷新table用
                admin.putTempData('formOk', true);
            }, function (data) {
                Feng.error("提交失败！" + data.responseJSON.message)
            });
            data.field.approveOperate = 4;
            ajax.set(data.field);
            ajax.start();
            return false;
        };
        Feng.confirm("是否取消申请?取消申请则该流程结束", operation);
    });
});