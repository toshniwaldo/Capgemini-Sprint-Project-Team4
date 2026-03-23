package com.example.DemoCheck;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class DemoCheckApplication {

	@Autowired
	private DataSource dataSource;

	public static void main(String[] args) {
		SpringApplication.run(DemoCheckApplication.class, args);
	}

	@PostConstruct
	public void testConnection() throws Exception {
		System.out.println("DB Connection: " + dataSource.getConnection());
	}
}
