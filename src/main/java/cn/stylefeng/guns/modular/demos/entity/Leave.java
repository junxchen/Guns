package cn.stylefeng.guns.modular.demos.entity;

import cn.stylefeng.guns.modular.activiti.entity.BaseWorkFlowEntity;
import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

/**
 * @author xuyuxiang
 * @name: Leave
 * @description: 请假实体
 * @date 2019/10/2917:53
 */
@TableName("demo_leave")
public class Leave extends BaseWorkFlowEntity {

    /**
     * 主键id
     */
    @TableId(value = "leave_id", type = IdType.ID_WORKER)
    private Long leaveId;

    /**
     * 天数
     */
    @TableField("leave_days")
    private Integer leaveDays;

    /**
     * 原因
     */
    @TableField("leave_reason")
    private String leaveReason;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private Long createUser;

    /**
     * 修改人
     */
    @TableField(value = "update_user", fill = FieldFill.UPDATE)
    private Long updateUser;

    public Long getLeaveId() {
        return leaveId;
    }

    public void setLeaveId(Long leaveId) {
        this.leaveId = leaveId;
    }

    public Integer getLeaveDays() {
        return leaveDays;
    }

    public void setLeaveDays(Integer leaveDays) {
        this.leaveDays = leaveDays;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }
}
