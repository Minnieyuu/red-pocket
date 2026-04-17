package com.example.yumi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.yumi.Enum.FailedMessageStatus;
import com.example.yumi.Enum.GrabStatus;
import com.example.yumi.dto.GrabPocketDto;
import com.example.yumi.entity.FailedMessage;
import com.example.yumi.event.GrabSuccessEvent;
import com.example.yumi.service.FailedMessageService;
import com.example.yumi.service.RedPocketService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class RedPocketController {

	@Autowired
	RedPocketService redPocketService;

	@Autowired
	FailedMessageService failedMessageService;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@PostMapping("/minnie/grabRedPocket")
	public ResponseEntity<Map<String, Object>> grabRedPocket(@RequestBody @Valid GrabPocketDto grab) {

		// lua腳本
		int code = redPocketService.grab(grab.getActivityId(), grab.getUserId());

		// 2. 透過 Enum 翻譯官，把 code 轉成有意義的狀態物件
		GrabStatus status = GrabStatus.of(code);

		// 3. 核心邏輯：只有成功搶到的人，才進 MQ 隊列排隊寫入資料庫
		if (status == GrabStatus.SUCCESS) {

			eventPublisher.publishEvent(new GrabSuccessEvent(grab.getActivityId(), grab.getUserId()));

			log.info("存入EVENT，活動編號={}，userId={}", grab.getActivityId(), grab.getUserId());

		}

		// 4. 回傳結果
		return ResponseEntity.status(status.getHttpCode()).body(Map.of(
				"code", status.getHttpCode(),
				"success", status.isSuccess(),
				"message", status.getMessage()));
	}

	/* 將失敗的資料撈出，重新丟進event，並寫入MQ */
	@PostMapping("/minnie/retryDeadLetter")
	public ResponseEntity<Map<String, Object>> retryDeadLetter() {

		List<FailedMessage> failedMessages = failedMessageService.findByStatus(FailedMessageStatus.PENDING);

		if (failedMessages.size() == 0) {

			return ResponseEntity.status(200).body(Map.of(
					"code", "200",
					"message", "無待處理失敗訊息"));

		}

		failedMessageService.publishEvent(failedMessages);

		return ResponseEntity.status(200).body(Map.of(
				"code", "200",
				"message", "已將待處理失敗訊息存入EVENT"));
	}

	@PostMapping("/minnie/directDB")
	@Transactional
	public ResponseEntity<Map<String, Object>> directDB(@RequestBody @Valid GrabPocketDto grab) {

		// 1. 原子更新：判斷並扣減庫存 (不超發的核心)
		int updatedRows = redPocketService.updateStock(grab.getActivityId());

		GrabStatus status = GrabStatus.SYSTEM_ERROR;
		if (updatedRows > 0) {

			try {

				// 2. 寫入領取紀錄 (唯一索引防止重複領取)
				redPocketService.insertGrabRecord(grab.getActivityId(), grab.getUserId());

				status = GrabStatus.SUCCESS;

				return ResponseEntity.status(status.getHttpCode()).body(Map.of(
						"code", status.getHttpCode(),
						"success", status.isSuccess(),
						"message", status.getMessage()));

			} catch (Exception e) {

				log.error("####### 失敗 rollBack !!!");

				return ResponseEntity.status(status.getHttpCode()).body(Map.of(
						"code", status.getHttpCode(),
						"success", status.isSuccess(),
						"message", status.getMessage()));

			}

		} else {

			status = GrabStatus.SOLD_OUT;
			return ResponseEntity.status(status.getHttpCode()).body(Map.of(
					"code", status.getHttpCode(),
					"success", status.isSuccess(),
					"message", status.getMessage()));

		}

	}

	@PostMapping("/minnie/grabWithRedis")
	@Transactional
	public ResponseEntity<Map<String, Object>> grabWithRedis(@RequestBody @Valid GrabPocketDto grab) {

		// lua腳本
		int code = redPocketService.grab(grab.getActivityId(), grab.getUserId());

		// 2. 透過 Enum 翻譯官，把 code 轉成有意義的狀態物件
		GrabStatus status = GrabStatus.of(code);

//		// 3. 核心邏輯：只有成功搶到的人，寫進DB
//		if (status == GrabStatus.SUCCESS) {
//
//			// 2. 寫入領取紀錄 (唯一索引防止重複領取)
//			redPocketService.insertGrabRecord(grab.getActivityId(), grab.getUserId());
//
//		}

		return ResponseEntity.status(status.getHttpCode()).body(Map.of(
				"code", status.getHttpCode(),
				"success", status.isSuccess(),
				"message", status.getMessage()));

	}

}
