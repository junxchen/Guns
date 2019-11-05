package cn.stylefeng.guns.modular.demos.service;

import cn.stylefeng.guns.core.common.exception.BizExceptionEnum;
import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.shiro.ShiroUser;
import cn.stylefeng.guns.modular.activiti.service.base.BaseWorkFlowService;
import cn.stylefeng.guns.modular.demos.entity.Leave;
import cn.stylefeng.guns.modular.demos.mapper.LeaveMapper;
import cn.stylefeng.guns.modular.demos.model.LeaveDto;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.RequestEmptyException;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xuyuxiang
 * @name: LeaveService
 * @description: 请假Service
 * @date 2019/10/2917:59
 */
@Service
public class LeaveService extends ServiceImpl<LeaveMapper, Leave>{

    @Autowired
    BaseWorkFlowService baseWorkFlowService;

    /**
     * 查询我的请假审批列表
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 18:12
     **/
    public LayuiPageInfo findPageBySpec(LeaveDto leaveDto) {
        Page pageContext = getPageContext();
        String leaveDays = leaveDto.getLeaveDays();
        String leaveReason = leaveDto.getLeaveReason();
        QueryWrapper<Leave> leaveQueryWrapper = new QueryWrapper<>();

        ShiroUser shiroUser = ShiroKit.getUser();
        if(ToolUtil.isEmpty(shiroUser)){
            throw new ServiceException(BizExceptionEnum.NOT_LOGIN);
        }
        Long userId = shiroUser.getId();

        leaveQueryWrapper.eq("create_user",userId);

        if(ToolUtil.isNotEmpty(leaveDays)){
            leaveQueryWrapper.eq("leave_days",leaveDays);
        }
        if(ToolUtil.isNotEmpty(leaveReason)){
            leaveQueryWrapper.like("leave_reason",leaveReason);
        }
        List<Leave> leaveList = this.list(leaveQueryWrapper);
        baseWorkFlowService.fillFlowEntityListInfo(leaveList);
        int count = this.count(leaveQueryWrapper);
        pageContext.setRecords(leaveList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }

    /**
     * 保存请假信息，并返回实体
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 18:26
     **/
    public Leave add(LeaveDto leaveDto) {
        Leave leave = new Leave();
        ToolUtil.copyProperties(leaveDto,leave);
        this.save(leave);
        return leave;
    }

    /**
     * 修改请假信息，并返回实体
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 18:26
     **/
    public Leave edit(LeaveDto leaveDto) {
        Leave leave = new Leave();
        ToolUtil.copyProperties(leaveDto,leave);
        this.updateById(leave);
        return leave;
    }
    /**
     * 开启请假流程
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 10:32
     **/
    @Transactional(rollbackFor = Exception.class)
    public void start(LeaveDto leaveDto){
        Long leaveId = leaveDto.getLeaveId();
        Leave leave = this.getById(leaveId);
        if(ToolUtil.isEmpty(leave)){
            throw new RequestEmptyException("查不到该条请假内容");
        }
        //获取流程key
        String processKey = leaveDto.getProcessKey();
        //开启流程
        baseWorkFlowService.startProcess(leave,leaveId,processKey);
        this.updateById(leave);
    }

    /**
     * 保存请假信息并开始流程
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 18:26
     **/
    @Transactional(rollbackFor = Exception.class)
    public void addAndStart(LeaveDto leaveDto) {
        Long leaveId = leaveDto.getLeaveId();
        Leave leave = null;
        if(ToolUtil.isEmpty(leaveId)){
            leave = this.add(leaveDto);
        }else{
            leave = this.edit(leaveDto);
        }
        leaveId = leave.getLeaveId();
        leaveDto.setLeaveId(leaveId);
        this.start(leaveDto);
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
     * 删除请假信息
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 14:16
     **/
    public void delete(LeaveDto leaveDto) {
        Long leaveId = leaveDto.getLeaveId();
        this.removeById(leaveId);
    }
}
