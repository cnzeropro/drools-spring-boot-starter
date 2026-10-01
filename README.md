# drools-spring-boot-starter

Spring Boot Starter：把 **Drools 规则引擎**接进 Spring Boot —— 规则文件 / 决策表按目录加载、缓存、定时刷新，注入模板即可执行。

## 配置

```yaml
drools:
  paths:              # 规则文件或决策表所在目录（数组、支持通配）
    - classpath:rules
    - file:/opt/rules
  charset: UTF-8      # 规则文件编码
  mode: Cloud         # 模式：stream 或 cloud
  listen: true        # 是否开启监听器
  refresh: true       # 是否自动刷新规则
```

以上均为 `DroolsProperties` 的字段（前缀 `drools`），示例值可与实际不同。

## 用法

注入 `DroolsTemplate`：

```java
@Autowired
private DroolsTemplate droolsTemplate;

KieSession session      = droolsTemplate.decode2Session(ruleContent);          // 由规则内容建会话
KieSessionsPool pool    = droolsTemplate.decode2SessionPool(ruleContent);      // 建会话池
KieSessionsPool pooled  = droolsTemplate.decode2SessionPool(8, ruleContent);   // 指定池大小
KieBase kieBase         = droolsTemplate.getKieBase(ruleContent);
KieSession byFiles      = droolsTemplate.getKieSession("rules/a.drl");         // 按规则文件取会话
List<String> refreshed  = droolsTemplate.refresh();                            // 手动刷新缓存
```

## 组成

| 类 | 作用 |
| --- | --- |
| `DroolsTemplate` | 对外主入口：会话 / 会话池 / KieBase 获取、规则刷新 |
| `DroolsAccessor` | 规则定位与读取（配合 `DroolsFileUtil`） |
| `DroolsSchedule` + `DroolsCacheRefreshTask` | 定时刷新缓存的调度（`ScheduledThreadPoolExecutor`，带拒绝策略） |
| `DroolsProperties` | `drools.*` 配置绑定 |
| `DroolsAutoConfiguration` | 自动装配（经 `spring.factories`） |
| `DroolsConstant` | 默认目录 / 编码 / 模式等常量 |

## 说明

- 面向 Spring Boot 2.x + Drools 7.x
- 规则可放 classpath 也可放外部目录，扫描范围由 `drools.paths` 决定
- 规则更新后由调度任务刷新缓存，也可调用 `refresh()` 手动刷新
