layui.use(['form', 'ax'], function () {
    var $ = layui.jquery;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;

    // 让当前iframe弹层高度适应
    admin.iframeAuto();

    //表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/model/addItem", function (data) {
            //关掉对话框
            admin.closeThisDialog();

            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);

            top.layui.admin.open({
                type: 2,
                title: '编辑流程',
                maxmin: true,
                area: ['1600px', '800px'],
                content: Feng.ctxPath + data.data
            });

        }, function (data) {
            Feng.error("添加失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();
    });
});