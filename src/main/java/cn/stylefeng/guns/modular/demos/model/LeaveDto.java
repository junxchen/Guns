package cn.stylefeng.guns.modular.demos.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuyuxiang
 * @name: LeaveDto
 * @description: 请假Dto
 * @date 2019/10/2918:06
 */
@Data
public class LeaveDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请假id
     */
    public Long leaveId;

    /**
     * 请假天数
     */
    private String leaveDays;

    /**
     * 请假原因
     */
    private String leaveReason;

    /**
     * 流程key
     */
    private String processKey;
}
