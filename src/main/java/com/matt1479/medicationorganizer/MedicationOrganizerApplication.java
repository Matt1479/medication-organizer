package com.matt1479.medicationorganizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.matt1479.medicationorganizer.config.RsaKeyProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class MedicationOrganizerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicationOrganizerApplication.class, args);
	}

}
