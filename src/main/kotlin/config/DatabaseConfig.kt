package com.example.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseConfig {
    fun init() {
        //val jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/ecommerce_db"
        val jdbcUrl = "jdbc:postgresql://localhost:5432/ecommerce_db?sslmode=disable"
        val username = System.getenv("DB_USERNAME") ?: "postgres"
        val password = System.getenv("DB_PASSWORD") ?: "password"

        println("Connecting to database: $jdbcUrl")

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
        }

        val dataSource = HikariDataSource(config)

        try {
            // Run Flyway migrations
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()

            val migrationsCount = flyway.migrate().migrationsExecuted
            println("Flyway migrations executed: $migrationsCount")

        } catch (e: Exception) {
            println("Flyway migration failed: ${e.message}")
            e.printStackTrace()
        }

        Database.connect(dataSource)
    }
}