package com.hollybam.hollybam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@MapperScan("com.hollybam.hollybam.dao")
@SpringBootApplication
@EnableScheduling
public class HollybamApplication {

	public static void main(String[] args) {
		SpringApplication.run(HollybamApplication.class, args);
	}

}
