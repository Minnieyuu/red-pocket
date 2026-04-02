package com.example.yumi.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import com.example.yumi.Enum.Active;
import com.example.yumi.config.RedisKey;
import com.example.yumi.dao.ActivityDao;
import com.example.yumi.entity.Activity;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // 自動生成建構子
public class RedPocketService {

	@Autowired
	ActivityDao activityDao;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	private final DefaultRedisScript<Long> grabScript;

	/**
	 * 初始化Redis
	 */
	@PostConstruct
	public void initStock() {

		// 1. 找所有有效活動
		List<Activity> activitys = activityDao.findByStatus(Active.Active);

		if (activitys == null || activitys.isEmpty()) {
			log.info("找不到活動，請先在 activity 表新增一筆資料");
			return;
		}

		// 2. 準備 Key-Value
		Map<String, Integer> stockMap = RedisKey.stockKeys(activitys);

		// 3. 真正存入 Redis
		stockMap.forEach((key, totalStock) -> {

			/**
			 * 核心邏輯：如果 Redis 已經有這個 Key 了，就不要覆蓋它！ 這樣重啟時，才會保留已經搶到一半的庫存數量。
			 */

			Boolean isAbsent = redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(totalStock));

			if (Boolean.TRUE.equals(isAbsent)) {

				log.info("✅ 初始化成功：Key=" + key + ", 庫存=" + totalStock);

			} else {

				log.info("ℹ️ Redis 已存在庫存，略過初始化：" + key);

			}
		});

	}

	public int grab(String activityId, String userId) {

		// key[0]=庫存 , key[1]=搶到的人
		List<String> keys = Arrays.asList(RedisKey.stockKey(activityId), RedisKey.winnersKey(activityId));

		// ① 執行 Lua 腳本（原子操作）
		Long result;
		try {
			result = redisTemplate.execute(grabScript, keys, userId);
		} catch (Exception e) {
			log.error("[Redis] Lua 腳本執行異常，userId={}", userId, e);
			return -9; // Redis 故障，走降級流程
		}

		if (result == null) {
			log.error("[Redis] Lua 回傳 null，userId={}", userId);
			return -9;
		}
		int code = result.intValue();

		return code;
	}

}
