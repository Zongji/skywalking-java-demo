package com.demo.sentinel.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.sentinel.demo.entity.Actor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActorMapper extends BaseMapper<Actor> {
}