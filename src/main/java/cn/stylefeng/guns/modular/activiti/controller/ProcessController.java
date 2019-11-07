package cn.stylefeng.guns.modular.activiti.controller;

import cn.stylefeng.guns.core.common.annotion.Permission;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.activiti.model.ProcessModel;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.activiti.service.ProcessService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;


/**
 * activiti流程控制器
 *
 * @Author xuyuxiang
 * @Date 2019/10/25 14:40
 **/
@Controller
@RequestMapping("/process")
public class ProcessController extends BaseController {

    @Autowired
    private ProcessService processService;

    private String PREFIX = "/modular/activiti/process";


    @RequestMapping("")
    public String index(){
        return PREFIX+"/process.html";
    }

    /**
     * 跳转到流程监控页面
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 20:15
     **/
    @RequestMapping("/monitor")
    public String monitor(){
        return PREFIX+"/process_monitor.html";
    }

    /**
     * 跳转到版本列表页面
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 20:15
     **/
    @RequestMapping("/version")
    public String version(){
        return PREFIX+"/process_version.html";
    }

    /**
     * @author wangjinqian
     * @methodName list
     * @description 查询部署的流程列表(最新版本)
     * @date 2019/10/28 10:47
     */
    @Permission
    @RequestMapping("/list")
    @ResponseBody
    public LayuiPageInfo list(ProcessModel processModel){
        return processService.list(processModel);
    }

    @Permission
    @RequestMapping("/versionHistory")
    @ResponseBody
    public LayuiPageInfo versionHistory(ProcessModel processModel){
        return processService.versionHistory(processModel);
    }

    /**
     * @author wangjinqian
     * @methodName deleteAct
     * @description 删除部署的流程
     * @date 2019/10/28 11:09
     */
    @Permission
    @RequestMapping("/delete")
    @ResponseBody
    public ResponseData delete(@RequestParam String deploymentId){
        processService.delete(deploymentId);
        return ResponseData.success();
    }

    /**
     * 查看已部署模型流程图
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 14:41
     **/
    @Permission
    @RequestMapping("/viewPic")
    @ResponseBody
    public void viewPic(@RequestParam String deploymentId,
                        @RequestParam String imageName,
                        HttpServletResponse response){
        processService.viewPic(deploymentId,imageName,response);
    }

    /**
     * 流程监控
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 20:16
     **/
    @Permission
    @ResponseBody
    @RequestMapping("/monitorProcess")
    public LayuiPageInfo monitorProcess(TaskDto taskDto) {
        return this.processService.monitorProcess(taskDto);
    }


}


