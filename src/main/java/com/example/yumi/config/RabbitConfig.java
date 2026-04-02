package com.example.yumi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	/* 正常Queue */
	public static final String EXCHANGE = "redpocket.exchange";
	public static final String ROUTING_KEY = "redpocket.routing.key";
	public static final String QUEUE = "redpocket.queue";

	/* 死信Queue */
	public static final String DLQ_EXCHANGE = "redpocket.dlq.exchange";
	public static final String DLQ_ROUTING_KEY = "redpocket.dlq.key";
	public static final String DLQ_QUEUE = "redpocket.dlq.queue";

	/* 系統死信Queue */
	public static final String SYS_DLQ_EXCHANGE = "redpocket.sys.dlq.exchange";
	public static final String SYS_DLQ_ROUTING_KEY = "redpocket.sys.dlq.key";
	public static final String SYS_DLQ_QUEUE = "redpocket.sys.dlq.queue";

	/* 自動把 Record 轉成 JSON 字串 */
	@Bean
	MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/* 1.定義EXCHANE */
	@Bean
	DirectExchange redPocketExchange() {

		return new DirectExchange(EXCHANGE);
	}

	/* 2.定義Queue */
	@Bean
	Queue redPocketQueue() {
		return QueueBuilder.durable(QUEUE)
				.withArgument("x-dead-letter-exchange", SYS_DLQ_EXCHANGE) // 萬一出事，丟給這個交換機
				.withArgument("x-dead-letter-routing-key", SYS_DLQ_ROUTING_KEY) // 丟過去時要帶的 Key
				.build();
	}

	/* 綁定 */
	@Bean
	Binding redPocketBinding() {

		return BindingBuilder.bind(redPocketQueue()).to(redPocketExchange()).with(ROUTING_KEY);

	}

	/**
	 * 死信Queue
	 * 
	 */

	@Bean
	DirectExchange redPocketDLQExchange() {

		return new DirectExchange(DLQ_EXCHANGE);
	}

	@Bean
	Queue redPocketDQLQueue() {
		return new Queue(DLQ_QUEUE, true);
	}

	@Bean
	Binding redPocketDQLBinding() {

		return BindingBuilder.bind(redPocketDQLQueue()).to(redPocketDLQExchange()).with(DLQ_ROUTING_KEY);

	}

	/**
	 * 系統死信Queue
	 * 
	 */

	@Bean
	DirectExchange redPocketSysDLQExchange() {

		return new DirectExchange(SYS_DLQ_EXCHANGE);
	}

	@Bean
	Queue redPocketSysDQLQueue() {
		return new Queue(SYS_DLQ_QUEUE, true);
	}

	@Bean
	Binding redPocketSysDQLBinding() {

		return BindingBuilder.bind(redPocketSysDQLQueue()).to(redPocketSysDLQExchange()).with(SYS_DLQ_ROUTING_KEY);

	}

}
