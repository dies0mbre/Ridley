package service

import model.Players
import model.Puzzles
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        // Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        Database.connect(hikari())
        /*transaction {
            create(Players)
            Players.insert {
                it[login] = "admin"
                it[password] = "admin"
                it[data] = "0:done"
            }
            create(Puzzles)
            Puzzles.insert {
                it[name] = "David's Father"
                it[text] = "David's father has three sons : Snap, Crackle and _____ ?"
                it[answer] = "David"
            }

        }*/
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql://ec2-174-129-255-37.compute-1.amazonaws.com:5432/d849viisnojdug"
        config.username = "qfrpiztbjiouij"
        config.password = "ff042f9a1d4509cce619e34674addd64fba710b14289efd043421227656749e2"

        config.maximumPoolSize = 5
        config.minimumIdle = 1
        config.idleTimeout = 60000 // 1 minutes
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(
        block: suspend () -> T): T =
        newSuspendedTransaction { block() }

}