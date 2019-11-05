package cn.stylefeng.guns.modular.activiti.service.base;

import cn.hutool.core.date.DateUtil;
import cn.stylefeng.guns.core.common.exception.BizExceptionEnum;
import cn.stylefeng.guns.core.shiro.ShiroKit;
import cn.stylefeng.guns.core.shiro.ShiroUser;
import cn.stylefeng.guns.modular.activiti.entity.BaseWorkFlowEntity;
import cn.stylefeng.guns.modular.activiti.service.HistoryService;
import cn.stylefeng.guns.modular.activiti.service.TaskService;
import cn.stylefeng.guns.modular.demos.entity.Leave;
import cn.stylefeng.guns.modular.system.service.UserService;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.ServiceException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xuyuxiang
 * @name: WorkFlowBaseService
 * @description: 工作流基础service
 * @date 2019/10/2918:19
 */
@Service
public class BaseWorkFlowService<T extends BaseWorkFlowEntity>{

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private org.activiti.engine.TaskService actTaskService;

    @Autowired
    private TaskService taskService;

    @Autowired
    IdentityService identityService;

    @Autowired
    HistoryService historyService;

    @Autowired
    private UserService userService;

    /**
     * 开启流程
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 18:31
     **/
    @Transactional(rollbackFor = Exception.class)
    public void startProcess(T entity, Long businessKey, String processKey){

        ShiroUser shiroUser = ShiroKit.getUser();
        if(ToolUtil.isEmpty(shiroUser)){
            throw new ServiceException(BizExceptionEnum.NOT_LOGIN);
        }
        Long userId = shiroUser.getId();

        //填充申请人id
        entity.setApplyUserId(userId);
        //填充申请时间
        entity.setApplyDate(DateUtil.now());
        //将申请人姓名和申请时间作为参数传入流程实体
        String applyUserName = shiroUser.getName();
        String applyDate = entity.getApplyDate();
        //保存发起人信息
        identityService.setAuthenticatedUserId(String.valueOf(userId));
        //保存启动流程参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("applyUserId",userId);
        paramMap.put("applyUserName",applyUserName);
        paramMap.put("applyDate",applyDate);
        paramMap.put("entity",entity);
        //开启流程，传入业务id作为businessKey
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, String.valueOf(businessKey),paramMap);
        //获取实例id
        String processInstanceId = processInstance.getId();
        //保存启动流程信息
        historyService.saveStartEventApproveInfo(processInstanceId);
        //保存实例id
        entity.setInstanceId(processInstanceId);
    }

    /**
     * 单填充流程模型字段
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 10:47
     **/
    public void fillFlowEntityInfo(BaseWorkFlowEntity baseWorkFlowEntity){
        String instanceId = baseWorkFlowEntity.getInstanceId();
        if(ToolUtil.isEmpty(instanceId)){
            baseWorkFlowEntity.setApplyUserName("-");
            baseWorkFlowEntity.setApplyDate("-");
            baseWorkFlowEntity.setApproveProcess("-");
            baseWorkFlowEntity.setApproveUserName("-");
            baseWorkFlowEntity.setApproveStatus(0);
        }else{
            Long applyUserId = baseWorkFlowEntity.getApplyUserId();
            String applyUserName = userService.getById(applyUserId).getName();
            baseWorkFlowEntity.setApplyUserName(applyUserName);
            //获取实例
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            //运行中
            if (ToolUtil.isNotEmpty(instance)) {
                Task task = actTaskService.createTaskQuery().processInstanceId(instanceId).singleResult();
                //当前审批环节
                String taskName = task.getName();
                //当前审批人
                String taskAssignee = task.getAssignee();
                String taskAssigneeUserName = userService.getById(taskAssignee).getName();
                //当前审批状态
                baseWorkFlowEntity.setApproveProcess(taskName);
                baseWorkFlowEntity.setApproveUserName(taskAssigneeUserName);
                baseWorkFlowEntity.setApproveStatus(1);
            }else{//已结束
                baseWorkFlowEntity.setApproveProcess("-");
                baseWorkFlowEntity.setApproveUserName("-");
                baseWorkFlowEntity.setApproveStatus(2);
            }
        }
    }

    /**
     * 批量填充工作流模型字段
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 11:02
     **/
    public void fillFlowEntityListInfo(Collection<? extends BaseWorkFlowEntity> baseWorkFlowEntityList){
        for (BaseWorkFlowEntity baseWorkFlowEntity:baseWorkFlowEntityList) {
            this.fillFlowEntityInfo(baseWorkFlowEntity);
        }
    }

    /**
     * 重新申请或取消申请
     *
     * @Author xuyuxiang
     * @Date 2019/11/5 16:40
     **/
    public void reStartOrCancelRequire(Leave leave, Integer approveOperate) {
        taskService.reStartOrCancelRequire(leave,approveOperate);
    }
}
