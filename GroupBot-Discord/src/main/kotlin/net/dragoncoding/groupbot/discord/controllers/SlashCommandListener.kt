package net.dragoncoding.groupbot.discord.controllers

import net.dragoncoding.groupbot.discord.command.IDiscordSlashCommand
import net.dragoncoding.groupbot.discord.interfaces.IDiscordEventListener
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class SlashCommandListener : ListenerAdapter(), IDiscordEventListener {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SlashCommandListener::class.java)
    }

    @Autowired
    private lateinit var commandManager: CommandManager

    private val commandsWithButtonResponse: MutableList<IDiscordSlashCommand> = ArrayList()
    private val commandsWithModalResponse: MutableList<IDiscordSlashCommand> = ArrayList()

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val command = commandManager.getSlashCommandByName(event.fullCommandName)
        if (command == null) {
            LOGGER.error("Was not able to retrieve the command.")
            event.reply("I cannot process this command at the moment. Sorry for this inconvenience!")
                .setEphemeral(true)
                .queue()
            return
        }

        val executionStatus = command.onCommand(event)
        if (executionStatus.isNotOk) return

        if (command.hasButtonInteraction()) {
            commandsWithButtonResponse.add(command)
        }

        if (command.hasModalInteraction()) {
            commandsWithModalResponse.add(command)
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val buttonCommand = commandsWithButtonResponse.firstOrNull { event.componentId == it.buttonId }
        if (buttonCommand == null) {
            LOGGER.error("Was not able to retrieve the button command.")
            event.reply("I cannot process this button at the moment. Sorry for this inconvenience!")
                .setEphemeral(true)
                .queue()
            return
        }
        commandsWithButtonResponse.removeIf { event.componentId == it.buttonId }

        val executionStatus = buttonCommand.onButton(event)
        if (executionStatus.isNotOk) return
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val modalCommand = commandsWithModalResponse.firstOrNull { event.modalId == it.modalId }
        if (modalCommand == null) {
            LOGGER.error("Was not able to retrieve the modal command.")
            event.reply("I cannot process this modal at the moment. Sorry for this inconvenience!")
                .setEphemeral(true)
                .queue()
            return
        }
        commandsWithModalResponse.removeIf { event.modalId == it.buttonId }

        val executionStatus = modalCommand.onModal(event)
        if (executionStatus.isNotOk) return
    }


}
