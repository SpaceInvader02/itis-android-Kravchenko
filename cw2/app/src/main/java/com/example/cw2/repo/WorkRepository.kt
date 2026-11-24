package com.example.cw2.repo

import kotlinx.coroutines.delay

object WorkRepository {

    suspend fun doHeavyWork(number: Int) {
        delay(5000L)
        println("$number заверешно")
    }
}
