package cn.stylefeng.guns.config;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.stereotype.Component;

/**
 * @author xuyuxiang
 * @name: ProcessEngineConfiguration
 * @description: 流程引擎配置
 * @date 2019/10/2218:03
 */
@Component
public class ProcessEngineConfiguration implements ProcessEngineConfigurationConfigurer {

    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        springProcessEngineConfiguration.setActivityFontName("宋体");
        springProcessEngineConfiguration.setLabelFontName("宋体");
        springProcessEngineConfiguration.setAnnotationFontName("宋体");
        springProcessEngineConfiguration.setDbIdentityUsed(false);
        springProcessEngineConfiguration.setDatabaseType("mysql");
        springProcessEngineConfiguration.setDatabaseSchemaUpdate("true");
    }
}
