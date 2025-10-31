plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.4"
}

group = "com.ddbs"
version = "0.0.1-SNAPSHOT"
description = "Session Management Microservice for Choroid"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // For web applications
    implementation("org.springframework.boot:spring-boot-starter-web")
    // For database access
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.postgresql:postgresql:42.7.8")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.apache.spark:spark-core_2.12:3.5.3")
    implementation("org.apache.spark:spark-sql_2.12:3.5.3")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper")
    
    // Jersey dependencies for Spark UI (using javax.servlet, not jakarta)
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("org.glassfish.jersey.core:jersey-server:2.41")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet-core:2.41")
    implementation("org.glassfish.jersey.containers:jersey-container-servlet:2.41")
    implementation("org.glassfish.jersey.inject:jersey-hk2:2.41")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
