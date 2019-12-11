package model

import org.jetbrains.exposed.sql.Table

object Players : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val login = varchar("login", 255)
    val password = varchar("password",255)
    val data = varchar("data", 500)
}

object Puzzles : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val name = varchar("name", 255)
    val text = varchar("text", 1000)
    val answer = varchar("answer", 255)
}


data class Player(
    val id: Int,
    val login: String,
    val password: String,
    val data: String
)


data class NewPlayer(
    val id: Int?,
    val login: String,
    val password: String
)