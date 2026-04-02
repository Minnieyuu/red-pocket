package com.example.yumi.Enum;

import lombok.Getter;

@Getter
public enum FailedMessageStatus {

	PENDING("待處理"), RETRIED("已重試");

	private String desc;

	private FailedMessageStatus(String desc) {
		this.desc = desc;
	}
}
