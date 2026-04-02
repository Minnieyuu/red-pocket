package com.example.yumi.Enum;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrabStatus {

	SUCCESS(1, 200, "恭喜搶到紅包！", true),
	ALREADY_GRABBED(-1, 200, "你已搶過，每人限搶一次", false),
	SOLD_OUT(0, 200, "很遺憾，紅包已搶完", false),
	SYSTEM_ERROR(-9, 500, "系統繁忙，請稍後再試", false);

	private final int serviceCode; // Service 回傳的原始 code
	private final int httpCode; // 要給前端的代碼
	private final String message;
	private final boolean success;

	// 快速轉換：傳入 service code，回傳對應的 Enum
	public static GrabStatus of(int code) {
		return Arrays.stream(values())
				.filter(s -> s.serviceCode == code)
				.findFirst()
				.orElse(SYSTEM_ERROR);
	}
}
