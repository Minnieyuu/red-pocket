package com.example.yumi.listener;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.yumi.config.RabbitConfig;
import com.example.yumi.event.GrabSuccessEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GrabEventListener {

	private static final String MQ_LOST_LIST = "redpocket:MQlost:list";

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

//	@Async
	@EventListener
	public void handleGrabSuccess(GrabSuccessEvent event) {
		try {
			log.info("Publish grab event to MQ, activityId={}, userId={}", event.activityId(), event.userId());

			rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, event);

			log.info("Grab event published to MQ, activityId={}, userId={}", event.activityId(), event.userId());
		}
		catch (Exception e) {
			redisTemplate.opsForHash().put(MQ_LOST_LIST, event.userId(), event.activityId());
			log.error("Publish grab event to MQ failed, cached in Redis. activityId={}, userId={}",
					event.activityId(), event.userId(), e);
			throw new IllegalStateException("Publish grab event to MQ failed", e);
		}
	}
}
