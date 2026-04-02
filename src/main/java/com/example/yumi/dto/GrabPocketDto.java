package com.example.yumi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrabPocketDto {

	@NotBlank
	@NotNull
	private String activityId;

	@NotBlank
	@NotNull
	private String userId;

}
