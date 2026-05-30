package com.SPT;

import com.SPT.Config.WhatsappTwilioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(WhatsappTwilioProperties.class)
public class SptApplication {

	public static void main(String[] args) {
		SpringApplication.run(SptApplication.class, args);
	}

}
