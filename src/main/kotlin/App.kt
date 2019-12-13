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
            playerService.addPuzzle(NewPuzzle(name = "Almost Alive",
                text = "They have not flesh, nor feathers, nor scales, nor bone. Yet they have fingers and thumbs of their own. What are they?",
                answer = "Gloves"))
            playerService.addPuzzle(NewPuzzle(name = "Kids Riddles B",
                text = "I am white when I am dirty, and black when I am clean. What am I?",
                answer = "A blackboard"))
            playerService.addPuzzle(NewPuzzle(name = "Do You Have It?",
                text = "Poor people have it. Rich people need it. If you eat it you die. what is it?",
                answer = "Nothing"))
            playerService.addPuzzle(NewPuzzle(name = "Birthday Riddle",
                text = "What goes up but never comes down??",
                answer = "Age"))
            playerService.addPuzzle(NewPuzzle(name = "Kids Riddles H",
                text = "The more you take away, the more I become. What am I?",
                answer = "A hole"))
            playerService.addPuzzle(NewPuzzle(name = "Ancient?",
                text = "There is an ancient invention still used in some parts of the world today that allows people to see through walls. What is it?",
                answer = "A window"))

            call.respond(playerService.getAllPuzzles())
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
            call.respond("1")
        }
    }
}