* * *

# ElasticExecutor - 动态线程池治理框架

**中文** | [English](./README_EN.md)

**ElasticExecutor** 是一个轻量级、高扩展性的 Java 动态线程池治理框架。它允许开发者在不重启应用的情况下，通过 Nacos 配置中心动态调整线程池的核心参数（核心线程数、最大线程数、队列容量等），并提供实时的变更日志监控。

> **核心目标**：解决传统线程池参数调优困难、故障排查滞后的痛点，实现线程池的“热插拔”式管理。

* * *

## ✨ 核心特性 (Features)

-   **⚡ 动态调参**：支持运行时动态修改 `corePoolSize`、`maximumPoolSize`、`queueCapacity` 等核心参数，实时生效。

-   **🔌 无感接入**：基于 Spring Boot Starter 设计，引入依赖并配置 Nacos 即可使用，零代码侵入。

-   **🛡️ 变更监控**：提供可视化的配置变更日志，清晰记录参数的 "旧值 ➜ 新值" 变化，便于回溯。

-   **⚙️ 兼容性强**：

    -   完美适配 **Spring Boot 3.x** (支持 `spring.config.import` 机制)。
    -   完美适配 **Nacos 2.x** (基于 gRPC 长连接)。
    -   **配置解析增强**：支持 YAML 嵌套结构与 Properties 平铺结构的自动识别与绑定（解决了 Spring Binder 的 Map 转换痛点）。

-   **📊 监控埋点**（开发中）：集成 Micrometer，支持 Prometheus/Grafana 实时监控线程池运行指标。

* * *

## 🛠️ 架构设计 (Architecture)

1.  **启动阶段**：自动扫描并注册 `ElasticExecutorProperties` 配置类。

1.  **监听阶段**：利用 Nacos Config Service 监听远程配置文件的变更。

1.  **解析阶段**：

    -   使用 `YamlPropertiesFactoryBean` 将复杂的 YAML 嵌套结构扁平化。
    -   利用 Spring `Binder` API 进行强类型校验与绑定。

1.  **执行阶段**：

    -   计算配置差异 (Diff)。
    -   优雅更新 JDK `ThreadPoolExecutor` 实例参数（处理 Core/Max 更新顺序问题）。
    -   输出变更日志。

* * *

## 🚀 快速开始 (Quick Start)

### 1. 引入依赖

在你的 Spring Boot 项目 `pom.xml` 中添加 `elastic-executor-spring-boot-starter`：

XML

```
<dependency>
    <groupId>org.puregxl</groupId>
    <artifactId>elastic-executor-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 项目配置

在 `application.yml` 中配置 Nacos 地址及动态线程池规则。

**注意**：Spring Boot 3.x 需使用 `config.import` 语法。

YAML

```
spring:
  application:
    name: elastic-demo
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yaml
  config:
    import:
      - optional:nacos:example-thread-pool.yaml  # 导入动态配置 data-id

# 本地默认配置（可选）
elastic-executor:
  enable: true
  monitor:
    enable: true
    collect-interval: 10
```

### 3. Nacos 动态配置

在 Nacos 控制台新建配置 `example-thread-pool.yaml`：

YAML

```
elastic-executor:
  executors:
    - thread-pool-id: order-service-executor  # 线程池唯一标识
      core-pool-size: 10
      maximum-pool-size: 20
      queue-capacity: 1024
      keep-alive-time: 60
      blocking-queue: LinkedBlockingQueue
      rejected-handler: AbortPolicy
```

### 4. 获取与使用

框架会自动根据配置创建或更新线程池。

Java

```
@Resource
private ThreadPoolExecutor orderServiceExecutor; // Bean 名称需与 thread-pool-id 一致
```

* * *

## 📝 变更日志示例

当你在 Nacos 修改参数并发布后，控制台将输出清晰的对比日志：

Plaintext

```
----------------------------------------------------------------------
🔄 [ElasticExecutor] Thread Pool Configuration Changed
----------------------------------------------------------------------
 Pool Name                : order-service-executor
 Core Pool Size           : 10 ➜ 20
 Maximum Pool Size        : 20 ➜ 40
 Queue Capacity           : 1024 ➜ 2048
 Keep Alive Time          : 60 ➜ 60
 Rejected Handle          : AbortPolicy ➜ CallerRunsPolicy
 Allow Core Thread Timeout: false ➜ false
----------------------------------------------------------------------
```

* * *

## 📂 项目结构

Plaintext

```
elastic-executor
├── elastic-executor-core       # 核心模块：配置定义、Binder解析、变更监听
├── elastic-executor-starter    # Starter模块：自动配置、Bean注入
├── elastic-executor-example    # 示例模块：演示 Demo
└── pom.xml
```

* * *

## 🗓️ 开发计划 (Roadmap)

-   [x] 完成核心配置类与 Nacos 监听对接
-   [x] 解决 Spring Boot 3 Binder 解析 YAML 嵌套 Map 的问题
-   [x] 实现 JDK 线程池参数动态热更新
-   [x] 支持更多类型的阻塞队列动态调整 (ResizableLinkedBlockingQueue)
-   [x] 集成 Prometheus 监控指标导出
-   [ ] 提供 Web 控制台页面 (Admin Console)
