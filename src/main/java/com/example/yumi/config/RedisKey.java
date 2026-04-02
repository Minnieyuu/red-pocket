package com.example.yumi.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.yumi.entity.Activity;

public final class RedisKey {

	// 禁止實例化（純工具類別）
	private RedisKey() {
	}

	/** key 格式模板 */
	private static final String STOCK_KEY = "redpocket:stock:%s";
	private static final String WINNERS_KEY = "redpocket:winners:%s";

	/**
	 * 取得庫存 key(多筆)
	 * 
	 */
	public static Map<String, Integer> stockKeys(List<Activity> actives) {

		Map<String, Integer> res = new HashMap<>();

		for (Activity active : actives) {

			res.put(String.format(STOCK_KEY, active.getActivityId()), active.getTotalStock());
		}

		return res;
	}

	/**
	 * 取得庫存 key 例：stockKey("activity-20240101") →
	 * "redpocket:stock:activity-20240101"
	 */
	public static String stockKey(String activityId) {
		return String.format(STOCK_KEY, activityId);
	}

	/**
	 * 取得已搶用戶 Set key 例：winnersKey("activity-20240101") →
	 * "redpocket:winners:activity-20240101"
	 */
	public static String winnersKey(String activityId) {
		return String.format(WINNERS_KEY, activityId);
	}

}
