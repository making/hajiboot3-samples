package com.example.hajiboot3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController // (1)
public class Hajiboot3Application {

	@GetMapping(path = "/") // (2)
	public String hello() {
		return "Hello World!";
	}

	public static void main(String[] args) {
		SpringApplication.run(Hajiboot3Application.class, args);
	}
}