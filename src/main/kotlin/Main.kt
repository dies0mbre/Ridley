package com.example.kotlinserver

import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args : Array<String>){
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}
