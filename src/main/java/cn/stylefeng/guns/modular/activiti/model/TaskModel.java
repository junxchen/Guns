package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuyuxiang
 * @name: TaskModel
 * @description: 任务model
 * @date 2019/10/3017:20
 */
@Data
public class TaskModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 流程定义id
     */
    private String processDefinitionId;

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 任务标题 ：请假流程（张三 2019-09-23 11:23:44）
     */
    private String taskTitle;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 申请人姓名
     */
    private String applyUserName;

    /**
     * 申请人时间
     */
    private String applyDate;

    /**
     * 待办人id
     */
    private String taskAssigneeUserId;

    /**
     * 待办人姓名
     */
    private String taskAssigneeUserName;

    /**
     * 委托人id
     */
    private String taskOwnerUserId;

    /**
     * 委托人姓名
     */
    private String taskOwnerName;

    /**
     * 审批环节
     */
    private String approveProcess;

    /**
     * 开始时间(已办任务、流程监控使用)
     */
    private String startTime;

    /**
     * 结束时间(已办任务、流程监控使用)
     */
    private String endTime;

    /**
     * 耗时时间(已办任务、流程监控使用)
     */
    private String duration;

    /**
     * 流程状态(已办任务、流程监控使用,0运行中，1挂起 ，2已结束)
     */
    private Integer processStatus;

    /**
     * 结束原因(已办任务、流程监控使用)
     */
    private String endReason;

}
