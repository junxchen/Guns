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

    //草稿表单提交事件
    form.on('submit(btnSubmitDraft)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/leave/editItem", function (data) {
            Feng.success("编辑成功！");
            //关掉对话框
            admin.closeThisDialog();
            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);
        }, function (data) {
            Feng.error("添加失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();

        return false;
    });

    //流程表单提交事件
    form.on('submit(btnSubmitStart)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/leave/addAndStartItem", function (data) {
            Feng.success("编辑并启动成功！");
            //关掉对话框
            admin.closeThisDialog();
            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);
        }, function (data) {
            Feng.error("添加失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();

        return false;
    });
});