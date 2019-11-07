package cn.stylefeng.guns.modular.activiti.controller;

import cn.stylefeng.guns.core.common.annotion.Permission;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.activiti.model.ApproveDto;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.activiti.service.TaskService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author xuyuxiang
 * @name: TaskController
 * @description: 流程任务控制器
 * @date 2019/10/3017:14
 */
@Controller
@RequestMapping("/task")
public class TaskController extends BaseController {

    @Autowired
    private TaskService taskService;

    private String PREFIX = "/modular/activiti/task";

    /**
     * 查看任务页面
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 15:13
     **/
    @RequestMapping("/view")
    public String view() {
        return PREFIX + "/task_view.html";
    }

    /**
     * 我的待办任务首页
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 17:17
     **/
    @RequestMapping("/todoTaskHome")
    public String todoTaskHome(){
        return PREFIX+"/task_todo.html";
    }

    /**
     * 我的已办任务首页
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 17:17
     **/
    @RequestMapping("/doneTaskHome")
    public String doneTaskHome(){
        return PREFIX+"/task_done.html";
    }

    /**
     * 候选人列表页面
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 17:17
     **/
    @RequestMapping("/candidate")
    public String candidateUser(){
        return PREFIX+"/task_candidate.html";
    }

    /** 
     * 候选人列表接口
     * 
     * @Author xuyuxiang
     * @Date 2019/11/6 15:23
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/candidateList")
    public LayuiPageInfo candidateList(@RequestParam String processInstanceId) {
        return this.taskService.candidateList(processInstanceId);
    }

    /**
     * 查询我的待办任务列表
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 15:39
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/todoList")
    public LayuiPageInfo todoList(TaskDto taskDto) {
        return this.taskService.findTodoPageBySpec(taskDto);
    }

    /**
     * 查询我的已办任务列表
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 15:39
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/doneList")
    public LayuiPageInfo doneList(TaskDto taskDto) {
        return this.taskService.findDonePageBySpec(taskDto);
    }

    /**
     * 查看任务详情
     *
     * @Author xuyuxiang
     * @Date 2019/10/31 18:26
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/viewTaskDetail")
    public ResponseData viewTaskDetail(@RequestParam String processInstanceId) {
        Map<String,Object> resultMap = this.taskService.viewTaskDetail(processInstanceId);
        return ResponseData.success(resultMap);
    }

    /**
     * 完成审批
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:25
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/doneTask")
    public ResponseData doneTask(ApproveDto approveDto) {
        this.taskService.doneTask(approveDto);
        return ResponseData.success();
    }

    /**
     * 挂起任务
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:25
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/suspend")
    public ResponseData suspend(@RequestParam String processInstanceId) {
        this.taskService.suspend(processInstanceId);
        return ResponseData.success();
    }

    /**
     * 激活任务
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:25
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/active")
    public ResponseData active(@RequestParam String processInstanceId) {
        this.taskService.active(processInstanceId);
        return ResponseData.success();
    }

    /**
     * 委托他人审核
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 14:25
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/changeAssignee")
    public ResponseData changeAssignee(@RequestParam String processInstanceId,
                                       @RequestParam String taskAssignee) {
        this.taskService.changeAssignee(processInstanceId,taskAssignee);
        return ResponseData.success();
    }
}
