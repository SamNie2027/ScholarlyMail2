package com.samnie.scholarlymail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

@SpringBootApplication
@EnableCouchbaseRepositories(basePackages = "com.samnie.scholarlymail")
public class ScholarlyMailApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScholarlyMailApplication.class, args);
	}

}
