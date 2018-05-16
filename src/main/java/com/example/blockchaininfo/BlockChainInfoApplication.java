package com.example.blockchaininfo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BlockChainInfoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockChainInfoApplication.class, args);

	}
}
