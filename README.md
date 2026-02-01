# ElasticExecutor - 动态线程池治理框架

![License](https://img.shields.io/badge/license-Apache%202.0-blue)
![Language](https://img.shields.io/badge/language-Java%2017-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.7-green)
![Nacos](https://img.shields.io/badge/Nacos-2.2.1-blue)

**ElasticExecutor** 是一个轻量级、高扩展性的 Java 动态线程池治理框架。它允许开发者在不重启应用的情况下，通过 Nacos 配置中心动态调整线程池的核心参数（核心线程数、最大线程数、队列容量等），并提供实时的变更日志监控。

> **核心目标**：解决传统线程池参数调优困难、故障排查滞后的痛点，实现线程池的“热插拔”式管理。

---

## ✨ 核心特性 (Features)

* **⚡ 动态调参**：支持运行时动态修改 `corePoolSize`、`maximumPoolSize`、`queueCapacity` 等核心参数，实时生效。
* **🔌 无感接入**：基于 Spring Boot Starter 设计，引入依赖并配置 Nacos 即可使用，零代码侵入。
* **🛡️ 变更监控**：提供可视化的配置变更日志，清晰记录参数的 "旧值 ➜ 新值" 变化，便于回溯。
* **⚙️ 兼容性强**：
    * 完美适配 **Spring Boot 3.x** (支持 `spring.config.import` 机制)。
    * 完美适配 **Nacos 2.x** (基于 gRPC 长连接)。
    * **配置解析增强**：支持 YAML 嵌套结构与 Properties 平铺结构的自动识别与绑定（解决了 Spring Binder 的 Map 转换痛点）。
* **📊 监控埋点**（开发中）：集成 Micrometer，支持 Prometheus/Grafana 实时监控线程池运行指标。

## 🛠️ 架构设计 (Architecture)

1.  **启动阶段**：自动扫描并注册 `ElasticExecutorProperties` 配置类。
2.  **监听阶段**：利用 Nacos Config Service 监听远程配置文件的变更。
3.  **解析阶段**：
    * 使用 `YamlPropertiesFactoryBean` 将复杂的 YAML 嵌套结构扁平化。
    * 利用 Spring `Binder` API 进行强类型校验与绑定。
4.  **执行阶段**：
    * 计算配置差异 (Diff)。
    * 优雅更新 JDK `ThreadPoolExecutor` 实例参数（处理 Core/Max 更新顺序问题）。
    * 输出变更日志。

## 🚀 快速开始 (Quick Start)

### 1. 引入依赖
在你的 Spring Boot 项目 `pom.xml` 中添加 `elastic-executor-spring-boot-starter`：

```xml
<dependency>
    <groupId>org.puregxl</groupId>
    <artifactId>elastic-executor-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
