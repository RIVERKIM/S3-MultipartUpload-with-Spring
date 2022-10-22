import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.4"
	id("io.spring.dependency-management") version "1.0.14.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	//implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	//csv
	implementation("org.apache.commons:commons-csv:1.5")
	//microsoft document processing
	implementation("org.apache.poi:poi-ooxml:4.1.2")
	//aws-java-sdk-s3
	//implementation("com.amazonaws:aws-java-sdk-s3:1.12.319")
	implementation("software.amazon.awssdk:bom:2.14.7")
	implementation("software.amazon.awssdk:s3:2.17.293")
	implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

	//kotest
	testImplementation("io.kotest:kotest-runner-junit5:5.4.0")
	testImplementation("io.kotest:kotest-assertions-core:5.4.0")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")


	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	//testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
