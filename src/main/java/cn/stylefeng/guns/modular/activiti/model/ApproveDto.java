package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

/**
 * @author xuyuxiang
 * @name: ApproveDTO
 * @description: 审批信息DTO
 * @date 2019/11/414:26
 */
@Data
public class ApproveDto {
    private static final long serialVersionUID = 1L;

    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 审批操作（0为空 1通过 2不通过）
     */
    private Integer approveOperate;

    /**
     * 审批意见
     */
    private String approveNote;
}
