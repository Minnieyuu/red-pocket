package com.example.yumi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.yumi.Enum.FailedMessageStatus;
import com.example.yumi.dao.FailedMessageDao;
import com.example.yumi.entity.FailedMessage;
import com.example.yumi.event.GrabSuccessEvent;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FailedMessageService {

	@Autowired
	FailedMessageDao failedMessageDao;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	public List<FailedMessage> findByStatus(FailedMessageStatus pending) {

		return failedMessageDao.findByStatus(pending);
	}

	public void publishEvent(List<FailedMessage> failedMessages) {

		for (FailedMessage m : failedMessages) {

			eventPublisher.publishEvent(new GrabSuccessEvent(m.getActivityId(), m.getUserId()));

			log.info("存入EVENT，活動編號={}，userId={}", m.getActivityId(), m.getUserId());

		}

		log.info("一共存入:{}筆", failedMessages.size());

	}

}
