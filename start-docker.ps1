# Docker 环境启动脚本

Write-Host "===== Docker 环境启动脚本 =====" -ForegroundColor Green

# 检查 Docker 是否安装
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "错误: Docker 未安装，请先安装 Docker" -ForegroundColor Red
    exit 1
}

# 检查 docker-compose 是否安装
if (-not (Get-Command docker-compose -ErrorAction SilentlyContinue)) {
    Write-Host "错误: docker-compose 未安装，请先安装 docker-compose" -ForegroundColor Red
    exit 1
}

Write-Host "1. 启动 MySQL 容器..." -ForegroundColor Yellow
docker-compose up -d

Write-Host "2. 等待 MySQL 启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "3. 检查 MySQL 容器状态..." -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "===== MySQL 连接信息 =====" -ForegroundColor Green
Write-Host "主机: localhost"
Write-Host "端口: 3306"
Write-Host "用户名: root"
Write-Host "密码: root123456"
Write-Host "数据库: demo5"
Write-Host "DDL 文件已自动执行"
Write-Host ""
Write-Host "===== 下一步 =====" -ForegroundColor Green
Write-Host "1. 运行 Spring Boot 应用: .\mvnw.cmd spring-boot:run"
Write-Host "2. 或在 IDE 中运行 Demo5Application"
Write-Host ""
Write-Host "应用将在 http://localhost:8082/demo5 启动" -ForegroundColor Cyan