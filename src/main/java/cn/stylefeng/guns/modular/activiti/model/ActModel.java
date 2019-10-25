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
    private String id;
    private String name;
    private String key;
    private String category;
    private Date createTime;
    private Date lastUpdateTime;
    private Integer version;
    private String metaInfo;
    private String deploymentId;
    private String tenantId;
    private boolean hasEditorSource;
}
