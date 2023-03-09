package org.zero.drools.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zero.drools.core.DroolsTemplate;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Slf4j
@AllArgsConstructor
public class DroolsCacheRefreshTask implements Runnable {
    private final DroolsTemplate kieTemplate;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Start updating the rules file.");
        }
        kieTemplate.refresh();
        if (log.isDebugEnabled()) {
            log.debug("The rules file was updated successfully.");
        }
    }
}
