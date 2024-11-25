# 使用 Alpine 精简版 JRE 镜像
FROM amazoncorretto:17-alpine

# 设置作者信息
LABEL authors="markus"

# 设置工作目录
WORKDIR /app

# 将构建好的 Spring Boot JAR 文件复制到容器中
COPY /target/balance-mgmt-api.jar /app/balance-mgmt-api.jar

# 设置环境变量
ENV JAVA_OPTS=""
ENV spring.profiles.active=cluster

# 设置启动命令
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/balance-mgmt-api.jar" ]
