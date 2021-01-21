package com.rconcept.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author LZx
 * @since 2020/12/31
 */
@Slf4j
@RestController
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@GetMapping("/hello")
	public Mono<String> hello() {
		log.info("hello");
		return Mono.just("hello");
	}

}
