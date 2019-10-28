layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 系统管理--部门管理
     */
    var Process = {
        tableId: "processTable",
        condition: {
            deploymentId: ""
        }
    };

    /**
     * 初始化表格的列
     */
    Process.initColumn = function () {
        return [[
            {type: 'numbers'},
            {field: 'id', sort: true, title: '流程id'},
            {field: 'name', sort: true, title: '流程名称'},
            {field: 'key', sort: true, title: 'key'},
            {field: 'description', sort: true, title: '描述'},
            {field: 'version', sort: true, title: '版本号'},
            {field: 'resourceName', sort: true, title: '流程资源'},
            {field: 'diagramResourceName', sort: true, title: '流程图资源'},
            {field: 'deploymentId', sort: true, title: '部署id'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Process.search = function () {
        var queryData = {};
        queryData['name'] = $("#name").val();
        queryData['key'] = $("#key").val();
        table.reload(Process.tableId, {where: queryData});
    };


    /**
     * 点击删除流程
     *
     * @param data 点击按钮时候的行数据
     */
    Process.onDeleteProcess = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/process/delete", function () {
                Feng.success("删除成功!");
                table.reload(Process.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("deploymentId", data.deploymentId);
            ajax.start();
        };
        Feng.confirm("是否删除流程" + data.name + "?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Process.tableId,
        url: Feng.ctxPath + '/process/list',
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: Process.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Process.search();
    });

    // 工具条点击事件
    table.on('tool(' + Process.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'delete') {
            Process.onDeleteProcess(data);
        }
    })
});
