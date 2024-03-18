package net.dragoncoding.groupbot.app

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import javax.sql.DataSource

@SpringBootApplication
class GroupBotApplication

@Autowired
lateinit var source: DataSource

@Autowired
lateinit var bot: GroupBot

val timer = Executors.newScheduledThreadPool(3)
val taskList = ArrayList<ScheduledFuture<*>>()

fun main(args: Array<String>) {
    (timer as ScheduledThreadPoolExecutor).removeOnCancelPolicy = true

    runApplication<GroupBotApplication>(*args)

    shutdown()
}

private fun shutdown() {
    for (task in taskList) {
        task.cancel(false)
    }

    timer.shutdown()
}

