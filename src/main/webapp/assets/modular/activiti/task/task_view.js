layui.use(['table','form', 'ax', 'element'], function () {
    var $ = layui.jquery;
    var table = layui.table;
    var $ax = layui.ax;
    var form = layui.form;
    var admin = layui.admin;
    var layer = layui.layer;
    var element = layui.element;

    var processInstanceId  = Feng.getUrlParam("processInstanceId");
    //是否展示审批页签，根据页面传过来的flag决定，在已办任务，流程监控，我的发起申请的页面跳转查看时传入false
    //不展示该审批页签，当从待办任务跳转查看时传入true，展示页签，为避免审批不通过走到调整流程时发起人能看到自己的
    //待办任务时展示审批页签，此处判断flag如果为true，且后端返回的flag也为true，才展示审批页签
    var viewFlag = Feng.getUrlParam("viewFlag");
    var ajax = new $ax(Feng.ctxPath + "/task/viewTaskDetail?processInstanceId=" + processInstanceId);
    var result = ajax.start();
    var formKey = Feng.ctxPath + result.data.formKey;
    var logicFlag =  result.data.logicFlag;
    var processImgBase64 = result.data.processImg;
    $("#formIframe").attr("src",formKey);
    $("#processImg").attr("src",processImgBase64);
    var approveHistoryList = result.data.approveHistoryList;
    //后台返回允许展示
    if(logicFlag){
        if(viewFlag === "false"){
            //但前台不展示
            element.tabDelete("taskView","doneTaskTab");
        }else{
            form.val('approveForm', {
                "processInstanceId":processInstanceId
            });
        }
    }else{
        element.tabDelete("taskView","doneTaskTab");
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