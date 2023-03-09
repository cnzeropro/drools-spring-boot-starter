package org.zero.drools.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.zero.drools.task.DroolsCacheRefreshTask;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Slf4j
@RequiredArgsConstructor
public class DroolsSchedule implements InitializingBean {
    private final DroolsTemplate droolsTemplate;
    private ScheduledThreadPoolExecutor executor;
    private Runnable runnable;
    private Future<?> future;

    @Override
    public void afterPropertiesSet() {
        long update = droolsTemplate.getDroolsProperties().getUpdatePeriod();
        if (update < 10L) {
            log.warn("DroolsProperties.updatePeriod automatic correction to 30s.");
            droolsTemplate.getDroolsProperties().setUpdatePeriod(30L);
        }
        executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2,
                new CustomThreadFactory("rule-schedule-"),
                new ThreadPoolExecutor.AbortPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        throw new RejectedExecutionException("自动装载Drools规则文件异常");
                    }
                });
        runnable = new DroolsCacheRefreshTask(droolsTemplate);
        // 自动注入时执行
        execute();
    }

    public void execute() {
        future = executor.scheduleAtFixedRate(runnable, 1, droolsTemplate.getDroolsProperties().getUpdatePeriod(), TimeUnit.SECONDS);
    }

    public void stop() {
        future.cancel(true);
        executor.remove(runnable);
    }

    public void shutdown() {
        stop();
        executor.shutdown();
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private static final AtomicInteger THREAD_NUMBER = new AtomicInteger(1);

        private final ThreadGroup group;
        private final String namePrefix;

        CustomThreadFactory(String name) {
            SecurityManager securityManager = System.getSecurityManager();
            group = Objects.nonNull(securityManager) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = name + "-pool-" + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + THREAD_NUMBER.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
