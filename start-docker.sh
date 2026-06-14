#!/bin/bash

echo "===== Docker 环境启动脚本 ====="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查 docker-compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误: docker-compose 未安装，请先安装 docker-compose"
    exit 1
fi

echo "1. 启动 MySQL 容器..."
docker-compose up -d

echo "2. 等待 MySQL 启动..."
sleep 10

echo "3. 检查 MySQL 容器状态..."
docker-compose ps

echo ""
echo "===== MySQL 连接信息 ====="
echo "主机: localhost"
echo "端口: 3306"
echo "用户名: root"
echo "密码: root123456"
echo "数据库: demo5"
echo "DDL 文件已自动执行"
echo ""
echo "===== 下一步 ====="
echo "1. 运行 Spring Boot 应用: ./mvnw spring-boot:run"
echo "2. 或在 IDE 中运行 Demo5Application"
echo ""
echo "应用将在 http://localhost:8082/demo5 启动"