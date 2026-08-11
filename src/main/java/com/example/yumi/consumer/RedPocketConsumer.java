package com.example.yumi.consumer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
	RabbitTemplate rabbitTemplate;

	@Transactional
	@RabbitListener(queues = RabbitConfig.QUEUE, ackMode = "MANUAL")
	public void handleGrabMessage(
			GrabSuccessEvent event,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag,
			Channel channel) throws IOException {

		Optional<Activity> activity = activityDao.findByStatusAndActivityId(Active.Active, event.activityId());

		if (activity.isEmpty()) {
			log.error("Activity not found or inactive, activityId={}", event.activityId());
			channel.basicReject(tag, false);
			return;
		}

		try {
			if (grabRecordDao.existsByUserIdAndActivity_ActivityId(event.userId(), event.activityId())) {
				channel.basicAck(tag, false);
				log.info("Duplicate grab message ignored, activityId={}, userId={}", event.activityId(), event.userId());
				return;
			}

			GrabRecord record = new GrabRecord();
			record.setActivity(activity.get());
			record.setGrabTime(LocalDateTime.now());
			record.setUserId(event.userId());

			grabRecordDao.saveAndFlush(record);

			int updatedRows = activityDao.decreaseStock(event.activityId());
			if (updatedRows == 0) {
				throw new IllegalStateException("DB stock is empty or activity inactive, activityId=" + event.activityId());
			}

			ackAfterCommit(event, tag, channel);

			log.info("Grab persisted, waiting for DB commit. activityId={}, userId={}",
					event.activityId(), event.userId());
		}
		catch (DataIntegrityViolationException e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			channel.basicAck(tag, false);
			log.info("Duplicate grab detected by unique constraint, ack message. activityId={}, userId={}",
					event.activityId(), event.userId());
		}
		catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			sendToBusinessDlq(event, e);
			channel.basicAck(tag, false);
			log.error("Grab message failed, sent to business DLQ. activityId={}, userId={}",
					event.activityId(), event.userId(), e);
		}
	}

	private void ackAfterCommit(GrabSuccessEvent event, long tag, Channel channel) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					channel.basicAck(tag, false);
					log.info("DB commit succeeded, ack message. activityId={}, userId={}",
							event.activityId(), event.userId());
				}
				catch (IOException e) {
					log.error("Ack failed after DB commit. RabbitMQ will redeliver if connection closes.", e);
				}
			}
		});
	}

	private void sendToBusinessDlq(GrabSuccessEvent event, Exception e) {
		DeadLetterMessageDto dto = new DeadLetterMessageDto();
		dto.setActivityId(event.activityId());
		dto.setUserId(event.userId());
		dto.setErrorType(e.getClass().getSimpleName());
		dto.setErrorMessage(e.getMessage());

		rabbitTemplate.convertAndSend(
				RabbitConfig.DLQ_EXCHANGE,
				RabbitConfig.DLQ_ROUTING_KEY,
				dto);
	}
}
