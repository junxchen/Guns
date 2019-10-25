package cn.stylefeng.guns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 定时任务自动配置
 *
 * @author fengshuonan
 * @Date 2019/2/24 16:23
 */
@Configuration
public class SchedulingConfig {

    @Primary
    @Bean
    public TaskExecutor primaryTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    /**
     * 定时任务执行测试,注意在Application上加@EnableScheduling
     *
     * @author fengshuonan
     * @Date 2019/3/27 2:48 PM
     *//*
    @Bean
    public SpringTasks scheduledTasks() {
        return new SpringTasks();
    }

    *//**
     * quartz方式，配置Scheduler实例
     *
     * @author fengshuonan
     * @Date 2019/2/24 19:03
     *//*
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }

    *//**
     * 启动quartz的示例
     *
     * @author fengshuonan
     * @Date 2019/3/27 3:34 PM
     *//*
    @Bean
    public StartQuartzExample startQuartzExample() {
        return new StartQuartzExample();
    }*/

}
