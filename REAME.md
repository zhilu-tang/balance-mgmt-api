# Balance Management API

## 项目概述

`balance-mgmt-api` 是一个用于管理用户账户余额的 RESTful API 服务。该项目提供了创建账户、查询余额、转账等功能，旨在为金融应用提供可靠、高效的余额管理服务。

## roadmap
| 状态 | 内容 | 说明 |
|----|------|------|
| ✅  | 初始化项目结构 | 完成项目的基本目录结构和初始配置 |
| ✅  | 核心功能实现：创建账户、查询余额、转账等功能 | 实现主要的业务逻辑和功能 |
| ✅  | 核心功能实现：一致性保障、事务处理、分布式锁等 | 实现系统的高可用性和数据一致性 |
| ✅  | 单元测试 | 编写并运行单元测试，确保各个模块的功能正确 |
| ✅  | 集成测试 | 编写并运行集成测试，确保各模块之间的协同工作正常 |
| ✅  | 性能测试 | 进行性能测试，确保系统在高负载下的表现 |
| ✅  | 部署k8s | 编写并测试 Kubernetes 部署配置文件，确保应用可以在 K8s 集群中正常运行 |
|    | 部署到阿里云K8S集群 | 将应用部署到阿里云的 K8s 集群中，确保生产环境的稳定性 |
| ✅  | 文档说明 | 编写详细的项目文档，包括安装、配置、使用说明等 |

## 目录结构

```markdown
balance-mgmt-api/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/pkg/balance/mgmt/api/
│   │   │       ├── controller/
│   │   │       │   └── BalanceController.java
│   │   │       ├── service/
│   │   │       │   └── BalanceService.java
│   │   │       ├── repository/
│   │   │       │   └── BalanceRepository.java
│   │   │       └── BalanceApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── data.sql
│   └── test/
│       └── java/
│           └── com/pkg/balance/mgmt/api/
│               ├── controller/
│               │   └── BalanceControllerTest.java
│               ├── service/
│               │   └── BalanceServiceTest.java
│               └── BalanceApplicationTests.java
├── build.gradle  # Gradle 构建脚本
├── pom.xml       # Maven 构建脚本
└── README.md     # 项目说明文档
```
## 技术栈

- **编程语言**：Java
- **框架**：Spring Boot
- **数据库**：MySQL
- **构建工具**：Maven
- **API 文档**：Swagger

## 核心功能

### 1. 创建账户

- **接口**：`POST /api/account/create`
- **请求参数**：
  - `accountNumber` (String): 用户ID
  - `balance` (Double): 初始余额
- **响应**：
  - 成功：返回新创建的账户信息
  - 失败：返回错误信息

### 2. 查询账户

- **接口**：`GET /api/account/get`
- **请求参数**：
  - `accountNumber` (String): 用户accountNumber
- **响应**：
  - 成功：返回账户信息
  - 失败：返回错误信息

### 3. 转账

- **接口**：`POST /api/account/createTransaction`
- **请求参数**：
  - `sourceAccountNumber` (String): 转出用户accountNumber
  - `destinationAccountNumber` (String): 转入用户accountNumber
  - `amount` (Double): 转账金额
- **响应**：
  - 成功：返回转账结果
  - 失败：返回错误信息

## 环境配置

### 1. 数据库配置

在 `src/main/resources/application.yml` 中配置数据库连接信息：

```
spring:
  datasource:
    url: jdbc:mysql://MYSQL_HOST:MYSQL_PORT/balance_db?useSSL=false&serverTimezone=UTC
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
```
### 2. 依赖管理

使用 Maven 进行依赖管理。：

### 3. 启动项目

- **使用 Maven**：
```
sh mvn spring-boot:run
```

## 测试

### 1. 单元测试
项目包含单元测试和集成测试，位于 `src/test/java` 目录下。可以使用以下命令运行所有测试：

- **使用 Maven**：
```
sh mvn test
```

### 2. 集成测试
项目包含集成测试，位于 `src/test/java` 目录下。
对 rest 接口采用端到端集成测试，需要配置好数据库、缓存连接信息，会自动自动创建测试数据库。

### 3. 性能测试
性能测试脚本位于 loadtesting 目录下。执行以下命令运行性能测试：
```
sh loadtesting/run_load.sh
```
方法二：
运行com.pkg.balance.mgmt.performance.JMeterLiveTest 测试类，会自动启动应用并进行压测。

## 部署

### 1. Docker镜像构建
项目提供 Dockerfile，可以使用以下命令构建镜像并运行容器：

```
sh mvn clean dockerfile:build
```
### 2. 部署到 Kubernetes
项目提供 Kubernetes 部署配置文件，可以使用以下命令将应用部署到本地 Kubernetes 集群：
```
-- 创建 secret 用于拉取 docker image
kubectl create secret docker-registry regcred \      
     --docker-server=https://index.docker.io/v1/ \
     --docker-username=YOUR_USERNAME \
     --docker-password=YOUR_PASSWORD \
     --docker-email=YOUR_EMAIL
sh kubectl apply -f deploy/mysql-service.yml
sh kubectl apply -f deploy/redis-service.yml
sh kubectl apply -f deploy/balance-mgmt-deployment-local.yml
```


## API 文档

项目使用 Swagger 生成 API 文档。启动项目后，可以通过以下 URL 访问 API 文档：

```
http://localhost:8080/swagger-ui.html
```
## 贡献

欢迎贡献代码和提出改进建议。请遵循以下步骤：

1. Fork 项目
2. 创建新分支 (`git checkout -b feature-branch`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature-branch`)
5. 提交 Pull Request

## 联系我们

如果有任何问题或建议，请通过以下方式联系我们：

- 邮箱：zhilu.tang@gmail.com
- GitHub Issues：[https://github.com/zhilu.tang/balance-mgmt-api/issues](https://github.com/yourusername/balance-mgmt-api/issues)

---

感谢您的关注和支持！
```