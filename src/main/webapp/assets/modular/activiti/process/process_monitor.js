layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 工作流管理--流程监控
     */
    var Monitor = {
        tableId: "monitorTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    Monitor.initColumn = function () {
        return [[
            {field: 'taskId', hide: true, sort: true, title: 'taskId'},
            {field: 'processInstanceId', hide: true, sort: true, title: 'processInstanceId'},
            {field: 'taskTitle', sort: true, title: '任务标题'},
            {field: 'processName', sort: true, title: '流程名称'},
            {field: 'applyUserName', sort: true, title: '申请人'},
            {field: 'approveProcess', sort: true, title: '任务名称'},
            {field: 'taskAssigneeUserName', sort: true, title: '委托人'},
            {field: 'startTime', sort: true, title: '开始时间'},
            {field: 'endTime', sort: true, title: '结束时间'},
            {field: 'duration', sort: true, title: '耗时时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Monitor.search = function () {
        var queryData = {};
        queryData['processName'] = $("#processName").val();
        queryData['applyUserId'] = $("#applyUserId").val();
        table.reload(Monitor.tableId, {where: queryData});
    };

    /**
     * 弹出查看任务
     */
    Monitor.onOpenView = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '任务详情',
            area: ['1600px', '800px'],
            content: Feng.ctxPath + '/task/view?processInstanceId='+data.processInstanceId + "&flag=false",
            end: function () {
                admin.getTempData('formOk') && table.reload(Monitor.tableId);
            }
        });
    };
    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Monitor.tableId,
        url: Feng.ctxPath + '/process/monitorProcess',
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: Monitor.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Monitor.search();
    });

    // 工具条点击事件
    table.on('tool(' + Monitor.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        if (layEvent === 'view') {
            Monitor.onOpenView(data);
        }

    });
});
