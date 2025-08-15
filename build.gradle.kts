plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "1.9.23" // 確保有這行，版本號可能不同
    kotlin("plugin.spring") version "1.9.23" // 確保有這行，版本號可能不同
	kotlin("plugin.jpa") version "1.9.23"
}

group = "com.example"
version = "0.0.3-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("me.paulschwarz:spring-dotenv:4.0.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.apache.poi:poi:5.2.5") // 您可以使用最新的穩定版本
    implementation("org.apache.poi:poi-ooxml:5.2.5") // 對應 poi 的版本
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.microsoft.sqlserver:mssql-jdbc")	
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0") // 請檢查最新版本
}

tasks.withType<Test> {
	useJUnitPlatform()
}
