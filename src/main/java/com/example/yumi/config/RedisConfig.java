package com.example.yumi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisConfig {

	/**
	 * 設定 RedisTemplate 序列化為純字串（避免 key 出現亂碼）
	 */
	@Bean
	RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		StringRedisSerializer str = new StringRedisSerializer();
		template.setKeySerializer(str);
		template.setValueSerializer(str);
		template.setHashKeySerializer(str);
		template.setHashValueSerializer(str);
		template.afterPropertiesSet();
		return template;
	}

	/**
	 * 載入 Lua 腳本。 Spring 會計算腳本 SHA1，第一次用 EVAL 執行後快取， 之後改用 EVALSHA 執行（效能更佳）。
	 */
	@Bean
	DefaultRedisScript<Long> grabRedPocketScript() {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/grab_redpocket.lua")));
		script.setResultType(Long.class);
		return script;
	}

}
