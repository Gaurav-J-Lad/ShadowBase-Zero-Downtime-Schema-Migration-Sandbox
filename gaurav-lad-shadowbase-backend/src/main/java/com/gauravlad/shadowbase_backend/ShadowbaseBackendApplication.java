package com.gauravlad.shadowbase_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class ShadowbaseBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				ShadowbaseBackendApplication.class,
				args
		);
	}
}