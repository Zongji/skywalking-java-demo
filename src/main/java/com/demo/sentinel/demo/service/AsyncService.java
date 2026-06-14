package com.demo.sentinel.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncService {

    @Async
    public void callAsync() {
        log.info("this is in the @Async function, traceId:{}， currentThread：{}",
                TraceContext.traceId(), Thread.currentThread().getName());
    }
}
