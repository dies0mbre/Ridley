package com.example.kotlinserver

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.main() {
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    routing {
        get("") {
            call.respond("I'm alive!")
        }
        get("hello") {
            call.respond(HttpStatusCode.Accepted, "Hello")
        }
        get("random/{min}/{max}") {
            val min = call.parameters["min"]?.toIntOrNull() ?: 0
            val max = call.parameters["max"]?.toIntOrNull() ?: 10
            val randomString = "${(min until max).shuffled().last()}"
            call.respond(mapOf("value" to randomString))
        }
    }
}