package com.example.yumi.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.yumi.config.RabbitConfig;
import com.example.yumi.dao.FailedMessageDao;
import com.example.yumi.dto.DeadLetterMessageDto;
import com.example.yumi.entity.FailedMessage;
import com.rabbitmq.client.Channel;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 處理死信 存入DB->FailedMessage
 *
 */
@Component
@Slf4j
public class DeadLetterConsumer {

	@Autowired
	FailedMessageDao failedMessageDao;

	@Transactional /* 確保資料一致姓 失敗回滾 */
	@RabbitListener(queues = RabbitConfig.DLQ_QUEUE, ackMode = "MANUAL") /* 手動簽收 */
	public void handleFailedMessage(
			DeadLetterMessageDto deadLetter,
			@Header(AmqpHeaders.DELIVERY_TAG) long tag,
			Channel channel

	) {

		try {

			FailedMessage failedMessage = new FailedMessage();

			failedMessage.setUserId(deadLetter.getUserId());
			failedMessage.setActivityId(deadLetter.getActivityId());

			failedMessage
					.setErrorMessage("原因：" + deadLetter.getErrorType()
							+ " \n詳細內容 :" + deadLetter.getErrorMessage());

			failedMessageDao.save(failedMessage);

			// 成功存入 DB 後，才簽收訊息
			channel.basicAck(tag, false);

			log.info("✅ 已手動簽收死信並存入資料庫");

		} catch (Exception e) {

			log.info("死信存入DB失敗，{}", e);
		}

	}

}
