## ✨ Features

* **⚡ Dynamic Tuning**: Supports runtime modification of `corePoolSize`, `maximumPoolSize`, and `queueCapacity`, taking effect immediately.
* **🔌 Seamless Integration**: Designed as a Spring Boot Starter. Just add the dependency and configure Nacos—no code intrusion required.
* **🛡️ Change Monitoring**: Provides visualized configuration change logs, clearly recording "Old Value ➜ New Value" for easy backtracking.
* **⚙️ High Compatibility**:
    * Perfectly adapts to **Spring Boot 3.x** (supports `spring.config.import` mechanism).
    * Perfectly adapts to **Nacos 2.x** (gRPC long connection).
    * **Enhanced Parsing**: Automatically recognizes and binds YAML nested structures and Properties flat structures (Solving Spring Binder's Map conversion issues).
* **📊 Metrics** (WIP): Integrated with Micrometer to support Prometheus/Grafana for real-time monitoring.

## 🛠️ Architecture

1.  **Startup**: Automatically scans and registers `ElasticExecutorProperties`.
2.  **Listen**: Uses Nacos Config Service to listen for remote configuration changes.
3.  **Parse**: 
    * Flattens complex YAML nested structures using `YamlPropertiesFactoryBean`.
    * Uses Spring `Binder` API for strong type validation and binding.
4.  **Execute**:
    * Calculates configuration differences (Diff).
    * Gracefully updates JDK `ThreadPoolExecutor` parameters (handling Core/Max update order safety).
    * Outputs change logs.

## 🚀 Quick Start

### 1. Add Dependency
Add `elastic-executor-spring-boot-starter` to your `pom.xml`:

```xml
<dependency>
    <groupId>org.puregxl</groupId>
    <artifactId>elastic-executor-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>


### 2. Configuration

Configure Nacos address and dynamic rules in `application.yml`. **Note**: Spring Boot 3.x requires `config.import` syntax.

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
      - optional:nacos:example-thread-pool.yaml  # Import dynamic config data-id

# Local default config (Optional)
elastic-executor:
  enable: true
  monitor:
    enable: true
    collect-interval: 10
```

### 3. Nacos Config

Create a configuration `example-thread-pool.yaml` in Nacos Console:

YAML

```
elastic-executor:
  executors:
    - thread-pool-id: order-service-executor  # Unique ID
      core-pool-size: 10
      maximum-pool-size: 20
      queue-capacity: 1024
      keep-alive-time: 60
      blocking-queue: LinkedBlockingQueue
      rejected-handler: AbortPolicy
```

### 4. Usage

The framework automatically creates or updates the thread pool bean.

Java

```
@Resource
private ThreadPoolExecutor orderServiceExecutor; // Bean name must match thread-pool-id
```

## 📝 Change Log Example

When you modify parameters in Nacos and publish, the console outputs a clear comparison log:

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

## 📂 Project Structure

Plaintext

```
elastic-executor
├── elastic-executor-core       # Core: Config definition, Binder parsing, Listener
├── elastic-executor-starter    # Starter: Auto-configuration, Bean Injection
├── elastic-executor-example    # Example: Demo project
└── pom.xml
```

## 🗓️ Roadmap

-   [x] Core configuration class & Nacos listener integration
-   [x] Solved Spring Boot 3 Binder parsing issue for YAML nested Maps
-   [x] Implemented dynamic hot update for JDK ThreadPool parameters
-   [ ] Support dynamic resizing for more BlockingQueue types (ResizableLinkedBlockingQueue)
-   [ ] Integrate Prometheus metrics export
-   [ ] Provide Web Admin Console

## 🤝 Contribution

Issues and Pull Requests are welcome.

**Author**: Gao Xiaolei

**School**: North University of China