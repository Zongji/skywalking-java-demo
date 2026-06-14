package com.demo.sentinel.demo.service;

import com.demo.sentinel.demo.entity.Actor;
import com.demo.sentinel.demo.mapper.ActorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TestScheduler {

    @Autowired
    private ActorMapper actorMapper;

    @Scheduled(fixedRate = 1000)
    public void test11() {
        log.info("this is the test task, time is:{}", LocalDateTime.now().toString());
        
        List<Actor> actors = actorMapper.selectList(null);
        log.info("查询到 {} 条 actor 记录", actors.size());
        actors.subList(0, 10).forEach(actor -> log.info("Actor: id={}, firstName={}, lastName={}",
                actor.getActorId(), actor.getFirstName(), actor.getLastName()));
    }
}
