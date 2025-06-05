package org.kevin.trello

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TrelloApplication

fun main(args: Array<String>) {
	runApplication<TrelloApplication>(*args)
}
