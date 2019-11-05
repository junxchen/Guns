package cn.stylefeng.guns.modular.demos.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.stylefeng.guns.core.common.annotion.Permission;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.demos.entity.Leave;
import cn.stylefeng.guns.modular.demos.model.LeaveDto;
import cn.stylefeng.guns.modular.demos.service.LeaveService;
import cn.stylefeng.roses.core.base.controller.BaseController;
import cn.stylefeng.roses.core.reqres.response.ResponseData;
import cn.stylefeng.roses.core.util.ToolUtil;
import cn.stylefeng.roses.kernel.model.exception.RequestEmptyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xuyuxiang
 * @name: LeaveController
 * @description: 请假控制器
 * @date 2019/10/2917:57
 */
@Controller
@RequestMapping("/leave")
public class LeaveController extends BaseController {
    private String PREFIX = "/modular/demos/leave/";

    @Autowired
    LeaveService leaveService;

    /**
     * 请假列表页面
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:40
     **/
    @RequestMapping("")
    public String index() {
        return PREFIX + "/leave.html";
    }

    /**
     * 请假申请页面
     *
     * @author stylefeng
     * @Date 2019-03-13
     */
    @RequestMapping("/add")
    public String add() {
        return PREFIX + "/leave_add.html";
    }

    /**
     * 修改请假申请页面
     *
     * @author stylefeng
     * @Date 2019-03-13
     */
    @RequestMapping("/edit")
    public String edit(@RequestParam("leaveId") Long leaveId) {
        if(ToolUtil.isEmpty(leaveId)){
            throw new RequestEmptyException("请假id为空");
        }
        return PREFIX + "/leave_edit.html";
    }

    /**
     * 调整请假申请页面
     *
     * @author stylefeng
     * @Date 2019-03-13
     */
    @RequestMapping("/change")
    public String change(@RequestParam("leaveId") Long leaveId) {
        if(ToolUtil.isEmpty(leaveId)){
            throw new RequestEmptyException("请假id为空");
        }
        return PREFIX + "/leave_change.html";
    }

    /**
     * 请假表单预览页
     *
     * @Author xuyuxiang
     * @Date 2019/11/1 14:51
     **/
    @RequestMapping("/view")
    public String view(@RequestParam("leaveId") Long leaveId) {
        if(ToolUtil.isEmpty(leaveId)){
            throw new RequestEmptyException("请假id为空");
        }
        return PREFIX + "/leave_view.html";
    }

    /**
     * 保存请假信息接口
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/getItem")
    @ResponseBody
    public ResponseData getItem(@RequestParam("leaveId") Long leaveId) {
        Leave leave = this.leaveService.getById(leaveId);
        LeaveDto leaveDto = new LeaveDto();
        BeanUtil.copyProperties(leave,leaveDto);
        return ResponseData.success(leaveDto);
    }

    /**
     * 保存请假信息接口
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/addItem")
    @ResponseBody
    public ResponseData addItem(LeaveDto leaveDto) {
        this.leaveService.add(leaveDto);;
        return ResponseData.success();
    }

    /**
     * 修改请假信息接口
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/editItem")
    @ResponseBody
    public ResponseData editItem(LeaveDto leaveDto) {
        this.leaveService.edit(leaveDto);
        return ResponseData.success();
    }

    /**
     * 删除请假信息
     *
     * @Author xuyuxiang
     * @Date 2019/10/30 14:15
     **/
    @Permission
    @RequestMapping("/delete")
    @ResponseBody
    public ResponseData delete(LeaveDto leaveDto) {
        this.leaveService.delete(leaveDto);
        return ResponseData.success();
    }

    /**
     * 开启请假流程
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/startItem")
    @ResponseBody
    public ResponseData startItem(LeaveDto leaveDto) {
        this.leaveService.start(leaveDto);;
        return ResponseData.success();
    }

    /**
     * 保存请假信息并发起流程接口
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 14:56
     **/
    @Permission
    @RequestMapping("/addAndStartItem")
    @ResponseBody
    public ResponseData addAndStart(LeaveDto leaveDto) {
        this.leaveService.addAndStart(leaveDto);;
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
    public LayuiPageInfo list(LeaveDto leaveDto) {
        return this.leaveService.findPageBySpec(leaveDto);
    }

}
