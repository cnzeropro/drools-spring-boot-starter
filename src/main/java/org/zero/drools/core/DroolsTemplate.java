package org.zero.drools.core;

import lombok.extern.slf4j.Slf4j;
import org.drools.core.event.DefaultProcessEventListener;
import org.kie.api.KieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionsPool;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.zero.drools.util.DroolsFileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Slf4j
public class DroolsTemplate extends DroolsAccessor implements BeanClassLoaderAware {
    private static final int DEFAULT_POOL_SIZE = 10;

    private ClassLoader classLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (Objects.isNull(classLoader)) {
            classLoader = getClass().getClassLoader();
        }

        throw new IllegalArgumentException("Please set base paths (drools.paths = xxx)");
    }

    /**
     * 根据文件名获取KieSession
     */
    public KieSession getKieSession(@NonNull String... ruleFilePaths) {
        List<String> ruleContents = new ArrayList<>();
        for (String ruleFilePath : ruleFilePaths) {
            String ruleContent = DroolsFileUtil.get(ruleFilePath);
            if (!StringUtils.hasText(ruleContent)) {
                ruleContents.addAll(DroolsFileUtil.reload(new String[]{ruleFilePath}, getDroolsProperties().getCharset()));
            }
            ruleContents.add(ruleContent);
        }
        return decode2Session(ruleContents.toArray(new String[0]));
    }

    /**
     * 把规则字符串解析成KieSessionPool
     */
    public KieSessionsPool decode2SessionPool(@NonNull String... ruleContents) {
        return decode2SessionPool(DEFAULT_POOL_SIZE, ruleContents);
    }

    /**
     * 把规则字符串解析成KieSessionPool
     */
    public KieSessionsPool decode2SessionPool(int poolSize, @NonNull String... ruleContents) {
        KieBase kieBase = getKieBase(ruleContents);
        return kieBase.newKieSessionsPool(poolSize);
    }

    /**
     * 把规则字符串解析成KieSession
     */
    public KieSession decode2Session(@NonNull String... ruleContents) {
        KieBase kieBase = getKieBase(ruleContents);
        KieSession kieSession = kieBase.newKieSession();
        if (getDroolsProperties().isListen()) {
            kieSession.addEventListener(new DefaultRuleRuntimeEventListener());
            kieSession.addEventListener(new DefaultAgendaEventListener());
            kieSession.addEventListener(new DefaultProcessEventListener());
        }
        return kieSession;
    }

    /**
     * 获取KieBase
     */
    public KieBase getKieBase(@NonNull String... ruleContents) {
        KieHelper kieHelper = new KieHelper();

        for (String ruleContent : ruleContents) {
            kieHelper.addContent(ruleContent, ResourceType.DRL);
        }

        if (getDroolsProperties().isVerify()) {
            Results results = kieHelper.verify();
            List<Message> messages = results.getMessages(Message.Level.WARNING, Message.Level.ERROR);
            if (!CollectionUtils.isEmpty(messages)) {
                messages.forEach(message -> log.error("Error: {}", message.getText()));
                throw new IllegalStateException("Compilation errors");
            }
        }

        KieBaseConfiguration config = kieHelper.ks.newKieBaseConfiguration();
        String mode = getDroolsProperties().getMode();
        if (EventProcessingOption.STREAM.getMode().equalsIgnoreCase(mode)) {
            config.setOption(EventProcessingOption.STREAM);
        } else if (EventProcessingOption.CLOUD.getMode().equalsIgnoreCase(mode)) {
            config.setOption(EventProcessingOption.CLOUD);
        } else {
            throw new UnsupportedOperationException("Unsupported mode");
        }
        return kieHelper.build(config);
    }

    /**
     * 内部使用
     * 刷新系统定义的所有规则文件内容到缓存
     */
    public List<String> refresh() {
        String[] paths = getDroolsProperties().getPaths();
        if (ObjectUtils.isEmpty(paths)) {
            throw new IllegalArgumentException("Please set base paths (drools.paths = xxx)");
        }

        return DroolsFileUtil.reload(paths, getDroolsProperties().getCharset());
    }
}
