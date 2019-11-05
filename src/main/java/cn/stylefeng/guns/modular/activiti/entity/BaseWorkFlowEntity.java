package cn.stylefeng.guns.modular.activiti.entity;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

/**
 * @author xuyuxiang
 * @name: BaseWorkFlowEntity
 * @description: 流程基础实体类，工作流实体需继承此类
 * @date 2019/10/2916:59
 */

public class BaseWorkFlowEntity implements Serializable {

    /**
     * 实例id
     */
    @TableField("instance_id")
    private String instanceId;

    /**
     * 申请人id
     */
    @TableField("apply_user_id")
    private Long applyUserId;

    /**
     * 申请时间
     */
    @TableField("apply_date")
    private String applyDate;

    /**
     * 申请人姓名
     */
    @TableField(exist = false)
    private String applyUserName;

    /**
     * 当前审批状态(0 未开始 1审批中 2审批完成）
     */
    @TableField(exist = false)
    private Integer approveStatus;

    /**
     * 当前审批环节
     */
    @TableField(exist = false)
    private String approveProcess;

    /**
     * 当前审批人
     */
    @TableField(exist = false)
    private String approveUserName;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(String applyDate) {
        this.applyDate = applyDate;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public Integer getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(Integer approveStatus) {
        this.approveStatus = approveStatus;
    }

    public String getApproveProcess() {
        return approveProcess;
    }

    public void setApproveProcess(String approveProcess) {
        this.approveProcess = approveProcess;
    }

    public String getApproveUserName() {
        return approveUserName;
    }

    public void setApproveUserName(String approveUserName) {
        this.approveUserName = approveUserName;
    }
}
