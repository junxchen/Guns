package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author xuyuxiang
 * @name: TaskDto
 * @description: 工作流任务信息
 * @date 2019/10/3017:19
 */
@Data
public class TaskDto implements Serializable {

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 代理人或候选人
     */
    private String assigneeOrCandidateUserId;

    /**
     * 候选组
     */
    private List<String> candidateGroupIdList;
}
