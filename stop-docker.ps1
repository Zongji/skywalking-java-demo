# 停止 Docker 容器脚本

Write-Host "===== 停止 Docker 容器 =====" -ForegroundColor Green

docker-compose down

Write-Host ""
Write-Host "MySQL 容器已停止" -ForegroundColor Green