layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    /**
     * 工作流管理--流程模型管理
     */
    var Model = {
        tableId: "modelTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    Model.initColumn = function () {
        return [[
            {field: 'id', hide: true, sort: true, title: 'id'},
            {field: 'name', sort: true, title: '模型名称'},
            {field: 'key', sort: true, title: '模型键值'},
            {field: 'version', sort: true, title: '模型版本'},
            {field: 'createTime', sort: true, title: '创建时间'},
            {align: 'center', toolbar: '#tableBar', title: '操作', minWidth: 200}
        ]];
    };

    /**
     * 点击查询按钮
     */
    Model.search = function () {
        var queryData = {};
        queryData['name'] = $("#name").val();
        queryData['key'] = $("#key").val();
        table.reload(Model.tableId, {where: queryData});
    };

    /**
     * 弹出添加
     */
    Model.openAddModel = function () {
        admin.putTempData('formOk', false);
        top.layui.admin.open({
            type: 2,
            title: '添加模型',
            content: Feng.ctxPath + '/model/add',
            end: function () {
                admin.getTempData('formOk') && table.reload(Model.tableId);
            }
        });
    };

    /**
     * 编辑-跳到流程设计器页面
     */
    Model.onEditModel = function (data) {
        top.layui.admin.open({
            type: 2,
            title: '编辑流程',
            maxmin: true,
            area: ['1600px', '800px'],
            content: Feng.ctxPath + "/modeler.html?modelId=" + data.id
        });
    };

    /**
     * 点击删除
     *
     * @param data 点击按钮时候的行数据
     */
    Model.onDeleteModel = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/model/delete", function (data) {
                Feng.success("删除成功!");
                table.reload(Model.tableId);
            }, function (data) {
                Feng.error("删除失败!" + data.responseJSON.message + "!");
            });
            ajax.set("modelId", data.id);
            ajax.start();
        };
        Feng.confirm("是否删除?", operation);
    };

    /**
     * 点击部署
     *
     * @param data 点击按钮时候的行数据
     */
    Model.onDeployModel = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/model/deploy", function (data) {
                Feng.success("部署成功!");
                table.reload(Model.tableId);
            }, function (data) {
                Feng.error("部署失败!" + data.responseJSON.message + "!");
            });
            ajax.set("modelId", data.id);
            ajax.start();
        };
        Feng.confirm("是否部署?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Model.tableId,
        url: Feng.ctxPath + '/model/list',
        page: true,
        height: "full-98",
        cellMinWidth: 100,
        cols: Model.initColumn()
    });

    // 搜索按钮点击事件
    $('#btnSearch').click(function () {
        Model.search();
    });

    // 添加按钮点击事件
    $('#btnAdd').click(function () {
        Model.openAddModel();
    });

    // 工具条点击事件
    table.on('tool(' + Model.tableId + ')', function (obj) {
        var data = obj.data;
        var layEvent = obj.event;

        if (layEvent === 'edit') {
            Model.onEditModel(data);
        } else if (layEvent === 'delete') {
            Model.onDeleteModel(data);
        } else if (layEvent === 'deploy') {
            Model.onDeployModel(data);
        }

    });
});
