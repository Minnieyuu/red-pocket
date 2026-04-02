package com.example.yumi.Enum;

import lombok.Getter;

@Getter
public enum Active {

	Active("活動進行中"), Finish("活動結束");

	private String desc;

	private Active(String desc) {
		this.desc = desc;
	}

}
