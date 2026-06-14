package com.demo.sentinel.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
public class TestController {
    @GetMapping("/test")
    public String test(HttpServletRequest httpRequest) {
        log.info(httpRequest.toString());
        log.info("--------------demo5 /test, traceId:{}", TraceContext.traceId());

        return "demo:" + TraceContext.traceId();
    }
}
