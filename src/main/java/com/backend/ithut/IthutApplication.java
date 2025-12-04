package com.backend.ithut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/")
public class IthutApplication {

	public static void main(String[] args) {
		SpringApplication.run(IthutApplication.class, args);
	}

    @GetMapping
    public String Main() {
        return "Hello, world!";
    }
}