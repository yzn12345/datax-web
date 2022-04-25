package com.wugui.datax.admin.core.conf;

import com.wugui.datax.admin.core.scheduler.JobScheduler;
import com.wugui.datax.admin.mapper.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * xxl-job config
 * JobAdminConfig是 admin 的核心配置类，此处开始初始化调度器、读取admin的application.yml
 *  @Value读取application.yml中的配置加载到程序中。
 *  InitialingBean是一个接口，提供了一个唯一的方法afterPropertiesSet()。
 *  DisposableBean也是一个接口，提供了一个唯一的方法destory()。
 * @author xuxueli 2017-04-28
 */

@Component
public class JobAdminConfig implements InitializingBean, DisposableBean {

    private static JobAdminConfig adminConfig = null;

    public static JobAdminConfig getAdminConfig() {
        return adminConfig;
    }


    // ---------------------- XxlJobScheduler ----------------------

    private JobScheduler xxlJobScheduler;

    //  1.实现了 InitializingBean 接口，初始化完成后会调用此方法
    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
        xxlJobScheduler = new JobScheduler();
        //2.初始化调度器
        xxlJobScheduler.init();
    }

    @Override
    public void destroy() throws Exception {
        xxlJobScheduler.destroy();
    }


    // ---------------------- XxlJobScheduler ----------------------

    // conf
    @Value("${datax.job.i18n}")
    private String i18n;

    @Value("${datax.job.accessToken}")
    private String accessToken;

    @Value("${spring.mail.username}")
    private String emailUserName;

    @Value("${datax.job.triggerpool.fast.max}")
    private int triggerPoolFastMax;

    @Value("${datax.job.triggerpool.slow.max}")
    private int triggerPoolSlowMax;

    @Value("${datax.job.logretentiondays}")
    private int logretentiondays;

    @Value("${datasource.aes.key}")
    private String dataSourceAESKey;

    // dao, service

    @Resource
    private JobLogMapper jobLogMapper;
    @Resource
    private JobInfoMapper jobInfoMapper;
    @Resource
    private JobRegistryMapper jobRegistryMapper;
    @Resource
    private JobGroupMapper jobGroupMapper;
    @Resource
    private JobLogReportMapper jobLogReportMapper;
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private DataSource dataSource;
    @Resource
    private JobDatasourceMapper jobDatasourceMapper;

    public String getI18n() {
        return i18n;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getEmailUserName() {
        return emailUserName;
    }

    public int getTriggerPoolFastMax() {
        return triggerPoolFastMax < 200 ? 200 : triggerPoolFastMax;
    }

    public int getTriggerPoolSlowMax() {
        return triggerPoolSlowMax < 100 ? 100 : triggerPoolSlowMax;
    }

    public int getLogretentiondays() {
        return logretentiondays < 7 ? -1 : logretentiondays;
    }

    public JobLogMapper getJobLogMapper() {
        return jobLogMapper;
    }

    public JobInfoMapper getJobInfoMapper() {
        return jobInfoMapper;
    }

    public JobRegistryMapper getJobRegistryMapper() {
        return jobRegistryMapper;
    }

    public JobGroupMapper getJobGroupMapper() {
        return jobGroupMapper;
    }

    public JobLogReportMapper getJobLogReportMapper() {
        return jobLogReportMapper;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public JobDatasourceMapper getJobDatasourceMapper() {
        return jobDatasourceMapper;
    }

    public String getDataSourceAESKey() {
        return dataSourceAESKey;
    }

    public void setDataSourceAESKey(String dataSourceAESKey) {
        this.dataSourceAESKey = dataSourceAESKey;
    }
}
