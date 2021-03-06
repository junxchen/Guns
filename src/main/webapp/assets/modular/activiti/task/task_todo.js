layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 工作流管理--待办任务管理
     */
    var TodoTask = {
        tableId: "todoTaskTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    TodoTask.initColumn = function () {
        return [[
            {field: 'taskId', hide: true, sort: true, title: 'taskId'},
            {field: 'processInstanceId', hide: true, sort: true, title: 'processInstanceId'},
            {field: 'taskAssigneeUserId', hide: true, sort: true, title: 'taskAssigneeUserId'},
            {field: 'taskOwnerUserId', hide: true, sort: true, title: 'taskOwnerUserId'},
            {field: 'taskTitle', sort: true, title: '任务标题'},
            {field: 'processName', sort: true, title: '流程名称'},
            {field: 'applyUserName', sort: true, title: '申请人'},
            {field: 'approveProcess', sort: true, title: '当前审批环节'},
            {field: 'taskAssigneeUserName', sort: true, title: '审批人'},
            {field: 'taskOwnerName', sort: true, title: '委托人'},
            {field: 'applyDate', sort: true, title: '申请时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    TodoTask.search = function () {
        var queryData = {};
        queryData['processName'] = $("#processName").val();
        queryData['applyUserId'] = $("#applyUserId").val();
        table.reload(TodoTask.tableId, {where: queryData});
    };

    /**
     * 点击委托审核
     *
     * @param data 点击按钮时候的行数据
     */
    TodoTask.onOpenEntrust = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '节点候选人',
            area: ['500px', '800px'],
            content: Feng.ctxPath + '/task/candidate?processInstanceId='+ data.processInstanceId
                + "&changeAssigneeOrEntrust=entrust" + "&tableId=" + TodoTask.tableId,
            end: function () {
                admin.getTempData('formOk') && table.reload(TodoTask.tableId);
            }
        });
    };

    /**
     * 弹出办理任务
     */
    TodoTask.onOpenDone = function (data) {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '任务详情',
            area: ['1600px', '800px'],
            content: Feng.ctxPath + '/task/view?processInstanceId='+ data.processInstanceId + "&viewFlag=true",
            end: function () {
                admin.getTempData('formOk') && table.reload(TodoTask.tableId);
            }
        });
    };
    // 渲染表格
    var tableResult = table.render({
        elem: '#' + TodoTask.tableId,
        url: Feng.ctxPath + '/task/todoList',
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: TodoTask.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        TodoTask.search();
    });

    // 工具条点击事件
    table.on('tool(' + TodoTask.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'done') {
            TodoTask.onOpenDone(data);
        } else if (layEvent === 'entrust') {
            //委托
            TodoTask.onOpenEntrust(data);
        }

    });
});
