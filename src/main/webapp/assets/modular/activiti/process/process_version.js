layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 工作流管理--流程版本管理
     */
    var ProcessVersion = {
        tableId: "processVersionTable",
        condition: {
        }
    };
    var key = Feng.getUrlParam("key");
    /**
     * 初始化表格的列
     */
    ProcessVersion.initColumn = function () {
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
    ProcessVersion.search = function () {
        var queryData = {};
        queryData['name'] = $("#name").val();
        queryData['key'] = key;
        table.reload(ProcessVersion.tableId, {where: queryData});
    };


    /**
     * 点击查看流程图
     *
     * @param data 点击按钮时候的行数据
     */
    ProcessVersion.onViewProcessPic = function (data) {
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
    ProcessVersion.onOpenExport = function (data) {
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

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + ProcessVersion.tableId,
        url: Feng.ctxPath + '/process/versionHistory?key='+key,
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: ProcessVersion.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        ProcessVersion.search();
    });

    // 工具条点击事件
    table.on('tool(' + ProcessVersion.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;
        if (layEvent === 'viewPic') {
            ProcessVersion.onViewProcessPic(data);
        }else if(layEvent === 'export'){
            ProcessVersion.onOpenExport(data);
        }
    })
});
