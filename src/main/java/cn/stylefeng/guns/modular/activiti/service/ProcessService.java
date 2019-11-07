package cn.stylefeng.guns.modular.activiti.service;

import cn.hutool.core.io.IoUtil;
import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.activiti.model.ProcessModel;
import cn.stylefeng.guns.modular.activiti.model.TaskDto;
import cn.stylefeng.guns.modular.activiti.model.TaskModel;
import cn.stylefeng.roses.core.util.ToolUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色和菜单关联表 服务实现类
 * </p>
 *
 * @author stylefeng
 * @since 2018-12-07
 */
@Service
public class ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    /**
     * @author wangjinqian
     * @methodName showAct
     * @description 获取部署的流程列表
     * @date 2019/10/28 10:14
     * @param processModel
     */
    public LayuiPageInfo list(ProcessModel processModel) {
        Page pageContext = getPageContext();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        //设置查询参数
        if (ToolUtil.isNotEmpty(processModel)) {
            String deploymentId = processModel.getDeploymentId();
            if (ToolUtil.isNotEmpty(deploymentId)) {
                processDefinitionQuery.deploymentId(deploymentId);
            }
            String processModelName = processModel.getName();
            if (ToolUtil.isNotEmpty(processModelName)) {
                processDefinitionQuery.processDefinitionNameLike("%" + processModelName + "%");
            }
            String processModelKey = processModel.getKey();
            if (ToolUtil.isNotEmpty(processModelKey)) {
                processDefinitionQuery.processDefinitionKeyLike("%" + processModelKey + "%");
            }
        }
        processDefinitionQuery.latestVersion();
        //获取流程列表
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1),
                (int) pageContext.getSize());
        List<ProcessModel> list = new ArrayList<>();
        processDefinitionList.forEach(processDefinition -> list.add(new ProcessModel(processDefinition)));
        //设置分页数据
        long count = processDefinitionQuery.count();
        pageContext.setRecords(list);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }

    /**
     * @author wangjinqian
     * @methodName deleteAct
     * @description 删除部署的流程
     * @date 2019/10/28 11:10
     */
    public void delete(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    /**
     * 查看已部署模型流程图
     *
     * @Author xuyuxiang
     * @Date 2019/10/29 14:40
     **/
    public void viewPic(String deploymentId,String imageName,HttpServletResponse response) {
        InputStream in = repositoryService.getResourceAsStream(deploymentId,imageName);
        try {
            OutputStream out = response.getOutputStream();
            IoUtil.copy(in,out);
            // 关闭流
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 流程监控
     *
     * @Author xuyuxiang
     * @Date 2019/11/4 20:23
     **/
    public LayuiPageInfo monitorProcess(TaskDto taskDto) {
        Page pageContext = getPageContext();
        List<HistoricTaskInstance> taskList = historyService.getMonitorTaskInstanceList(taskDto,pageContext);
        long count = historyService.countMonitorTaskInstance(taskDto);
        //将任务转换成TaskModel
        List<TaskModel> taskModelList = taskService.copyMonitorTaskListInfo(taskList);
        pageContext.setRecords(taskModelList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
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
     * 查询流程版本历史
     *
     * @Author xuyuxiang
     * @Date 2019/11/6 10:59
     **/
    public LayuiPageInfo versionHistory(ProcessModel processModel) {
        Page pageContext = getPageContext();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        //设置查询参数
        if (ToolUtil.isNotEmpty(processModel)) {
            String processModelName = processModel.getName();
            if (ToolUtil.isNotEmpty(processModelName)) {
                processDefinitionQuery.processDefinitionNameLike("%" + processModelName + "%");
            }
            String processModelKey = processModel.getKey();
            processDefinitionQuery.processDefinitionKey(processModelKey);
        }
        processDefinitionQuery.orderByProcessDefinitionVersion().desc();
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1),
                (int) pageContext.getSize());
        List<ProcessModel> processModelList = new ArrayList<>();
        processDefinitionList.forEach(processDefinition -> processModelList.add(new ProcessModel(processDefinition)));
        //设置分页数据
        long count = processDefinitionQuery.count();
        pageContext.setRecords(processModelList);
        pageContext.setTotal(count);
        return LayuiPageFactory.createPageInfo(pageContext);
    }
}
