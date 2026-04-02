package com.example.yumi.consumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.yumi.Enum.Active;
import com.example.yumi.config.RabbitConfig;
import com.example.yumi.dao.ActivityDao;
import com.example.yumi.dao.GrabRecordDao;
import com.example.yumi.dto.DeadLetterMessageDto;
import com.example.yumi.entity.Activity;
import com.example.yumi.entity.GrabRecord;
import com.example.yumi.event.GrabSuccessEvent;
import com.rabbitmq.client.Channel;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedPocketConsumer {

	@Autowired
	ActivityDao activityDao;

	@Autowired
	GrabRecordDao grabRecordDao;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	@Autowired
	RabbitTemplate rabbitTemplate;

	@Transactional /* 確保資料一致姓 失敗回滾 */
	@RabbitListener(queues = RabbitConfig.QUEUE, ackMode = "MANUAL")
	public void handleGrabMessage(
			GrabSuccessEvent event,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag,
			Channel channel) throws IOException {

		Optional<Activity> activity = activityDao.findByStatusAndActivityId(Active.Active, event.activityId());

		if (activity.isEmpty()) {

			log.error("找不到活動或活動已關閉，活動ID{}", event.activityId());
			return;
		}

		try {

//			int i = 1 / 0;

			GrabRecord record = new GrabRecord();
			record.setActivity(activity.get());
			record.setGrabTime(LocalDateTime.now());
			record.setUserId(event.userId());

			/* 儲存紀錄 */
			grabRecordDao.save(record);

			/* 扣庫存 */
			activityDao.decreaseStock(event.activityId());

			/* 確認 DB commit 後才發送 ack */
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						channel.basicAck(tag, false);
						log.info("🎊 DB 已確定 Commit，正式發送 Ack！");
					} catch (IOException e) {
						log.error("Ack 失敗（網路問題），訊息將會 Requeue", e);
					}
				}
			});

//			channel.basicAck(tag, false);

			log.info("✅ [DB] 關聯紀錄已存入，庫存已扣減");

		} catch (Exception e) {

			DeadLetterMessageDto dto = new DeadLetterMessageDto();

			dto.setActivityId(event.activityId());
			dto.setUserId(event.userId());
			dto.setErrorType(e.getClass().getSimpleName());
			dto.setErrorMessage(e.getMessage());

			rabbitTemplate.convertAndSend(
					RabbitConfig.DLQ_EXCHANGE,
					RabbitConfig.DLQ_ROUTING_KEY,
					dto);

			channel.basicAck(tag, false);

			log.error("❌ 處理失敗，送入DLQ", e);

		}

	}

}
