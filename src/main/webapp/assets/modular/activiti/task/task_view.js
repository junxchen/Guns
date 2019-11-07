layui.use(['table','form', 'ax', 'element'], function () {
    var $ = layui.jquery;
    var table = layui.table;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;
    var element = layui.element;

    var processInstanceId  = Feng.getUrlParam("processInstanceId");
    var flag = Feng.getUrlParam("flag");
    var ajax = new $ax(Feng.ctxPath + "/task/viewTaskDetail?processInstanceId=" + processInstanceId);
    var result = ajax.start();
    var formKey = Feng.ctxPath + result.data.formKey;
    var processImgBase64 = result.data.processImg;
    $("#formIframe").attr("src",formKey);
    $("#processImg").attr("src",processImgBase64);
    var approveHistoryList = result.data.approveHistoryList;
    if(flag === "false"){
        element.tabDelete("taskView","doneTaskTab");
    }else{
        form.val('approveForm', {
            "processInstanceId":processInstanceId
        });
    }
    /**
     * 审批记录
     */
    var ApproveHistory = {
        tableId: "historyTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    ApproveHistory.initColumn = function () {
        return [[
            {field: 'taskName', sort: true, title: '任务名称'},
            {field: 'taskCreateTime', sort: true, title: '任务创建时间'},
            {field: 'approveUserName', sort: true, title: '操作人'},
            {field: 'approveTime', sort: true, title: '操作时间'},
            {field: 'approveOperate', sort: true, title: '操作内容',templet: function (d) {
                    if (d.approveOperate === 0) {
                        return "-";
                    } else if (d.approveOperate === 1){
                        return "<span class='layui-badge layui-bg-green'>通过</span>";
                    }else if (d.approveOperate === 2){
                        return "<span class='layui-badge layui-bg-red'>未通过</span>";
                    }else if (d.approveOperate === 3){
                        return "<span class='layui-badge layui-bg-green'>重新申请</span>";
                    }else if (d.approveOperate === 4){
                        return "<span class='layui-badge layui-bg-red'>取消申请</span>";
                    }else{
                        return "未知";
                    }
                }
            },
            {field: 'approveNote', sort: true, title: '审批意见'}
        ]];
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + ApproveHistory.tableId,
        data: approveHistoryList,
        page: false,
        height: "620px",
        cellMinWidth: 100,
        cols: ApproveHistory.initColumn()
    });

    //表单提交事件
    form.on('submit(btnSubmit)', function (data) {
        var ajax = new $ax(Feng.ctxPath + "/task/doneTask", function (data) {
            Feng.success("审批成功！");
            //关掉对话框
            admin.closeThisDialog();
            //传给上个页面，刷新table用
            admin.putTempData('formOk', true);
        }, function (data) {
            Feng.error("审批失败！" + data.responseJSON.message)
        });
        ajax.set(data.field);
        ajax.start();

        return false;
    });
});