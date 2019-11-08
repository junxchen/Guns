layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 工作流管理--部署流程管理
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

    /**
     * 点击查看流程图
     *
     * @param data 点击按钮时候的行数据
     */
    Process.onViewProcessPic = function (data) {
        top.layui.admin.open({
            type: 2,
            title: '查看流程图',
            area: ['1600px', '800px'],
            content: Feng.ctxPath + "/process/viewPic?deploymentId=" + data.deploymentId + "&imageName=" + data.diagramResourceName
        });
    };

    /**
     * 点击导出
     * @param data
     */
    Process.onOpenExport = function (data) {
        layer.confirm('请选择你要导出的文件类型', {
            btn: ['xml','json'] //按钮
        }, function(){
            layer.closeAll();
            window.location.href = Feng.ctxPath + "/process/export?deploymentId="
                + data.deploymentId + "&fileType=bpmn";
        }, function(){
            layer.closeAll();
            window.location.href = Feng.ctxPath + "/process/export?deploymentId="
                + data.deploymentId + "&fileType=json";
        });
    };

    /**
     * 查看版本历史
     * @param data
     */
    Process.onOpenVersion = function (data) {
        top.layui.admin.open({
            type: 2,
            title: '查看版本',
            area: ['1600px', '800px'],
            content: Feng.ctxPath + "/process/version?key=" + data.key
        });
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
        } else if (layEvent === 'viewPic') {
            Process.onViewProcessPic(data);
        } else if(layEvent === "export"){
            Process.onOpenExport(data);
        } else if(layEvent === "version"){
            Process.onOpenVersion(data);
        }
    })
});
