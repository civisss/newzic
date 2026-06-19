package com.newzic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NewzicApplication

fun main(args: Array<String>) {
    runApplication<NewzicApplication>(*args)
}
