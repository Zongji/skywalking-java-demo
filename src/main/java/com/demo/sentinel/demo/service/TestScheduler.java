package com.demo.sentinel.demo.service;

import com.demo.sentinel.demo.entity.Actor;
import com.demo.sentinel.demo.mapper.ActorMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TestScheduler {

    @Autowired
    private ActorMapper actorMapper;

    @Autowired
    private RestTemplate restTemplate;

//    @Scheduled(fixedRate =4000)
    public void test11() {
        log.info("this is the test task, time is:{}", LocalDateTime.now().toString());
        
        List<Actor> actors = actorMapper.selectList(null);
        log.info("查询到 {} 条 actor 记录, tranceId:{}", actors.size(), TraceContext.traceId());
//        actors.subList(0, 10).forEach(actor -> log.info("Actor: id={}, firstName={}, lastName={}",
//                actor.getActorId(), actor.getFirstName(), actor.getLastName()));

        // 调用外部服务, restTemplate调用外部接口不成功，需要改为使用httpclient组件才能成功。
        callExternalService();
    }

    private void callExternalService() {
        try {
            String url = "http://localhost:8082/demo5/test";
            log.info("开始调用外部服务: {}", url);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            log.info("外部服务调用成功, 响应状态: {}, 响应体: {}, traceId:{}", 
                    response.getStatusCode(), response.getBody(), TraceContext.traceId());
        } catch (Exception e) {
            log.error("调用外部服务失败: {}, traceId:{}", e.getMessage(), TraceContext.traceId());
        }
    }

    @Scheduled(fixedRate = 3000)
    public void callRemoteTask() {
        try {
            String url = "http://localhost:8083/demo-boot3/test";
            log.info("开始调用外部接口 (HttpClient5): {}, traceId:{}", url, TraceContext.traceId());
            
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet httpGet = new HttpGet(url);
                try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    int statusCode = response.getCode();
                    HttpEntity entity = response.getEntity();
                    String responseBody = entity != null ? EntityUtils.toString(entity) : null;
                    log.info("HttpClient5 调用成功, 状态码: {}, 响应体: {}, traceId:{}", 
                            statusCode, responseBody, TraceContext.traceId());
                }
            }
        } catch (IOException | ParseException e) {
            log.error("HttpClient5 调用失败: {}, traceId:{}", e.getMessage(), TraceContext.traceId());
        }
    }
}
