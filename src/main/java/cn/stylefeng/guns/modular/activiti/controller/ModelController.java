package cn.stylefeng.guns.modular.activiti.controller;

import cn.stylefeng.guns.core.common.annotion.Permission;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.activiti.service.ModelService;
import cn.stylefeng.guns.modular.activiti.model.ModelDto;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * activiti模型控制器
 *
 * @Author xuyuxiang
 * @Date 2019/10/25 14:40
 **/
@Controller
@RequestMapping("/model")
public class ModelController extends BaseController {

    private String PREFIX = "/modular/activiti/model";

    @Autowired
    ModelService modelService;

   /**
    * 模型列表页面
    *
    * @Author xuyuxiang
    * @Date 2019/10/25 14:40
    **/
    @RequestMapping("")
    public String index() {
        return PREFIX + "/model.html";
    }

    /**
     * 创建模型页面
     *
     * @author stylefeng
     * @Date 2019-03-13
     */
    @RequestMapping("/add")
    public String add() {
        return PREFIX + "/model_add.html";
    }

    /**
     * 创建模型接口
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/addItem")
    @ResponseBody
    public ResponseData addItem(ModelDto modelDto) {
        String rebackUrl = this.modelService.add(modelDto);;
        return ResponseData.success(rebackUrl);
    }

    /**
     * 删除接口
     *
     * @author stylefeng
     * @Date 2019-03-13
     */
    @Permission
    @RequestMapping("/delete")
    @ResponseBody
    public ResponseData delete(@RequestParam String modelId) {
        this.modelService.delete(modelId);
        return ResponseData.success();
    }

    /**
     * 部署
     *
     * @Author xuyuxiang
     * @Date 2019/10/28 15:17
     **/
    @Permission
    @RequestMapping("/deploy")
    @ResponseBody
    public ResponseData deploy(@RequestParam String modelId) {
        this.modelService.deploy(modelId);
        return ResponseData.success();
    }

   /**
    * 查询列表
    *
    * @Author xuyuxiang
    * @Date 2019/10/25 15:39
    **/
    @Permission
    @ResponseBody
    @RequestMapping("/list")
    public LayuiPageInfo list(ModelDto modelDto) {
        return this.modelService.findPageBySpec(modelDto);
    }

}


