package org.zero.drools.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Zero (cnzeropro@qq.com)
 * @since 2022/9/19
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "drools")
public class DroolsProperties {
    /**
     * 规则文件或决策表目录
     */
    private String[] paths;

    /**
     * 规则文件编码类型
     */
    private String charset = "UTF-8";

    /**
     * 模式：stream 或 cloud
     */
    private String mode = "Cloud";

    /**
     * 是否开启监听器
     */
    private boolean listen = true;

    /**
     * 是否自动更新
     */
    private boolean refresh = true;

    /**
     * 更新周期（单位：秒）
     * 请勿配置小于10秒，否则自动修正到30秒
     */
    private long updatePeriod = 30L;

    /**
     * 是否开启规则验证
     */
    private boolean verify = true;
}
