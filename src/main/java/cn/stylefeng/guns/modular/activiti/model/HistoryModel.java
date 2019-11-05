package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuyuxiang
 * @name: HistoryModel
 * @description: 审批记录model
 * @date 2019/11/116:20
 */
@Data
public class HistoryModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务创建时间
     */
    private String taskCreateTime;

    /**
     * 审批人
     */
    private String approveUserName;

    /**
     * 审批时间
     */
    private String approveTime;

    /**
     * 审批操作 0为空 1通过 2未通过
     */
    private Integer approveOperate;

    /**
     * 审批意见
     */
    private String approveNote;


}
