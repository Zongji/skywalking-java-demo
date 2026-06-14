# SkyWalking 在 Spring Boot 3 中的应用教程

## 一、概述

Apache SkyWalking 是一个开源的 APM（应用性能监控）系统，提供分布式追踪、服务网格遥测分析、度量聚合和可视化一体化解决方案。本教程基于当前代码库，介绍如何在 Spring Boot 3 项目中集成 SkyWalking，实现服务间调用链路追踪和定时任务追踪。

---

## 二、环境准备

### 2.1 安装 SkyWalking

#### 方式一：Docker 快速部署（推荐）

创建 `docker-compose.yml` 文件：

```bash
version: '3.8'
services:
  skywalking-oap:
    image: apache/skywalking-oap-server:9.3.0
    container_name: skywalking-oap
    restart: always
    ports:
      - "11800:11800"  # gRPC 接收端口
      - "12800:12800"  # HTTP 接收端口
    environment:
      SW_STORAGE: h2
      TZ: Asia/Shanghai

  skywalking-ui:
    image: apache/skywalking-ui:9.3.0
    container_name: skywalking-ui
    restart: always
    ports:
      - "8080:8080"
    environment:
      SW_OAP_ADDRESS: http://skywalking-oap:12800
      TZ: Asia/Shanghai
```

启动命令：
```bash
docker-compose up -d
```

访问 UI：http://localhost:8080

#### 方式二：Windows 环境手动安装

##### 2.1.1 下载安装包

从 SkyWalking 官方下载页面获取最新版本：
- 下载地址：https://skywalking.apache.org/downloads/
- 选择 `skywalking-9.3.0.tar.gz`（Windows 和 Linux 通用）

##### 2.1.2 解压安装包

```powershell
# 解压到指定目录
Expand-Archive -Path skywalking-9.3.0.tar.gz -DestinationPath D:\skywalking

# 进入解压目录
cd D:\skywalking\skywalking-9.3.0
```

目录结构说明：
```
skywalking-9.3.0/
├── bin/              # 启动脚本
├── config/           # 配置文件
├── oap-libs/         # OAP Server 依赖
├── skywalking-agent/ # Agent 目录
└── webapp/           # UI 前端
```

##### 2.1.3 配置 MySQL 存储

> **重要提醒**：OAP Server 默认使用 H2 内存数据库，生产环境必须配置持久化存储（如 MySQL）。

**步骤 1：创建 MySQL 数据库**

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE skywalking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'skywalking'@'localhost' IDENTIFIED BY 'skywalking@123';
GRANT ALL PRIVILEGES ON skywalking.* TO 'skywalking'@'localhost';
FLUSH PRIVILEGES;
```

**步骤 2：修改 OAP 配置文件**

编辑 `config/application.yml`：

```yaml
storage:
  selector: ${SW_STORAGE:mysql}
  mysql:
    properties:
      jdbcUrl: ${SW_JDBC_URL:jdbc:mysql://localhost:3306/skywalking?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true}
      dataSource.user: ${SW_DATA_SOURCE_USER:root}
      dataSource.password: ${SW_DATA_SOURCE_PASSWORD:root}
      dataSource.cachePrepStmts: true
      dataSource.prepStmtCacheSize: 250
      dataSource.prepStmtCacheSqlLimit: 2048
      dataSource.useServerPrepStmts: true
    metadataQueryMaxSize: 5000
    maxSizeOfArrayColumn: 20
    numOfSearchableValuesPerTag: 10
```

**步骤 3：添加 MySQL 驱动**

SkyWalking 9.3.0 默认不包含 MySQL 驱动，需要手动添加：

1. 下载 MySQL Connector/J 8.0.x 版本：https://dev.mysql.com/downloads/connector/j/
2. 将 `mysql-connector-j-8.0.xx.jar` 复制到 `oap-libs/` 目录

##### 2.1.4 启动 SkyWalking（Windows）

**启动 OAP Server：**

```powershell
# 进入 bin 目录
cd D:\skywalking\skywalking-9.3.0\bin

# 启动 OAP（前台运行）
.\oapService.bat
```

**启动 UI（新命令行窗口）：**

```powershell
cd D:\skywalking\skywalking-9.3.0\bin

# 启动 UI
.\webappService.bat
```

或者一次启动两个服务，使用startup.bat脚本。 startup.bat 脚本内实际是调用了上述两个脚本。



**验证启动成功：**
- OAP Server：访问 http://localhost:12800，返回 `{"code":0,"data":"success"}`
- UI：访问 http://localhost:8080

##### 2.1.5 停止 SkyWalking（Windows）

```powershell
# 停止 OAP
.\oapService.bat stop

# 停止 UI
.\webappService.bat stop
```

---

## 三、项目依赖配置

### 3.1 Maven 依赖

在 `pom.xml` 中添加 SkyWalking 相关依赖：

```xml
<!-- SkyWalking Toolkit for Trace -->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-trace</artifactId>
    <version>9.3.0</version>
</dependency>

<!-- SkyWalking Toolkit for Logback -->
<dependency>
    <groupId>org.apache.skywalking</groupId>
    <artifactId>apm-toolkit-logback-1.x</artifactId>
    <version>9.3.0</version>
</dependency>
```

> **说明**：当前项目使用 Spring Boot 3.5.14 和 Java 24，SkyWalking 9.3.0 完全兼容。

---

## 四、日志集成配置

### 4.1 Logback 配置

配置 `src/main/resources/logback-spring.xml`，实现日志中包含 TraceId：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
    <property name="LOG_PATH" value="logs"/>
    <property name="APP_NAME" value="demo5"/>

    <!-- 控制台输出（带 traceId） -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%tid] [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n</pattern>
            </layout>
        </encoder>
    </appender>

    <!-- gRPC 日志上报到 SkyWalking OAP -->
    <appender name="GRPC_LOG" class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.log.GRPCLogClientAppender">
        <grpcAddress>localhost:11800</grpcAddress>
        <maxMessageSize>10485760</maxMessageSize>
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%tid] [%thread] %-5level %logger{36} - %msg%n</pattern>
            </layout>
        </encoder>
    </appender>

    <!-- 根日志级别配置 -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="GRPC_LOG"/>
    </root>
</configuration>
```

**关键点说明**：
- `%tid`：SkyWalking 提供的 TraceId 占位符
- `GRPCLogClientAppender`：将日志上报到 SkyWalking OAP
- `grpcAddress`：OAP Server 的 gRPC 地址（默认端口 11800）

---

## 五、服务间调用链路追踪

### 5.1 使用 RestTemplate（不推荐）

**问题**：RestTemplate 默认不支持 SkyWalking 自动拦截，无法传递 Trace 上下文。

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 5.2 使用 HttpClient5（推荐）

HttpClient5 可以被 SkyWalking 自动拦截，实现完整的链路追踪：

```java
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

public void callRemoteService() {
    String url = "http://localhost:8083/demo-boot3/test";
    
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
            // 处理响应
            log.info("调用成功, traceId:{}", TraceContext.traceId());
        }
    } catch (Exception e) {
        log.error("调用失败: {}, traceId:{}", e.getMessage(), TraceContext.traceId());
    }
}
```

### 5.3 获取当前 TraceId

使用 `TraceContext` 工具类获取当前链路的 TraceId：

```java
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        String traceId = TraceContext.traceId();
        log.info("当前请求的 traceId: {}", traceId);
        return "demo:" + traceId;
    }
}
```

---

## 六、定时任务追踪

Spring 的 `@Scheduled` 定时任务会被 SkyWalking 自动拦截追踪：

```java
import org.springframework.scheduling.annotation.Scheduled;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

@Service
public class TestScheduler {

    @Scheduled(fixedRate = 3000)
    public void callRemoteTask() {
        log.info("定时任务开始执行, traceId:{}", TraceContext.traceId());
        
        // 执行业务逻辑
        // 调用外部服务等
        
        log.info("定时任务执行完成, traceId:{}", TraceContext.traceId());
    }
}
```

**注意**：需要在启动类上添加 `@EnableScheduling` 注解。

---

## 七、启动配置

### 7.1 设置 Java Agent

SkyWalking 使用 Java Agent 方式实现无侵入式的链路追踪，启动时需要指定 agent：

```bash
# Linux/Mac
java -javaagent:/path/to/skywalking-agent/skywalking-agent.jar \
     -Dskywalking.agent.service_name=demo5 \
     -Dskywalking.collector.backend_service=localhost:11800 \
     -jar demo5-0.0.1-SNAPSHOT.jar
```

```powershell
# Windows
java -javaagent:D:\path\to\skywalking-agent\skywalking-agent.jar ^
     -Dskywalking.agent.service_name=demo5 ^
     -Dskywalking.collector.backend_service=localhost:11800 ^
     -jar demo5-0.0.1-SNAPSHOT.jar
```

### 7.2 Agent 配置文件

也可以通过修改 `skywalking-agent/config/agent.config` 文件进行配置：

```properties
# 服务名称
agent.service_name=demo5

# OAP Server 地址
collector.backend_service=localhost:11800
```

---

## 八、验证链路追踪

### 8.1 查看日志

启动应用后，日志中会包含 TraceId：

```
2024-01-15 10:30:00.000 [abc123def456] [http-nio-8082-exec-1] INFO  c.d.s.d.controller.TestController - 当前请求的 traceId: abc123def456
```

### 8.2 SkyWalking UI 查看

访问 http://localhost:8080，在 SkyWalking UI 中可以查看：

1. **服务拓扑图**：展示服务间调用关系
2. **追踪**：查看完整的调用链路详情
3. **日志**：查看上报的日志（需配置 GRPC_LOG）

---

## 九、常见问题

### 9.1 RestTemplate 无法传递 Trace 上下文

**原因**：SkyWalking 的 RestTemplate 拦截器在 Spring Boot 3 中可能存在兼容性问题。

**解决方案**：改用 HttpClient5 或 OkHttp。

### 9.2 日志中没有 TraceId

**检查项**：
1. 确认引入了 `apm-toolkit-logback-1.x` 依赖
2. 确认配置文件中使用了 `TraceIdPatternLogbackLayout`
3. 确认应用正确配置了 Java Agent

### 9.3 SkyWalking UI 无法看到数据

**检查项**：
1. 确认 OAP Server 已启动（端口 11800、12800）
2. 确认 agent 配置的 `collector.backend_service` 正确
3. 确认服务名称配置正确

---

## 十、总结

本教程介绍了 SkyWalking 在 Spring Boot 3 中的核心应用：

| 功能 | 实现方式 |
|------|----------|
| 日志集成 | `apm-toolkit-logback-1.x` + `TraceIdPatternLogbackLayout` |
| 服务间调用 | HttpClient5（推荐） |
| 定时任务 | `@Scheduled` 自动追踪 |
| 获取 TraceId | `TraceContext.traceId()` |
| 日志上报 | `GRPCLogClientAppender` |

通过以上配置，即可实现完整的分布式链路追踪能力。