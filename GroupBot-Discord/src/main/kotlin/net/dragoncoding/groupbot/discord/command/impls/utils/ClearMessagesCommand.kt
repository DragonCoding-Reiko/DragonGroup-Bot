package net.dragoncoding.groupbot.discord.command.impls.utils

import net.dragoncoding.groupbot.common.logging.IStatus
import net.dragoncoding.groupbot.common.logging.Status
import net.dragoncoding.groupbot.discord.command.AbstractDiscordSlashCommand
import net.dragoncoding.groupbot.discord.command.annotations.DiscordCommand
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

@DiscordCommand
class ClearMessagesCommand : AbstractDiscordSlashCommand() {
    companion object {
        private const val DEFAULT_CLEAR_AMOUNT: Int = 1
    }

    override val name: String = "clear"
    override val description: String = "Clears the defined amount of messages (default: $DEFAULT_CLEAR_AMOUNT)"

    val amountParam = OptionData(
        OptionType.INTEGER,
        "amount",
        "The amount of messages to clear. If not set, default($DEFAULT_CLEAR_AMOUNT) will be used."
    )

    override fun runCommand(event: SlashCommandInteractionEvent): IStatus {
        val channel = event.channel
        val amount = event.getOption(amountParam.name)?.asInt ?: DEFAULT_CLEAR_AMOUNT

        val deletedAmount = event.channel.purgeMessagesById(*getMessagesToPurge(channel, amount).toLongArray()).size

        event.reply("Deleting $deletedAmount messages").setEphemeral(true).queue()
        return Status.OK_STATUS
    }

    private fun getMessagesToPurge(channel: MessageChannelUnion, amount: Int): List<Long> {
        val messageIds: MutableList<Long> = ArrayList()
        var i = amount
        for (message in channel.iterableHistory.cache(false)) {
            if (i <= 0) break
            if (!message.isPinned) {
                messageIds.add(message.idLong)
                --i
            }
        }
        return messageIds
    }

    override fun getCommandOptions(): List<OptionData> {
        return listOf(amountParam)
    }

    override fun getCommandPermission(): DefaultMemberPermissions? {
        return DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)
    }
}