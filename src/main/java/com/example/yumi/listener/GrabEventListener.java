package com.example.yumi.listener;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.yumi.config.RabbitConfig;
import com.example.yumi.event.GrabSuccessEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GrabEventListener {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Async
	@EventListener // 監聽 GrabSuccessEvent
	public void handleGrabSuccess(GrabSuccessEvent event) {

		try {

			log.info(" 準備送入 MQ，userId={}", event.userId());

			rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);

			log.info(" MQ 已接收");

		} catch (Exception e) {

			log.error(" MQ 接收失敗，userId={}", event.userId());

			// 存入 Redis 備份
			redisTemplate.opsForHash().put("redpocket:MQlost:list", event.userId(), event.activityId());
		}

	}

}
