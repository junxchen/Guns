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
});