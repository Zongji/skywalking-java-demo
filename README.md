## 说明
1. 本地启动skywalking(oap服务和前端ui服务)
   本地启动注意事项： oap启动依赖数据库存储，可以配置使用mysql作为数据存储。修改application.yaml中默认使用mysql, 并配置mysql连接信息
2. java应用启动的jvm配置增加
```shell
-javaagent:E:\Tech\Skywalking\skywalking-agent\skywalking-agent.jar -Dskywalking.agent.service_name=demo5 -Dskywalking.collector.backend_service=127.0.0.1:11800
```

## 教程说明
[skywalking-springboot3-guide.md](docs/skywalking-springboot3-guide.md)
