plugins {
    kotlin("jvm") version "1.9.24"
    id("io.ktor.plugin") version "2.3.11"
    kotlin("plugin.serialization") version "1.9.24"
    id("org.flywaydb.flyway") version "9.22.3"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.ktor:ktor-server-core-jvm:2.3.11")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.11")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.11")


    implementation("io.ktor:ktor-server-auth-jvm:2.3.11")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:2.3.11")


    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.11")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")


    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.44.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Redis
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")

    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.20.0")

    // Flyway migrations
    implementation("org.flywaydb:flyway-core:9.22.3")

    // BCrypt
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.3.11")

    // Swagger
    implementation("io.ktor:ktor-server-openapi:2.3.11")
    implementation("io.ktor:ktor-server-swagger:2.3.11")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:2.3.11")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:rabbitmq:1.19.3")
    testImplementation("io.mockk:mockk:1.13.8")

// testImplementation("org.jetbrains.exposed:exposed-assertions:0.44.1")
}

// Flyway configuration
flyway {
    url = "jdbc:postgresql://localhost:5432/ktor_db"
    user = "postgres"
    password = "password"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}