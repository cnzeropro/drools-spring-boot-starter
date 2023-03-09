package org.zero.drools.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ObjectUtils;
import org.zero.drools.config.DroolsProperties;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Setter
@Getter
@Slf4j
public abstract class DroolsAccessor implements InitializingBean {
    private DroolsProperties droolsProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (ObjectUtils.isEmpty(droolsProperties.getPaths())) {
            log.warn("Please set base paths (drools.paths = xxx)");
        }
    }
}
