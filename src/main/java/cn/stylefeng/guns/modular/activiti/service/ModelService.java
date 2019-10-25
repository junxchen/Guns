package cn.stylefeng.guns.modular.activiti.service;

import cn.hutool.core.bean.BeanUtil;
import cn.stylefeng.guns.core.common.page.LayuiPageFactory;
import cn.stylefeng.guns.core.common.page.LayuiPageInfo;
import cn.stylefeng.guns.modular.activiti.mapper.ModelMapper;
import cn.stylefeng.guns.modular.activiti.model.ActModel;
import cn.stylefeng.guns.modular.activiti.model.ModelDto;
import cn.stylefeng.roses.core.util.ToolUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ModelService extends ServiceImpl<ModelMapper, ActModel> {

    private String EDITOR_NODE_ID = "canvas";
    private String EDITOR_RESOURCE_ID = "canvas";
    private String STENCIL_SET_NODE_NAMESPACE = "http://b3mn.org/stencilset/bpmn2.0#";

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;
    /**
     * 添加模型
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 15:33
     **/
    @Transactional(rollbackFor = Exception.class)
    public void add(ModelDto modelDto) {
        //模型名称
        String name = modelDto.getName();
        //模型键
        String key = modelDto.getKey();
        //模型描述
        String description = modelDto.getDescription();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", EDITOR_NODE_ID);
        editorNode.put("resourceId", EDITOR_RESOURCE_ID);
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", STENCIL_SET_NODE_NAMESPACE);
        editorNode.set("stencilset", stencilSetNode);
        Model modelData = repositoryService.newModel();

        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        description = StringUtils.defaultString(description);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(name);
        modelData.setKey(StringUtils.defaultString(key));
        repositoryService.saveModel(modelData);
    }

    /**
     * 修改模型
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 15:42
     **/
    public void update(ModelDto modelDto) {

    }

    /**
     * 删除模型
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 15:43
     **/
    public void delete(Long modelId) {
    }

    /**
     * 查询模型列表
     *
     * @Author xuyuxiang
     * @Date 2019/10/25 16:03
     **/
    public LayuiPageInfo findPageBySpec(ModelDto modelDto) {
        Page pageContext = getPageContext();
        ModelQuery modelQuery = repositoryService.createModelQuery();
        if (ToolUtil.isNotEmpty(modelDto.getName())) {
            modelQuery.modelNameLike("%" + modelDto.getName() + "%");
        }
        if (ToolUtil.isNotEmpty(modelDto.getKey())) {
            modelQuery.modelKey(modelDto.getKey());
        }
        List<Model> models = modelQuery
                .listPage((int) pageContext.getSize() * ((int) pageContext.getCurrent() - 1), (int) pageContext.getSize());
        long count = repositoryService.createModelQuery().count();
        List<ActModel> list = new ArrayList<>();
        for (Model model:models) {
            ActModel actModel = new ActModel();
            BeanUtil.copyProperties(model,actModel);
            list.add(actModel);
        }
        pageContext.setRecords(list);
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
}
