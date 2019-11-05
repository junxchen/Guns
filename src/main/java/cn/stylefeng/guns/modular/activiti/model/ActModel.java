package cn.stylefeng.guns.modular.activiti.model;

import lombok.Data;

import java.util.Date;

/**
 * @author xuyuxiang
 * @name: ActModel
 * @description: 模型model
 * @date 2019/10/2516:08
 */
@Data
public class ActModel {
    private static final long serialVersionUID = 1L;

    /**
     * 模型id
     */
    private String id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型键值
     */
    private String key;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上次更新时间
     */
    private Date lastUpdateTime;

    /**
     * 版本
     */
    private Integer version;

    /**
     * metaInfo
     */
    private String metaInfo;

    /**
     * 部署id
     */
    private String deploymentId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 是否有资源
     */
    private boolean hasEditorSource;
}
