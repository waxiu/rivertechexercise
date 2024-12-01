package com.example.rivertech

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class RivertechApplication

fun main(args: Array<String>) {
    runApplication<RivertechApplication>(*args)
}
