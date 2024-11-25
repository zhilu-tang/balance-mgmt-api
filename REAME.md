# Balance Management API

## 项目概述

`balance-mgmt-api` 是一个用于管理用户账户余额的 RESTful API 服务。该项目提供了创建账户、查询余额、转账等功能，旨在为金融应用提供可靠、高效的余额管理服务。

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

- **接口**：`POST /api/v1/accounts`
- **请求参数**：
  - `userId` (String): 用户ID
  - `initialBalance` (Double): 初始余额
- **响应**：
  - 成功：返回新创建的账户信息
  - 失败：返回错误信息

### 2. 查询余额

- **接口**：`GET /api/v1/accounts/{userId}`
- **请求参数**：
  - `userId` (String): 用户ID
- **响应**：
  - 成功：返回用户的账户余额
  - 失败：返回错误信息

### 3. 转账

- **接口**：`POST /api/v1/transfers`
- **请求参数**：
  - `fromUserId` (String): 转出用户ID
  - `toUserId` (String): 转入用户ID
  - `amount` (Double): 转账金额
- **响应**：
  - 成功：返回转账结果
  - 失败：返回错误信息

## 环境配置

### 1. 数据库配置

在 `src/main/resources/application.yml` 中配置数据库连接信息：

```
yaml
spring:
datasource:
url: jdbc:mysql://localhost:3306/balance_mgmt?useSSL=false&serverTimezone=UTC
username: root
password: root
jpa:
hibernate:
ddl-auto: update
show-sql: true
```
### 2. 依赖管理

使用 Maven 进行依赖管理。：

### 3. 启动项目

- **使用 Maven**：
```
sh mvn spring-boot:run
```

## 测试

项目包含单元测试和集成测试，位于 `src/test/java` 目录下。可以使用以下命令运行所有测试：


- **使用 Maven**：
```
sh mvn test
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