layui.use(['table', 'admin', 'ax', 'ztree'], function () {
    var $ = layui.$;
    var table = layui.table;
    var $ax = layui.ax;
    var admin = layui.admin;
    var $ZTree = layui.ztree;

    var processInstanceId = Feng.getUrlParam("processInstanceId");

    /**
     * 工作流管理--节点候选人列表
     */
    var Candidate = {
        tableId: "candidateTable",
        condition: {
        }
    };

    /**
     * 初始化表格的列
     */
    Candidate.initColumn = function () {
        return [[
            {type:'radio'},
            {field: 'userId', hide: true, sort: true, title: 'taskId'},
            {field: 'userName', sort: true, title: '候选人名称'}
        ]];
    };

    /**
     * 委托
     */
    Candidate.changeAssignee = function (data) {
        var operation = function () {
            var ajax = new $ax(Feng.ctxPath + "/task/changeAssignee", function (data) {
                Feng.success("委托成功!");
                table.reload(Candidate.tableId);
            }, function (data) {
                Feng.error("委托失败!" + data.responseJSON.message + "!");
            });
            ajax.set("processInstanceId", processInstanceId);
            ajax.set("taskAssignee", data[0].userId);
            ajax.start();
        };
        Feng.confirm("是否委托?", operation);
    };

    // 渲染表格
    var tableResult = table.render({
        elem: '#' + Candidate.tableId,
        toolbar: '#candidateToolbar',
        defaultToolbar: [],
        url: Feng.ctxPath + '/task/candidateList?processInstanceId='+ processInstanceId,
        height: "full-98",
        cellMinWidth: 100,
        cols: Candidate.initColumn()
    });

    //头工具栏事件
    table.on('toolbar(candidateTable)', function(obj){
        var checkStatus = table.checkStatus(obj.config.id); //获取选中行状态
        switch(obj.event){
            case 'getCheckData':
                var data = checkStatus.data;  //获取选中行数据
                if(data.length == 0){
                    Feng.error("请选择一个候选人!");
                }else{
                    Candidate.changeAssignee(data);
                }
                break;
        };
    });

});
