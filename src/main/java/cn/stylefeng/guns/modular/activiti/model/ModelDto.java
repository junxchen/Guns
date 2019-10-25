package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xuyuxiang
 * @name: ModelDto
 * @description: 工作流模型信息
 * @date 2019/10/25 15:00
 */
@Data
public class ModelDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型键
     */
    private String key;

    /**
     * 模型描述
     */
    private String description;
}
