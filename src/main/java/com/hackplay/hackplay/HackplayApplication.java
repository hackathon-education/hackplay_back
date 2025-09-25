package com.hackplay.hackplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HackplayApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackplayApplication.class, args);
	}

}
