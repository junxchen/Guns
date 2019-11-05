layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 请假流程管理
     */
    var Leave = {
        tableId: "leaveTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    Leave.initColumn = function () {
        return [[
            {field: 'leaveId', hide: true, sort: true, title: 'leaveId'},
            {field: 'instanceId', hide: true, sort: true, title: 'instanceId'},
            {field: 'leaveDays', sort: true, title: '请假天数'},
            {field: 'leaveReason', sort: true, title: '请假原因'},
            {field: 'applyUserName', sort: true, title: '申请人'},
            {field: 'approveStatus', sort: true, title: '审批状态',templet: function (d) {
                    if (d.approveStatus === 0) {
                        return "<span class='layui-badge layui-bg-gray'>未开始</span>";
                    } else if (d.approveStatus === 1){
                        return "<span class='layui-badge layui-bg-black'>审批中</span>";
                    }else{
                        return "<span class='layui-badge layui-bg-blue'>审批完成</span>";
                    }
                }
            },
            {field: 'approveProcess', sort: true, title: '当前审批环节'},
            {field: 'approveUserName', sort: true, title: '当前审批人'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Leave.search = function () {
        var queryData = {};
        queryData['leaveDays'] = $("#leaveDays").val();
        queryData['leaveReason'] = $("#leaveReason").val();
        table.reload(Leave.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Leave.openAddLeave = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加请假',
            content: Feng.ctxPath + '/leave/add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };

    /**
     * 弹出编辑
     */
    Leave.onEditLeave = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '编辑请假',
            content: Feng.ctxPath + '/leave/edit?leaveId=' + data.leaveId,
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };

    /**
     * 开启流程
     */
    Leave.onStartLeave = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/leave/startItem", function (data) {
                Feng.success("启动成功!");
                table.reload(Leave.tableId);
            }, function (data) {
                Feng.error("启动失败!" + data.responseJSON.message + "!");
            });
            ajax.set("leaveId", data.leaveId);
            ajax.set("processKey", "process_leave");
            ajax.start();
        };
        Feng.confirm("是否开启流程?", operation);
    };

    /**
     * 点击删除
     *
     * @param data 点击按钮时候的行数据
     */
    Leave.onDeleteLeave = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/leave/delete", function (data) {
                Feng.success("删除成功!");
                table.reload(Leave.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("leaveId", data.leaveId);
            ajax.start();
        };
        Feng.confirm("是否删除?", operation);
    };


    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Leave.tableId,
        url: Feng.ctxPath + '/leave/list',
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: Leave.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Leave.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Leave.openAddLeave();
    });

    /**
     * 弹出查看任务
     */
    Leave.onOpenView = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '任务详情',
            area: ['1600px', '800px'],
            content: Feng.ctxPath + '/task/view?processInstanceId='+ data.instanceId + "&flag=false",
            end: function () {
                admin.getTempData('formOk') && table.reload(Leave.tableId);
            }
        });
    };

    // 工具条点击事件
    table.on('tool(' + Leave.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Leave.onEditLeave(data);
        } else if (layEvent === 'delete') {
            Leave.onDeleteLeave(data);
        } else if (layEvent === 'start') {
            Leave.onStartLeave(data);
        } else if(layEvent === 'view'){
            Leave.onOpenView(data);
        }

    });
});
