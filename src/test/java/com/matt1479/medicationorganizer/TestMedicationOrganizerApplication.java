package com.matt1479.medicationorganizer;

import org.springframework.boot.SpringApplication;

public class TestMedicationOrganizerApplication {

	public static void main(String[] args) {
		SpringApplication.from(MedicationOrganizerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
