package com.hollybam.hollybam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@MapperScan("com.hollybam.hollybam.dao")
@SpringBootApplication
public class HollybamApplication {

	public static void main(String[] args) {
		SpringApplication.run(HollybamApplication.class, args);
	}

}
