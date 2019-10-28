package cn.stylefeng.guns.modular.activiti.service;

import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.modular.activiti.model.ProcessDefinition;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    /**
     * @author wangjinqian
     * @methodName showAct
     * @description 获取部署的流程列表
     * @date 2019/10/28 10:14
     * @param definition
     */
    public Page showAct(ProcessDefinition definition) {
        //获取分页参数
        Page page = LayuiPageFactory.defaultPage();


        ProcessDefinitionQuery processDefinitionQuery = repositoryService
                .createProcessDefinitionQuery();
        List<org.activiti.engine.repository.ProcessDefinition> processDefinitionList = null;
        //设置查询参数
        if (definition != null) {
            if (!StringUtils.isEmpty(definition.getDeploymentId())) {
                processDefinitionQuery.deploymentId(definition.getDeploymentId());
            }
            if (!StringUtils.isEmpty(definition.getName())) {
                processDefinitionQuery.processDefinitionNameLike("%" + definition.getName() + "%");
            }
            if (!StringUtils.isEmpty(definition.getKey())) {
                processDefinitionQuery.processDefinitionKeyLike("%" + definition.getKey() + "%");
            }
        }
        //获取流程列表
        processDefinitionList = processDefinitionQuery.listPage((int)page.getSize() * ((int)page.getCurrent() - 1), (int)page.getSize());
        List<ProcessDefinition> list = new ArrayList<>();
        processDefinitionList
                .forEach(processDefinition -> list.add(new ProcessDefinition(processDefinition)));
        //设置分页数据
        long count = repositoryService.createProcessDefinitionQuery().count();
        page.setRecords(list);
        page.setTotal(count);
        return page;
    }

    /**
     * @author wangjinqian
     * @methodName deleteAct
     * @description 删除部署的流程
     * @date 2019/10/28 11:10
     */
    public void deleteAct(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }
}
