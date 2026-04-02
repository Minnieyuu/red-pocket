package com.example.yumi.dto;

import lombok.Data;

@Data
public class DeadLetterMessageDto {

	private String activityId;
	private String userId;
	private String errorMessage;
	private String errorType;

}
