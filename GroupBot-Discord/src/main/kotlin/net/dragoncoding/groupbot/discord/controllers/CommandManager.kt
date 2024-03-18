package net.dragoncoding.groupbot.discord.controllers

import net.dragoncoding.groupbot.discord.command.IDiscordSlashCommand
import net.dragoncoding.groupbot.discord.command.JDACommandManager
import net.dragoncoding.groupbot.discord.command.annotations.DiscordCommand
import net.dragoncoding.groupbot.discord.command.annotations.IgnoreCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class CommandManager : InitializingBean {
    @Autowired
    private lateinit var context: ApplicationContext
    lateinit var jdaCommandManager: JDACommandManager

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val commands: HashMap<String, Class<IDiscordSlashCommand>> = HashMap()

    override fun afterPropertiesSet() {
        jdaCommandManager = JDACommandManager(
            "net.dragoncoding.groupbot.discord.command.impls",
            DiscordCommand::class.java
        )
            .enableLogging()
            .excludeAnnotations(IgnoreCommand::class.java)
            .load(context)

        jdaCommandManager.getCommands().forEach {
            commands[it.fullName] = it.javaClass
        }
    }

    fun getSlashCommandByName(command: String): IDiscordSlashCommand? {
        try {
            if (!commands.containsKey(command)) {
                logger.error("No command found for '$command'")
                return null
            }

            val discordCommand = commands[command]?.let { context.getBean(it) }
            if (discordCommand == null) {
                logger.error("The command '$command' was not found!")
                return null
            }

            return discordCommand
        } catch (e: NoSuchBeanDefinitionException) {
            logger.error("Command Bean not found!", e)
            return null
        }
    }
}
