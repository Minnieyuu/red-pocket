package com.example.yumi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class RedPocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedPocketApplication.class, args);
	}

}
