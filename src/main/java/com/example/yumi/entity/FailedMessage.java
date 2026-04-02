package com.example.yumi.entity;

import java.time.LocalDateTime;

import com.example.yumi.Enum.FailedMessageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FAILED_MESSAGE")
public class FailedMessage {

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "USER_ID", length = 20)
	private String userId;

	@Column(name = "ACTIVITY_ID", length = 20)
	private String activityId;

	@Lob
	@Column(name = "ERROR_MESSAGE")
	private String errorMessage; // 紀錄為什麼失敗 (例如：資料庫斷線)

	/* PENDING: 待處理, RETRIED: 已重試過了 */
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20)
	private FailedMessageStatus status = FailedMessageStatus.PENDING;

	@Column(name = "create_time")
	private LocalDateTime createTime = LocalDateTime.now();
}
