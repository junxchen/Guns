package cn.stylefeng.guns.modular.activiti.controller;

import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.modular.activiti.model.ProcessDefinition;
import cn.stylefeng.guns.modular.activiti.service.ProcessService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.page.PageFactory;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


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

    private String PREFIX = "/modular/activiti/process/";


    @RequestMapping("")
    public String index(){
        return PREFIX+"process.html";
    }

    /**
     * @author wangjinqian
     * @methodName showAct
     * @description 查询部署的流程列表
     * @date 2019/10/28 10:47
     */
    @RequestMapping("/list")
    @ResponseBody
    public Object showAct(ProcessDefinition definition){
        return LayuiPageFactory.createPageInfo(processService.showAct(definition));
    }

    /**
     * @author wangjinqian
     * @methodName deleteAct
     * @description 删除部署的流程
     * @date 2019/10/28 11:09
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Object deleteAct(String deploymentId){
        processService.deleteAct(deploymentId);
        return ResponseData.success();
    }

}


