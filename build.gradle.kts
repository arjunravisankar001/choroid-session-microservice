plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ddbs"
version = "0.0.1-SNAPSHOT"
description = "Session Management Microservice for Choroid"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // For web applications
    implementation("org.springframework.boot:spring-boot-starter-web")

    // For database access with JPA and Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Use H2 database for simplicity
    runtimeOnly("com.h2database:h2")

    implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
