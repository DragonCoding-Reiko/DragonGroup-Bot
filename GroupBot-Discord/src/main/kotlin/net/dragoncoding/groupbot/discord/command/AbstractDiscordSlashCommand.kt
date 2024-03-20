package net.dragoncoding.groupbot.discord.command

import net.dragoncoding.groupbot.common.logging.IStatus
import net.dragoncoding.groupbot.common.logging.Status
import net.dragoncoding.groupbot.common.logging.StatusCode
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class AbstractDiscordSlashCommand : IDiscordSlashCommand {
    override val subCommandGroupName: String? = null
    override val parentCommandName: String? = null

    override var buttonId: String? = null
    override var modalId: String? = null

    override val acknowledgeCommand: Boolean = false
    override val acknowledgeButton: Boolean = false
    override val acknowledgeModal: Boolean = false

    override var deleteOriginal: Boolean = false

    override fun getCommandOptions(): List<OptionData> = listOf()
    override fun getCommandPermission(): DefaultMemberPermissions? = null

    override fun runButton(event: ButtonInteractionEvent): IStatus =
        Status(StatusCode.ERROR, "No Button Event implemented")

    override fun runModal(event: ModalInteractionEvent): IStatus =
        Status(StatusCode.ERROR, "No Modal Event implemented")

    override fun onCommand(event: SlashCommandInteractionEvent): IStatus {
        if (acknowledgeCommand)
            event.deferReply(true).complete()

        val status = runCommand(event)

        if (status.isOk) {
            if (acknowledgeCommand && deleteOriginal) {
                event.hook.deleteOriginal().queue()
            }
        } else {
            val message = "An error occurred while executing this command:\n${status.message}"
            if (acknowledgeCommand)
                event.hook.editOriginal(message).queue()
            else
                event.reply(message).setEphemeral(true).queue()
        }
        return status
    }

    override fun onButton(event: ButtonInteractionEvent): IStatus {
        if (acknowledgeButton)
            event.deferReply(true).complete()

        val status = runButton(event)

        if (status.isOk) {
            if (acknowledgeButton && deleteOriginal) {
                event.hook.deleteOriginal().queue()
            }
        } else {
            val message = "An error occurred while processing this button:\n${status.message}"
            if (acknowledgeButton)
                event.hook.editOriginal(message).queue()
            else
                event.reply(message).setEphemeral(true).queue()
        }
        return status
    }

    override fun onModal(event: ModalInteractionEvent): IStatus {
        if (acknowledgeModal)
            event.deferReply(true).complete()

        val status = runModal(event)

        if (status.isOk) {
            if (acknowledgeModal && deleteOriginal) {
                event.hook.deleteOriginal().queue()
            }
        } else {
            val message = "An error occurred while processing this modal:\n${status.message}"
            if (acknowledgeModal)
                event.hook.editOriginal(message).queue()
            else
                event.reply(message).setEphemeral(true).queue()
        }
        return status
    }
}