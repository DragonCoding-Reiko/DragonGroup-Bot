package net.dragoncoding.groupbot.discord.command

import net.dragoncoding.groupbot.common.logging.IStatus
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.*

interface IDiscordSlashCommand {
    /**
     * In the hierarchy or a subcommand this is the base command
     *
     * Example:
     *
     * - Full command in JDA: "create channel audit" -> "audit"
     * - Full command in JDA: "create audit" -> "audit"
     * - Full command in JDA: "create" -> "create"
     *
     */
    val name: String

    val description: String

    /**
     * In the hierarchy or a subcommand this is the base command
     *
     * Example:
     *
     * - Full command in JDA: "create channel audit" -> "channel"
     * - Full command in JDA: "create audit" -> null
     * - Full command in JDA: "create" -> null
     *
     */
    val subCommandGroupName: String?

    /**
     * In the hierarchy or a subcommand this is the base command
     *
     * Example:
     *
     * - Full command in JDA: "create channel audit" -> "create"
     * - Full command in JDA: "create audit" -> "create"
     * - Full command in JDA: "create" -> null
     *
     */
    val parentCommandName: String?

    val fullName: String
        get() {
            return if (!isSimpleCommand() && hasSubCommandGroup())
                "$parentCommandName $subCommandGroupName $name"
            else if (!isSimpleCommand())
                "$parentCommandName $name"
            else
                name
        }

    var buttonId: String?
    var modalId: String?

    val acknowledgeCommand: Boolean
    val acknowledgeButton: Boolean
    val acknowledgeModal: Boolean

    fun runCommand(event: SlashCommandInteractionEvent): IStatus
    fun runButton(event: ButtonInteractionEvent): IStatus
    fun runModal(event: ModalInteractionEvent): IStatus

    fun onCommand(event: SlashCommandInteractionEvent): IStatus
    fun onButton(event: ButtonInteractionEvent): IStatus
    fun onModal(event: ModalInteractionEvent): IStatus

    fun getCommandOptions(): List<OptionData>

    fun generateUuid(): String = UUID.randomUUID().toString()

    fun isSimpleCommand(): Boolean = this.parentCommandName == null
    fun hasSubCommandGroup(): Boolean = this.subCommandGroupName != null
    fun hasButtonInteraction(): Boolean = this.buttonId != null
    fun hasModalInteraction(): Boolean = this.modalId != null
}