package com.resurrection_finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ResurrectionFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResurrectionFinanceApplication.class, args);
	}

}