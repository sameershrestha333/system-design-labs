package com.sameer.sdl.system_design_labs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SystemDesignLabsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemDesignLabsApplication.class, args);
	}

}
