package com.redis;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = {
		RedisAutoConfiguration.class,
		RedisReactiveAutoConfiguration.class
})
@EnableCaching
public class SpringbootCacheExampleApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(SpringbootCacheExampleApplication.class);
		/*
		 * in case don't want to configure the config here, through maven use
		 * mvn spring-boot:run
		 * -Dspring-boot.run.arguments="--spring.config.name=springCache-dev"
		 * OR
		 * set SPRING_CONFIG_NAME=springCache-dev
		 * mvn spring-boot:run
		 */
		app.setDefaultProperties(Map.of("spring.config.name", "springCache-dev"));
		app.run(args);

	}

}
