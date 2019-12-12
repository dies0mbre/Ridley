package service

import model.*
import org.jetbrains.exposed.sql.*
import service.DatabaseFactory.dbQuery

class PlayerService {

    private val listeners = mutableMapOf<Int, suspend (Notification<Player?>) -> Unit>()

    fun addChangeListener(id: Int, listener: suspend (Notification<Player?>) -> Unit) {
        listeners[id] = listener
    }

    fun removeChangeListener(id: Int) = listeners.remove(id)

    private suspend fun onChange(type: ChangeType, id: Int, entity: Player? = null) {
        listeners.values.forEach {
            it.invoke(Notification(type, id, entity))
        }
    }

    suspend fun getAllPlayers(): List<Player> = dbQuery {
        Players.selectAll().map { toPlayer(it) }
    }

    suspend fun getPlayer(login: String): Player? = dbQuery {
        Players.select {
            (Players.login eq login)
        }.mapNotNull { toPlayer(it) }
            .singleOrNull()
    }

    suspend fun compPlayerPswd(login: String, pswd : String): Player? = dbQuery {
        Players.select {
            (Players.login.eq(login) and Players.password.eq(pswd))
        }.mapNotNull { toPlayer(it) }
            .singleOrNull()
    }

    suspend fun getPlayerById(id: Int): Player? = dbQuery {
        Players.select {
            (Players.id eq id)
        }.mapNotNull { toPlayer(it) }
            .singleOrNull()
    }

    suspend fun updatePlayer(player: Player): Player? {
        val login = player.login
        return if (login == null) {
            player
        } else {
            dbQuery {
                Players.update({ Players.login eq login }) {
                    it[data] = System.currentTimeMillis().toString() // !!! когда происходит победа !!!
                }
            }
            getPlayer(player.login).also {
                onChange(ChangeType.UPDATE, player.id, it)
            }
        }
    }

    suspend fun addPlayer(player: NewPlayer): Player {
        var key = 0
        dbQuery {
            key = (Players.insert {
                it[login] = player.login
                it[password] = player.password
                it[data] = ""
            } get Players.id)
        }
        return getPlayerById(key)!!.also {
            onChange(ChangeType.CREATE, key, it)
        }
    }

    suspend fun deletePlayer(id: Int): Boolean { // ВРЯД ЛИ ПОНАДОБИТСЯ АГАГА МММММ
        return dbQuery {
            Players.deleteWhere { Players.id eq id } > 0
        }.also {
            if (it) onChange(ChangeType.DELETE, id)
        }
    }

    private fun toPlayer(row: ResultRow): Player =
        Player(
            id = row[Players.id],
            login = row[Players.login],
            password = row[Players.password],
            data = row[Players.data]
        )
}