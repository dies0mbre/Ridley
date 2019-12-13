package com.example.kotlinserver

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.receive
import io.ktor.response.*
import io.ktor.routing.*
import model.NewPlayer
import model.NewPuzzle
import model.Puzzle
import service.DatabaseFactory
import service.PlayerService


fun Application.main() {
    install(DefaultHeaders) // This will add Date and Server headers to each HTTP response.
    install(CallLogging)    //
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
        jackson {
            configure(SerializationFeature.INDENT_OUTPUT, true)
        }
    }

    val playerService = PlayerService()
    DatabaseFactory.init()

    routing {
        get("/") {
            call.respond(playerService.getAllPlayers())
        }

        get("/signup/{login}/{password}") { // регистрация
            val login = call.parameters["login"].toString()
            val password = call.parameters["password"].toString()
            if (playerService.getPlayer(login) != null) {
                call.respond("0") // Existing login
            }
            else {
                val player = NewPlayer(login = login, password = password)
                playerService.addPlayer(player)
                call.respond(HttpStatusCode.Created, "1")
            }
        }

        get("/login/{login}/{password}") {
            // осуществляется попытка входа
            val login = call.parameters["login"].toString()
            val password = call.parameters["password"].toString()
            if (playerService.getPlayer(login) != null) {
                if (playerService.compPlayerPswd(login, password) != null)
                    call.respond("1")
                else call.respond("0")
            }
            else {
                call.respond(HttpStatusCode.Created, "0")
            }
        }

        get ("/play/enter/{login}") {
            val login = call.parameters["login"].toString()
            val data = playerService.getData(login).toString()
            var str  = ""
            var  dataAr = mutableListOf<Int>()
            var i = 0;
            while ( i != data.length ){
                if ((data[i] == ',') and (i==0)) {}
                else if (data[i] != ',') {
                    str += data[i].toString()
                    if (i == data.length-1) dataAr.add(str.toInt())
                }
                else {
                    dataAr.add(str.toInt())
                    str = ""
                }
                ++i
            }

            i = 1 // идентификатор загадки нерешенной
            for ( id in dataAr) {
                if ( i != id ) break;
                else i++
            }

            val currentPuzzle : Puzzle = playerService.getPuzzle(i)
            call.respond(currentPuzzle)
        }

        get ("/play/{id}/{login}") {
            val login = call.parameters["login"].toString()
            val idPuzzle = call.parameters["id"]?.toInt()
            var data = playerService.getData(login).toString() // забираю текущие идентификаторы решенных загадок
            data += ",$idPuzzle"
            val player = playerService.getPlayer(login)
            playerService.updatePlayer(player, data)
            call.respondRedirect("/play/enter/$login", permanent = true)
        }
    }
}