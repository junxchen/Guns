package cn.stylefeng.guns.modular.activiti.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.shiro.ShiroUser;
import cn.stylefeng.guns.modular.activiti.entity.BaseWorkFlowEntity;
import cn.stylefeng.guns.modular.activiti.model.ApproveDto;
import cn.stylefeng.guns.modular.activiti.model.HistoryModel;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.activiti.model.TaskModel;
import cn.stylefeng.guns.modular.activiti.util.TimeUtil;
import cn.stylefeng.guns.modular.system.service.UserService;
import cn.stylefeng.roses.core.util.ToolUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

/**
 * @author xuyuxiang
 * @name: TaskService
 * @description: 任务Service
 * @date 2019/10/3017:22
 */
@Service
public class TaskService<T extends BaseWorkFlowEntity> {

    private final String BASE64_PREFIX = "data:image/png;base64,";

    private final String APPLY_USER_NAME = "applyUserName";

    private final String APPLY_DATE = "applyDate";

    private final String APPLY_USER_ID = "applyUserId";

    private final String COMPLETED = "completed";

    @Autowired
    private org.activiti.engine.TaskService actTaskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private org.activiti.engine.HistoryService actHistoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    private UserService userService;

    /**
     * 查询我的待办任务列表
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 17:23
     **/
    public LayuiPageInfo findTodoPageBySpec(TaskDto taskDto) {
        Page pageContext = getPageContext();
        ShiroUser shiroUser = ShiroKit.getUser();

        String userIdString = String.valueOf(shiroUser.getId());

        List<Long> roleIdsList = shiroUser.getRoleList();

        List<String> roleIdsString = new ArrayList<>();
        for (Long roleId:roleIdsList) {
            String roleIdString = String.valueOf(roleId);
            roleIdsString.add(roleIdString);
        }

        //待办人或候选人
        taskDto.setAssigneeOrCandidateUserId(userIdString);
        //候选组
        taskDto.setCandidateGroupIdList(roleIdsString);
        TaskQuery taskQuery = this.getTodoTaskQuery(taskDto);
        List<Task> taskList = taskQuery.listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1),
                (int) pageContext.getSize());
        long count = taskQuery.count();
        List<TaskModel> taskModelList = this.copyTodoTaskListInfo(taskList);
        pageContext.setRecords(taskModelList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }

    /**
     * 获取待办任务TaskQuery
     *
     * @Author xuyuxiang
     * @Date 2019/10/31 15:11
     **/
    public TaskQuery getTodoTaskQuery(TaskDto taskDto){
        //获取TaskQuery
        TaskQuery taskQuery = actTaskService.createTaskQuery();
        String processName = taskDto.getProcessName();
        //根据流程名称查询
        if(ToolUtil.isNotEmpty(processName)){
            //根据流程名称查询流程定义实例集合
            ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionNameLike("%" + processName + "%");
            List<ProcessDefinition> processDefinitionList = processDefinitionQuery.list();
            //获取流程定义实例的key集合
            Set<String> processKeySet = new HashSet<>();
            for (ProcessDefinition processDefinition: processDefinitionList) {
                String key = processDefinition.getKey();
                processKeySet.add(key);
            }
            List<String> processKeyList = new ArrayList<>(processKeySet);
            taskQuery.processDefinitionKeyIn(processKeyList);
        }
        //根据申请人查询
        String applyUserId = taskDto.getApplyUserId();
        if(ToolUtil.isNotEmpty(applyUserId)){
            //TODO
        }
        String assigneeOrCandidateUserId = taskDto.getAssigneeOrCandidateUserId();
        List<String> candidateGroupIdList = taskDto.getCandidateGroupIdList();
        taskQuery.taskCandidateOrAssigned(assigneeOrCandidateUserId).taskCandidateGroupIn(candidateGroupIdList);
        //查询流程为未挂起的
        taskQuery.active();
        return taskQuery;
    }


    /**
     * 将Task集合转换为TaskModel集合（待办任务）
     *
     * @Author xuyuxiang
     * @Date 2019/10/31 14:17
     **/
    public List<TaskModel> copyTodoTaskListInfo(List<Task> taskList){
        //返回的TaskModel集合
        List<TaskModel> taskModelList = new ArrayList<>();

        for (Task task: taskList) {
            //任务id
            String taskId = task.getId();
            //任务名称
            String taskName = task.getName();
            Map<String, Object> variables = actTaskService.getVariables(taskId);
            //申请人id
            String applyUserId = (String) variables.get(APPLY_USER_ID);
            //申请人姓名
            String applyUserName = (String) variables.get(APPLY_USER_NAME);
            //申请人时间
            String applyDate = (String) variables.get(APPLY_DATE);
            //待办人id
            String taskAssignee = task.getAssignee();
            //待办人姓名
            String taskAssigneeUserName = userService.getById(taskAssignee).getName();
            //委托人id
            String taskOwner = task.getOwner();
            //委托人姓名
            String taskOwnerName = "-";
            if(ToolUtil.isNotEmpty(taskOwner)){
                //如果委托人不为空,则展示
                taskOwnerName = userService.getById(taskOwner).getName();
            }else{
                taskOwner = "-";
            }
            //获取流程定义id
            String processDefinitionId = task.getProcessDefinitionId();
            //获取流程定义实例
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
            //获取流程名称
            String processDefinitionName = processDefinition.getName();
            //流程实例id
            String processInstanceId = task.getProcessInstanceId();
            //待办任务标题
            String taskTitle = processDefinitionName + "(" + applyUserName + " " + applyDate + ")";

            //创建TaskModel
            TaskModel taskModel = new TaskModel();
            taskModel.setTaskId(taskId);
            taskModel.setProcessDefinitionId(processDefinitionId);
            taskModel.setProcessInstanceId(processInstanceId);
            taskModel.setTaskTitle(taskTitle);
            taskModel.setProcessName(processDefinitionName);
            taskModel.setApplyUserId(applyUserId);
            taskModel.setApplyUserName(applyUserName);
            taskModel.setApplyDate(applyDate);
            taskModel.setApproveProcess(taskName);
            taskModel.setTaskAssigneeUserId(taskAssignee);
            taskModel.setTaskAssigneeUserName(taskAssigneeUserName);
            taskModel.setTaskOwnerUserId(taskOwner);
            taskModel.setTaskOwnerName(taskOwnerName);
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }

    /**
     * 获取pageContext
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 16:04
     **/
    private Page getPageContext() {
        return LayuiPageFactory.defaultPage();
    }

    /**
     * 查看任务详情
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 9:37
     **/
    public Map<String, Object> viewTaskDetail(String processInstanceId) {
        //查看任务详情，使用流程定义实例id查看，返回数据包含表单地址，是否需要审批（flag,用于展示或隐藏审批框),审批意见记录，流程图
        Map<String,Object> resultMap = new HashMap<>();
        //获取历史流程实例
        HistoricProcessInstance processInstance = actHistoryService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        //获取流程定义实例
        String processDefinitionId = processInstance.getProcessDefinitionId();
        ProcessDefinition processDefinition = repositoryService.
                createProcessDefinitionQuery().
                processDefinitionId(processDefinitionId).
                singleResult();
        String formKey = "";
        //获取businessKey
        HistoricProcessInstance historicProcessInstance = actHistoryService.
                createHistoricProcessInstanceQuery().
                processInstanceId(processInstanceId).singleResult();
        String businessKey = historicProcessInstance.getBusinessKey();

        //判断是否有启动表单
        boolean hasStartFormKey = processDefinition.hasStartFormKey();
        //有的话则取表单key
        if(hasStartFormKey){
            //formKey + businessKey作为表单地址
            StartFormData startFormData = formService.getStartFormData(processDefinitionId);
            formKey = startFormData.getFormKey();
            formKey = formKey + businessKey;
        }

        //根据processInstanceId获取流程，如果流程未结束，且当前任务节点有自定义表单，则使用当前自定义表单
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        if(ToolUtil.isNotEmpty(task)){
            if(ToolUtil.isNotEmpty(task.getFormKey())){
                formKey = task.getFormKey() + businessKey;
            }
        }
        //是否展示审批页签，当流程未结束，且申请人不是当前登录用户时，logicFlag 为true,其他为false
        boolean logicFlag = false;
        if(ToolUtil.isNotEmpty(task)){
            String currentUserId = String.valueOf(ShiroKit.getUser().getId());
            String applyUserId = actTaskService.getVariables(task.getId()).get(APPLY_USER_ID).toString();
            if(!currentUserId.equals(applyUserId)){
                logicFlag = true;
            }
        }
        //获取流程图
        String taskImage = BASE64_PREFIX + this.getTaskImage(processInstanceId);
        resultMap.put("formKey",formKey);
        resultMap.put("logicFlag",logicFlag);
        resultMap.put("processImg",taskImage);
        List<HistoryModel> approveHistoryList = historyService.getApproveHistoryList(processInstanceId);
        resultMap.put("approveHistoryList",approveHistoryList);
        return resultMap;
    }

    /**
     * 获取运行中流程图片
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 10:08
     **/
    public String getTaskImage(String processInstanceId){
        InputStream imageStream = this.generateStream(processInstanceId);
        String imageBase64 = "";
        if (imageStream != null) {
            imageBase64 = Base64.encode(imageStream);
        }
        return imageBase64;
    }

    /**
     * 生成图片流
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 10:08
     **/
    public InputStream generateStream(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        HistoricProcessInstance historicProcessInstance =
                actHistoryService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = null;
        //已执行节点集合
        List<String> executedActivityIdList = new ArrayList<>();
        List<HistoricActivityInstance> historicActivityInstanceList = new ArrayList<>();
        if (processInstance != null) {
            processDefinitionId = processInstance.getProcessDefinitionId();
        }
        if (historicProcessInstance != null) {
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            historicActivityInstanceList =
                    actHistoryService.createHistoricActivityInstanceQuery().
                            processInstanceId(processInstanceId).
                            orderByHistoricActivityInstanceId().asc().list();
            //遍历历史节点，将节点集合放入已执行节点
            for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                String instanceActivityId = activityInstance.getActivityId();
                executedActivityIdList.add(instanceActivityId);
            }
        }
        if(ToolUtil.isOneEmpty(processDefinitionId,executedActivityIdList)){
            return null;
        }

        //高亮线路id集合
        ProcessDefinitionEntity definitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
        List<String> highLightedFlows = getHighLightedFlows(definitionEntity, historicActivityInstanceList);

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        ProcessDiagramGenerator diagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
        String activityFontName=processEngineConfiguration.getActivityFontName();
        String labelFontName=processEngineConfiguration.getLabelFontName();
        InputStream imageStream =diagramGenerator.generateDiagram(bpmnModel,"png", executedActivityIdList, highLightedFlows,activityFontName,labelFontName,null, null, 1.0);
        return imageStream;
    }

    /**
     * 高亮线路
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 10:09
     **/
    private List<String> getHighLightedFlows(
            ProcessDefinitionEntity processDefinitionEntity,
            List<HistoricActivityInstance> historicActivityInstances) {

        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i)
                            .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<ActivityImpl>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity
                    .findActivity(historicActivityInstances.get(i + 1)
                            .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances
                        .get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances
                        .get(j + 1);// 后续第二个节点
                if (activityImpl1.getStartTime().equals(
                        activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity
                            .findActivity(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                } else {
                    // 有不相同跳出循环
                    break;
                }
            }
            List<PvmTransition> pvmTransitions = activityImpl
                    .getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition
                        .getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    /**
     * 审批
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:36
     **/
    @Transactional(rollbackFor = Exception.class)
    public void doneTask(ApproveDto approveDto) {
        String processInstanceId = approveDto.getProcessInstanceId();
        Integer approveOperate = approveDto.getApproveOperate();
        String approveNote = approveDto.getApproveNote();
        //根据processInstanceId获取Task，此时Task一定处于运行中
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //获取taskId
        String taskId = task.getId();
        //完成审批时将审批记录保存
        Map<String,Object> paramMap = historyService.getLatestApproveInfo(processInstanceId,approveOperate,approveNote);

        String taskOwner = task.getOwner();
        //待办人
        String taskAssignee = task.getAssignee();
        if(ToolUtil.isNotEmpty(taskOwner)){
            //若任务所属人不为空，且是自己（任务被委托人操作后，任务回归到原待办人，owner不会清空），则正常办理
            if(taskOwner.equals(taskAssignee)){
                actTaskService.complete(taskId,paramMap);
            }else{
                //若任务所属人不为空，但不是自己，表明该任务为其他人委托办理的，则调用委托方法
                actTaskService.resolveTask(taskId,paramMap);
            }
        }else{
            //若任务所属人为空,则正常办理（当任务没有被委托时，owner默认为空,当任务被委托了，则原待办人变为owner）
            actTaskService.complete(taskId,paramMap);
        }
    }

    /**
     * 查询我的已办任务列表
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 16:59
     **/
    public LayuiPageInfo findDonePageBySpec(TaskDto taskDto) {
        Page pageContext = getPageContext();
        ShiroUser shiroUser = ShiroKit.getUser();
        String userIdString = String.valueOf(shiroUser.getId());
        taskDto.setAssigneeOrCandidateUserId(userIdString);
        //获取已办任务doneTaskQuery
        HistoricTaskInstanceQuery doneTaskQuery = historyService.getDoneTaskQuery(taskDto);
        List<HistoricTaskInstance> taskList = doneTaskQuery.listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1),
                (int) pageContext.getSize());
        long count = doneTaskQuery.count();
        //将已办任务转换成TaskModel
        List<TaskModel> taskModelList = this.copyFinishedTaskListInfo(taskList);
        pageContext.setRecords(taskModelList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }

    /**
     * 将HistoricTaskInstance集合转换为TaskModel集合（已办任务）
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 17:14
     **/
    public List<TaskModel> copyFinishedTaskListInfo(List<HistoricTaskInstance> taskList) {
        //返回的TaskModel集合
        List<TaskModel> taskModelList = new ArrayList<>();

        for (HistoricTaskInstance historicTaskInstance: taskList) {
            //任务id
            String taskId = historicTaskInstance.getId();
            //任务名称
            String taskName = historicTaskInstance.getName();
            //待办人id
            String taskAssignee = historicTaskInstance.getAssignee();
            //待办人姓名
            String taskAssigneeUserName = userService.getById(taskAssignee).getName();
            //委托人id
            String taskOwner = historicTaskInstance.getOwner();
            //委托人姓名
            String taskOwnerName = "-";
            if(ToolUtil.isNotEmpty(taskOwner)){
                //如果委托人不为空,则展示
                taskOwnerName = userService.getById(taskOwner).getName();
            }else{
                taskOwner = "-";
            }

            //获取流程定义id
            String processDefinitionId = historicTaskInstance.getProcessDefinitionId();
            //获取流程定义实例
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
            //获取流程名称
            String processDefinitionName = processDefinition.getName();
            //流程实例id
            String processInstanceId = historicTaskInstance.getProcessInstanceId();
            //开始时间
            String startTime = DateUtil.formatDateTime(historicTaskInstance.getStartTime());
            //结束时间
            String endTime = DateUtil.formatDateTime(historicTaskInstance.getEndTime());
            //耗时时间
            String duration = TimeUtil.formatDuring(historicTaskInstance.getDurationInMillis());

            //获取参数列表，因已办任务可能结束，此处只能通过历史查询
            String applyUserId = "-";
            String applyUserName = "-";
            String applyDate = "-";
            List<HistoricVariableInstance> historicVariableInstanceList = actHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (HistoricVariableInstance historicVariableInstance:historicVariableInstanceList) {
                //获取参数名称
                String variableName = historicVariableInstance.getVariableName();
                //取出参数
                if(APPLY_USER_ID.equals(variableName)){
                    //申请人id
                    applyUserId = (String) historicVariableInstance.getValue();
                }
                if(APPLY_USER_NAME.equals(variableName)){
                    //申请人姓名
                    applyUserName = (String) historicVariableInstance.getValue();
                }
                if(APPLY_DATE.equals(variableName)){
                    //申请人时间
                    applyDate = (String) historicVariableInstance.getValue();
                }
            }
            //待办任务标题
            String taskTitle = processDefinitionName + "(" + applyUserName + " " + applyDate + ")";

            //创建TaskModel
            TaskModel taskModel = new TaskModel();
            taskModel.setTaskId(taskId);
            taskModel.setProcessDefinitionId(processDefinitionId);
            taskModel.setProcessInstanceId(processInstanceId);
            taskModel.setTaskTitle(taskTitle);
            taskModel.setProcessName(processDefinitionName);
            taskModel.setApplyUserId(applyUserId);
            taskModel.setApplyUserName(applyUserName);
            taskModel.setApplyDate(applyDate);
            taskModel.setApproveProcess(taskName);
            taskModel.setTaskAssigneeUserId(taskAssignee);
            taskModel.setTaskAssigneeUserName(taskAssigneeUserName);
            taskModel.setTaskOwnerUserId(taskOwner);
            taskModel.setTaskOwnerName(taskOwnerName);
            taskModel.setStartTime(startTime);
            taskModel.setEndTime(endTime);
            taskModel.setDuration(duration);
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }

    /**
     * 将HistoricTaskInstance集合转换为TaskModel集合
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 17:14
     **/
    public List<TaskModel> copyMonitorTaskListInfo(List<HistoricTaskInstance> taskList) {
        //返回的TaskModel集合
        List<TaskModel> taskModelList = new ArrayList<>();

        for (HistoricTaskInstance historicTaskInstance: taskList) {
            //任务id
            String taskId = historicTaskInstance.getId();

            //获取流程定义id
            String processDefinitionId = historicTaskInstance.getProcessDefinitionId();
            //获取流程定义实例
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
            //获取流程名称
            String processDefinitionName = processDefinition.getName();
            //流程实例id
            String processInstanceId = historicTaskInstance.getProcessInstanceId();

            //获取参数列表，因已办任务可能结束，此处只能通过历史查询
            String applyUserId = "-";
            String applyUserName = "-";
            String applyDate = "-";
            List<HistoricVariableInstance> historicVariableInstanceList = actHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (HistoricVariableInstance historicVariableInstance:historicVariableInstanceList) {
                //获取参数名称
                String variableName = historicVariableInstance.getVariableName();
                //取出参数
                if(APPLY_USER_ID.equals(variableName)){
                    //申请人id
                    applyUserId = String.valueOf(historicVariableInstance.getValue());
                }
                if(APPLY_USER_NAME.equals(variableName)){
                    //申请人姓名
                    applyUserName = (String) historicVariableInstance.getValue();
                }
                if(APPLY_DATE.equals(variableName)){
                    //申请人时间
                    applyDate = (String) historicVariableInstance.getValue();
                }

            }

            //开始时间，此处开始时间为用户申请时间
            String startTime = applyDate;

            //根据processInstanceId获取流程状态，如果在Task表查询不到则表示该流程已经结束
            int processStatus = 0;
            Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
            //结束时间，此处结束时间为流程结束时间，若流程在运行中则不展示
            String endTime = "-";
            //任务名称,若流程结束则不展示
            String taskName = "-";
            //待办人id，若流程结束则不展示
            String taskAssignee = "-";
            //待办人姓名，若流程结束则不展示
            String taskAssigneeUserName = "-";
            //委托人id
            String taskOwner = "-";
            //委托人姓名
            String taskOwnerName = "-";
            if(ToolUtil.isEmpty(task)){
                processStatus = 2;
                //流程结束，因当前任务节点为该流程的最后节点，故结束时间为当前任务节点的结束时间
                Date tempEndTime = historicTaskInstance.getEndTime();
                endTime = DateUtil.formatDateTime(tempEndTime);
            }else{
                //流程未结束，查询流程是否挂起
                if(task.isSuspended()){
                    processStatus = 1;
                }
                //任务名称
                taskName = historicTaskInstance.getName();
                //待办人id
                taskAssignee = historicTaskInstance.getAssignee();
                //待办人姓名
                taskAssigneeUserName = userService.getById(taskAssignee).getName();
                taskOwner = historicTaskInstance.getOwner();
                if(ToolUtil.isNotEmpty(taskOwner)){
                    //如果委托人不为空,则展示
                    taskOwnerName = userService.getById(taskOwner).getName();
                }else{
                    taskOwner = "-";
                }

            }
            //结束原因(前台展示）
            String endReason = "-";
            //删除原因
            String deleteReason = historicTaskInstance.getDeleteReason();
            boolean isSameUser = applyUserId.equals(historicTaskInstance.getAssignee());
            //如果该流程已经结束，且该节点结束原因为completed,否则为异常结束
            if(processStatus == 2){
                //操作人不是申请人，则表示正常结束，否则为用户取消申请
                if(COMPLETED.equals(deleteReason)){
                    if(!isSameUser){
                        endReason = "正常结束";
                    }else{
                        endReason = "取消申请";
                    }
                }else{
                    endReason = "异常结束";
                }
            }

            //耗时时间，若流程结束，此处耗时时间为用户申请时间与流程结束时间差，流程运行则时间不展示
            String duration = "-";
            if(processStatus == 2){
                long between = DateUtil.between(DateUtil.parseDateTime(startTime), DateUtil.parseDateTime(endTime), DateUnit.MS);
                duration =  TimeUtil.formatDuring(between);
            }
            //待办任务标题
            String taskTitle = processDefinitionName + "(" + applyUserName + " " + applyDate + ")";

            //创建TaskModel
            TaskModel taskModel = new TaskModel();
            taskModel.setTaskId(taskId);
            taskModel.setProcessDefinitionId(processDefinitionId);
            taskModel.setProcessInstanceId(processInstanceId);
            taskModel.setTaskTitle(taskTitle);
            taskModel.setProcessName(processDefinitionName);
            taskModel.setApplyUserId(applyUserId);
            taskModel.setApplyUserName(applyUserName);
            taskModel.setApplyDate(applyDate);
            taskModel.setApproveProcess(taskName);
            taskModel.setTaskAssigneeUserId(taskAssignee);
            taskModel.setTaskAssigneeUserName(taskAssigneeUserName);
            taskModel.setTaskOwnerUserId(taskOwner);
            taskModel.setTaskOwnerName(taskOwnerName);
            taskModel.setStartTime(startTime);
            taskModel.setEndTime(endTime);
            taskModel.setDuration(duration);
            taskModel.setProcessStatus(processStatus);
            taskModel.setEndReason(endReason);
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }

    /**
     * 重新申请或取消申请
     *
     * @Author xuyuxiang
     * @Date 2019/11/5 16:43
     **/
    @Transactional(rollbackFor = Exception.class)
    public void reStartOrCancelRequire(T entity, Integer approveOperate) {
        String processInstanceId = entity.getInstanceId();
        //根据processInstanceId获取Task，此时Task一定处于运行中
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //获取taskId
        String taskId = task.getId();
        //完成审批时将审批记录保存
        Map<String,Object> paramMap = historyService.getLatestApproveInfo(processInstanceId,approveOperate,"-");
        //更新实体参数
        paramMap.put("entity",entity);
        actTaskService.complete(taskId,paramMap);
    }

    /**
     * 挂起任务
     *
     * @Author xuyuxiang
     * @Date 2019/11/6 10:31
     **/
    public void suspend(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    /**
     * 激活任务
     *
     * @Author xuyuxiang
     * @Date 2019/11/6 10:32
     **/
    public void active(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    /**
     * 候选人列表
     *
     * @Author xuyuxiang
     * @Date 2019/11/6 15:27
     **/
    public LayuiPageInfo candidateList(String processInstanceId) {
        Page pageContext = getPageContext();
        //获取任务，此时任务一定处于运行中
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //当前节点任务定义键值
        String taskDefinitionKey = task.getTaskDefinitionKey();
        //根据processInstanceId查询候选人集合，候选组（角色id）集合
        HistoricProcessInstance historicProcessInstance = actHistoryService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) processDefinition;
        //查询到当前节点的定义
        ActivityImpl entityActivity = processDefinitionEntity.findActivity(taskDefinitionKey);
        TaskDefinition taskDefinition = (TaskDefinition) entityActivity.getProperties().get("taskDefinition");
        //候选人
        Set<Expression> candidateUserIdExpressions = taskDefinition.getCandidateUserIdExpressions();
        //候选组
        Set<Expression> candidateGroupIdExpressions = taskDefinition.getCandidateGroupIdExpressions();

        //将用户id放到集合
        Set<Long> candidateUserIdSet = new HashSet<>();
        for (Expression expression: candidateUserIdExpressions) {
            //用户id
            Long candidateUserId = Long.valueOf(expression.getExpressionText());
            candidateUserIdSet.add(candidateUserId);
        }
        //将角色id放到组合
        Set<Long> candidateGroupIdSet = new HashSet<>();
        for (Expression expression: candidateGroupIdExpressions) {
            //组id(角色id)
            Long candidateGroupId = Long.valueOf(expression.getExpressionText());
            candidateGroupIdSet.add(candidateGroupId);
        }
        List<Map<String,Object>> candidateUserList = userService.getCandidateUser(candidateUserIdSet,candidateGroupIdSet);
        long count = candidateUserList.size();
        pageContext.setRecords(candidateUserList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }

    /**
     * 转办（将此任务转为其他人审核）
     *
     * @Author xuyuxiang
     * @Date 2019/11/7 15:11
     **/
    public void changeAssignee(String processInstanceId, String taskAssignee) {
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //转办
        actTaskService.setAssignee(task.getId(),taskAssignee);
    }

    /**
     * 委托（被委托人处理后任务依然转给委托人，只是多一条审批记录）
     *
     * @Author xuyuxiang
     * @Date 2019/11/7 17:11
     **/
    public void entrust(String processInstanceId, String taskAssignee) {
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //委托
        actTaskService.delegateTask(task.getId(),taskAssignee);
    }
}
