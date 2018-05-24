package com.blockchaininfo.backendData;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = "com.blockchaininfo.backendData.*")
@EnableScheduling
@SpringBootApplication
public class BackendDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendDataApplication.class, args);

	}
}
