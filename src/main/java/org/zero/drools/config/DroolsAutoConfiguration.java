package org.zero.drools.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zero.drools.core.DroolsSchedule;
import org.zero.drools.core.DroolsTemplate;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Configuration
@EnableConfigurationProperties(DroolsProperties.class)
public class DroolsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DroolsTemplate.class)
    public DroolsTemplate droolsTemplate(DroolsProperties droolsProperties) {
        DroolsTemplate droolsTemplate = new DroolsTemplate();
        droolsTemplate.setDroolsProperties(droolsProperties);
        return droolsTemplate;
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "drools.auto", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(DroolsSchedule.class)
    public DroolsSchedule droolsSchedule(DroolsTemplate droolsTemplate) {
        return new DroolsSchedule(droolsTemplate);
    }
}