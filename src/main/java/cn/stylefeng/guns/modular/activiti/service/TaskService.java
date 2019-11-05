package cn.stylefeng.guns.modular.activiti.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.stylefeng.guns.core.common.exception.BizExceptionEnum;
import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.shiro.ShiroUser;
import cn.stylefeng.guns.modular.activiti.model.ApproveDto;
import cn.stylefeng.guns.modular.activiti.model.HistoryModel;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.activiti.model.TaskModel;
import cn.stylefeng.guns.modular.activiti.util.TimeUtil;
import cn.stylefeng.guns.modular.system.service.UserService;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.FormService;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
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
public class TaskService {

    private final String BASE64_PREFIX = "data:image/png;base64,";

    private final String APPLY_USER_NAME = "applyUserName";

    private final String APPLY_DATE = "applyDate";

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
        return taskQuery;
    }


    /**
     * 将Task集合转换为TaskModel集合
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
            //申请人姓名
            String applyUserName = (String) variables.get(APPLY_USER_NAME);
            //申请人时间
            String applyDate = (String) variables.get(APPLY_DATE);
            //委托人id
            String taskAssignee = task.getAssignee();
            //委托人姓名
            String taskAssigneeUserName;
            if(ToolUtil.isNotEmpty(taskAssignee)){
                taskAssigneeUserName = userService.getById(taskAssignee).getName();
                //如果委托人是当前登录用户，即无委托人，则不展示
                ShiroUser shiroUser = ShiroKit.getUser();
                if(ToolUtil.isNotEmpty(shiroUser)){
                    String currentUserName = shiroUser.getName();
                    if(taskAssigneeUserName.equals(currentUserName)){
                        taskAssigneeUserName = "-";
                    }
                }else{
                    throw new ServiceException(BizExceptionEnum.NOT_LOGIN);
                }

            }else{
                taskAssigneeUserName = "-";
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
            taskModel.setApplyUserName(applyUserName);
            taskModel.setApplyDate(applyDate);
            taskModel.setApproveProcess(taskName);
            taskModel.setTaskAssigneeUserName(taskAssigneeUserName);
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
        //判断是否有启动表单
        boolean hasStartFormKey = processDefinition.hasStartFormKey();
        String formKey = "";
        //有的话则取表单key
        if(hasStartFormKey){
            //formKey + businessKey作为表单地址
            StartFormData startFormData = formService.getStartFormData(processDefinitionId);
            formKey = startFormData.getFormKey();
            HistoricProcessInstance historicProcessInstance = actHistoryService.
                    createHistoricProcessInstanceQuery().
                    processInstanceId(processInstanceId).singleResult();
            String businessKey = historicProcessInstance.getBusinessKey();
            formKey = formKey + businessKey;
        }
        //获取流程图
        String taskImage = BASE64_PREFIX + this.getTaskImage(processInstanceId);
        resultMap.put("formKey",formKey);
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
        actTaskService.complete(taskId,paramMap);
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
        HistoricTaskInstanceQuery doneTaskQuery = historyService.getFinishedTaskQuery(taskDto);
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
     * 将HistoricTaskInstance集合转换为TaskModel集合
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
            //委托人id
            String taskAssignee = historicTaskInstance.getAssignee();
            //委托人姓名
            String taskAssigneeUserName;
            if(ToolUtil.isNotEmpty(taskAssignee)){
                taskAssigneeUserName = userService.getById(taskAssignee).getName();
                //如果委托人是当前登录用户，即无委托人，则不展示
                ShiroUser shiroUser = ShiroKit.getUser();
                if(ToolUtil.isNotEmpty(shiroUser)){
                    String currentUserName = shiroUser.getName();
                    if(taskAssigneeUserName.equals(currentUserName)){
                        taskAssigneeUserName = "-";
                    }
                }else{
                    throw new ServiceException(BizExceptionEnum.NOT_LOGIN);
                }

            }else{
                taskAssigneeUserName = "-";
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
            String applyUserName = "-";
            String applyDate = "-";
            List<HistoricVariableInstance> historicVariableInstanceList = actHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            for (HistoricVariableInstance historicVariableInstance:historicVariableInstanceList) {
                //获取参数名称
                String variableName = historicVariableInstance.getVariableName();
                //如果参数名称为审批记录名称，则取出
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
            taskModel.setApplyUserName(applyUserName);
            taskModel.setApplyDate(applyDate);
            taskModel.setApproveProcess(taskName);
            taskModel.setTaskAssigneeUserName(taskAssigneeUserName);
            taskModel.setStartTime(startTime);
            taskModel.setEndTime(endTime);
            taskModel.setDuration(duration);
            taskModelList.add(taskModel);
        }
        return taskModelList;
    }
}
