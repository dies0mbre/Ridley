package com.example.kotlinserver

import io.ktor.server.engine.*
import io.ktor.server.netty.*
val port = System.getenv("PORT")?.toInt() ?: 23567


fun main(args : Array<String>){
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}
