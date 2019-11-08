package cn.stylefeng.guns.modular.activiti.service;

import cn.hutool.core.date.DateUtil;
import cn.stylefeng.guns.modular.activiti.model.HistoryModel;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.system.entity.User;
import cn.stylefeng.guns.modular.system.service.UserService;
import cn.stylefeng.roses.core.util.ToolUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author xuyuxiang
 * @name: HistoryService
 * @description: 流程审批记录Service
 * @date 2019/11/116:32
 */
@Service
public class HistoryService {

    private final String START_EVENT = "startEvent";

    private final String USER_TASK = "userTask";

    private final String APPROVE_HISTORY_LIST = "approveHistoryList";

    private final String APPROVE_OPERATE = "approveOperate";

    private final String ENTRUST_PREFIX = "【委托办理】";

    @Autowired
    private org.activiti.engine.HistoryService actHistoryService;

    @Autowired
    private org.activiti.engine.TaskService actTaskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    /**
     * 获取审批记录集合，从历史变量表获取
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 16:37
     **/
    public List<HistoryModel> getApproveHistoryList(String processInstanceId) {
        //获取历史参数记录
        List<HistoricVariableInstance> historicVariableInstanceList = actHistoryService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        List<HistoryModel> historyModelList = new ArrayList<>();
        for (HistoricVariableInstance historicVariableInstance:historicVariableInstanceList) {
            //获取参数名称
            String variableName = historicVariableInstance.getVariableName();
            //如果参数名称为审批记录名称，则取出
            if(APPROVE_HISTORY_LIST.equals(variableName)){
                historyModelList = (List<HistoryModel>) historicVariableInstance.getValue();
            }
        }
        return historyModelList;
    }

    /** 
     * 获取流程实例中开始事件startEvent节点
     * 
     * @Author xuyuxiang
     * @Date 2019/11/1 17:01
     **/
    private HistoricActivityInstance getHisActivityInstanceStartEvent(String processInstanceId) {
        return actHistoryService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType(START_EVENT).singleResult();
    }

    /**
     * 获取流程中最新审批UserTask节点，即最新一条
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 11:28
     **/
    private HistoricActivityInstance getHisActivityInstanceUserTaskLatest(String processInstanceId) {
        return actHistoryService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).activityType(USER_TASK)
                .orderByHistoricActivityInstanceStartTime().desc().list().get(0);
    }

    /**
     * 保存开始事件的审批意见（供流程开始后调用）
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:17
     **/
    @Transactional(rollbackFor = Exception.class)
    public void saveStartEventApproveInfo(String processInstanceId){
        //根据processInstanceId获取Task，此时Task一定处于运行中
        Task task = actTaskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
        //获取taskId
        String taskId = task.getId();
        HistoricActivityInstance hisActivityInstanceStartEvent = this.getHisActivityInstanceStartEvent(processInstanceId);
        //获取历史流程实例
        HistoricProcessInstance historicProcessInstance = actHistoryService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        //初始化审批意见参数集合
        List<HistoryModel> historyModelList = new ArrayList<>();
        //初始化HistoryModel
        HistoryModel historyModel = new HistoryModel();
        //获取启动用户
        String startUserId = historicProcessInstance.getStartUserId();
        User user = userService.getById(startUserId);
        String userName = user.getName();
        String taskName = "发起审批";
        //操作为空
        Integer approveOperate = 0;
        String approveNote = "-";
        //审批人（发起人）姓名
        historyModel.setApproveUserName(userName);
        //任务名称
        historyModel.setTaskName(taskName);

        //获取任务创建时间
        Date startTime = hisActivityInstanceStartEvent.getStartTime();
        historyModel.setTaskCreateTime(DateUtil.formatDateTime(startTime));

        //获取任务结束时间（审批时间）
        Date endTime = hisActivityInstanceStartEvent.getEndTime();
        if (ToolUtil.isNotEmpty(endTime)) {
            historyModel.setApproveTime(DateUtil.formatDateTime(endTime));
        } else {
            historyModel.setApproveTime("-");
        }

        //审批操作
        historyModel.setApproveOperate(approveOperate);
        //审批意见
        historyModel.setApproveNote(approveNote);

        historyModelList = new ArrayList<>();
        historyModelList.add(historyModel);
        Map<String,Object> paramMap = new HashMap<>();
        //将审批意见作为参数存入
        paramMap.put(APPROVE_HISTORY_LIST, historyModelList);
        actTaskService.setVariables(taskId, paramMap);
    }

    /**
     * 填充最新审批意见
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 16:16
     **/
    public Map<String, Object> getLatestApproveInfo(String processInstanceId,
                                                    Integer approveOperate,
                                                    String approveNote) {
        HistoricActivityInstance hisActivityInstanceUserTaskLatest = this.getHisActivityInstanceUserTaskLatest(processInstanceId);
        HistoryModel historyModel = new HistoryModel();
        //获取审批人姓名
        String assignee = hisActivityInstanceUserTaskLatest.getAssignee();
        User user = userService.getById(assignee);
        String userName = user.getName();
        String taskName = hisActivityInstanceUserTaskLatest.getActivityName();
        //审批人（发起人）姓名
        historyModel.setApproveUserName(userName);

        //任务名称，若该任务是委托办理的，则在taskName前加【委托办理】
        //获取taskId
        String taskId = hisActivityInstanceUserTaskLatest.getTaskId();
        HistoricTaskInstance historicTaskInstance = actHistoryService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        //如果当前任务节点owner不为空，且owner和assigne不一致，则标明当前记录为委托办理的
        String owner = historicTaskInstance.getOwner();
        String taskAssignee = historicTaskInstance.getAssignee();
        if(ToolUtil.isNotEmpty(owner)){
            if(!taskAssignee.equals(owner)){
                historyModel.setTaskName(ENTRUST_PREFIX + taskName);
            }else{
                historyModel.setTaskName(taskName);
            }
        }else{
            historyModel.setTaskName(taskName);
        }

        //获取任务创建时间
        Date startTime = hisActivityInstanceUserTaskLatest.getStartTime();
        historyModel.setTaskCreateTime(DateUtil.formatDateTime(startTime));

        //获取任务结束时间（审批时间）
        Date endTime = hisActivityInstanceUserTaskLatest.getEndTime();
        if(ToolUtil.isNotEmpty(endTime)){
            historyModel.setApproveTime(DateUtil.formatDateTime(endTime));
        }else{
            String defineEndTime = DateUtil.now();
            historyModel.setApproveTime(defineEndTime);
        }

        //审批操作
        historyModel.setApproveOperate(approveOperate);
        //审批意见
        historyModel.setApproveNote(approveNote);
        //获取审批意见
        List<HistoryModel> approveHistoryList = this.getApproveHistoryList(processInstanceId);
        approveHistoryList.add(historyModel);

        //更新审批意见
        Map<String, Object> approveParamMap = new HashMap<>();
        approveParamMap.put(APPROVE_HISTORY_LIST, approveHistoryList);
        //将审批操作存入参数，以此控制条件流转
        approveParamMap.put(APPROVE_OPERATE,approveOperate);
        return approveParamMap;
    }

    /**
     * 获取已办任务的HistoricTaskInstanceQuery
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 17:05
     **/
    public HistoricTaskInstanceQuery getDoneTaskQuery(TaskDto taskDto){
        HistoricTaskInstanceQuery historicTaskInstanceQuery = actHistoryService
                .createHistoricTaskInstanceQuery();
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
            historicTaskInstanceQuery.processDefinitionKeyIn(processKeyList);
        }
        //根据申请人查询
        String applyUserId = taskDto.getApplyUserId();
        if(ToolUtil.isNotEmpty(applyUserId)){
            //TODO
        }
        String assigneeUserId = taskDto.getAssigneeOrCandidateUserId();
        if(ToolUtil.isNotEmpty(assigneeUserId)){
            //根据办理人查询
            historicTaskInstanceQuery.taskAssignee(assigneeUserId);
        }
        //查询当前任务已结束的
        historicTaskInstanceQuery.finished();
        return historicTaskInstanceQuery;
    }

    /**
     * 获取流程监控的HistoricProcessInstanceQuery
     *
     * @Author xuyuxiang
     * @Date 2019/11/5 18:20
     **/
    public HistoricProcessInstanceQuery getMonitorHistoricProcessInstanceQuery(TaskDto taskDto) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = actHistoryService.createHistoricProcessInstanceQuery();
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
            historicProcessInstanceQuery.processDefinitionKeyIn(processKeyList);
        }
        //根据申请人查询
        String applyUserId = taskDto.getApplyUserId();
        if(ToolUtil.isNotEmpty(applyUserId)){
            //TODO
        }
        return historicProcessInstanceQuery;
    }

    /**
     * 获取流程监控实体列表
     *
     * @Author xuyuxiang
     * @Date 2019/11/5 19:24
     **/
    public List<HistoricTaskInstance> getMonitorTaskInstanceList(TaskDto taskDto, Page pageContext) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = this.getMonitorHistoricProcessInstanceQuery(taskDto);
        //获取历史流程实体列表
        List<HistoricProcessInstance> historicProcessInstanceList = historicProcessInstanceQuery.listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1),
                (int) pageContext.getSize());
        //定义返回的任务历史流程实体列表
        List<HistoricTaskInstance> historicTaskInstanceList = new ArrayList<>();
        //获取最新任务节点信息
        for (HistoricProcessInstance historicProcessInstance:historicProcessInstanceList) {
            String processInstanceId = historicProcessInstance.getId();
            //获取historicTaskInstanceQuery
            HistoricTaskInstanceQuery historicTaskInstanceQuery = actHistoryService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .orderByTaskCreateTime()
                    .desc();
            HistoricTaskInstance historicTaskInstance = historicTaskInstanceQuery.list().get(0);
            historicTaskInstanceList.add(historicTaskInstance);

        }
        return historicTaskInstanceList;
    }

    /**
     * 获取流程监控实体数量
     *
     * @Author xuyuxiang
     * @Date 2019/11/5 19:25
     **/
    public long countMonitorTaskInstance(TaskDto taskDto) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = this.getMonitorHistoricProcessInstanceQuery(taskDto);
        return historicProcessInstanceQuery.count();
    }
}
