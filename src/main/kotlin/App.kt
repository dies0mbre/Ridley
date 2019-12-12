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

    //DatabaseFactory.init()
    val playerService = PlayerService()
    DatabaseFactory.init()

    routing {
        get("/") {
            call.respond("The connection is built!")
        }
        get("/test/{login}/{password}") {
            val login = call.parameters["login"].toString()
            val password = call.parameters["password"].toString()
            call.respond("Finally!! you win is : $login $password")
        }
        get("/signup/{login}/{password}") { // регистрация
            val login = call.parameters["login"].toString()
            val password = call.parameters["password"].toString()
            // должен происходить коннект с базой данных на наличие логина,
            // если он есть, то отправлять ответ вида call.respond("existing_login")
            // иначе заносить новые поля в таблицу, инициализировать остальные поля пустыми
            if (playerService.getPlayer(login) != null) {
                call.respond("0") // Existing login
            }
            else {
                val player = NewPlayer(login = login, password = password)
                playerService.addPlayer(player)
                call.respond(HttpStatusCode.Created, "1")
            }
        }
        post("/login/{login}/{password}") {
            // осуществляется попытка входа
            val login = call.parameters["login"].toString()
            val pswd = call.parameters["password"].toString()
            try {
                // происходит обращение в базу данных, ищется логин и сверяется пароль
                // иначе бросается исключение
                throw Exception()
            } catch (e : Exception) {
                call.respond(HttpStatusCode.Accepted, "The incorrect login or password")
            }
        }

        put ("/") {

        }

        get("/random/{min}/{max}") {
            val min = call.parameters["min"]?.toIntOrNull() ?: 0
            val max = call.parameters["max"]?.toIntOrNull() ?: 10
            val randomString = "${(min until max).shuffled().last()}"
            call.respond(mapOf("value" to randomString))
        }
    }
}