package net.dragoncoding.groupbot.discord.command.impls.groups

import net.dragoncoding.groupbot.common.logging.IStatus
import net.dragoncoding.groupbot.common.logging.Status
import net.dragoncoding.groupbot.common.logging.Status.OK_STATUS
import net.dragoncoding.groupbot.common.logging.StatusCode
import net.dragoncoding.groupbot.data.models.DiscordGuildChannel
import net.dragoncoding.groupbot.data.models.GroupBotChannelType
import net.dragoncoding.groupbot.discord.command.AbstractDiscordSlashCommand
import net.dragoncoding.groupbot.discord.command.annotations.DiscordCommand
import net.dragoncoding.groupbot.discord.utils.JDAUtils
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.springframework.beans.factory.annotation.Autowired


@DiscordCommand
class SetGroupChannelCommand : AbstractDiscordSlashCommand() {

    companion object {
        const val OK_BUTTON_ID = "ok"
        const val CANCEL_BUTTON_ID = "cancel"
    }

    @Autowired
    lateinit var jdaUtils: JDAUtils

    override val parentCommandName: String? = "set"
    override val name: String = "groups-channel"
    override val description: String = "Sets the channel where groups will be posted and created in"

    override val acknowledgeCommand: Boolean = false
    override val acknowledgeButton: Boolean = true

    val channelParam =
        OptionData(OptionType.CHANNEL, "channel", "The channel where groups will be posted and created in", true)

    var newChannelId: Long? = null
    override fun runCommand(event: SlashCommandInteractionEvent): IStatus {
        val guild = event.guild ?: return Status(StatusCode.ERROR, "This command can only be used for guilds")
        val guildId = guild.idLong

        val groupGuild = jdaUtils.getGuild(guildId)
        val groupsChannel = groupGuild.specialChannels.firstOrNull { it.type == GroupBotChannelType.GROUPS_CHANNEL }

        val channelParam = event.getOption(channelParam.name)!!.asChannel
        if (channelParam.type != ChannelType.TEXT)
            return Status(StatusCode.ERROR, "The channel has to be a normal text channel")

        val newGroupChannel = channelParam.asTextChannel()

        if (groupsChannel != null) {
            val oldGroupsChannel = guild.getTextChannelById(groupsChannel.channelId)

            if (oldGroupsChannel != null) {
                if (oldGroupsChannel.idLong == newGroupChannel.idLong) {
                    event.reply("The channel is already set as groups channel").setEphemeral(true).queue()
                    return OK_STATUS
                }

                buttonId = generateUuid()
                newChannelId = newGroupChannel.idLong
                deleteOriginal = true
                event.reply("Are you sure you want to replace the current channel (${oldGroupsChannel.asMention}) with ${newGroupChannel.asMention}")
                    .setEphemeral(true)
                    .addActionRow(
                        Button.primary("$buttonId-$CANCEL_BUTTON_ID", "Keep Old Channel"),
                        Button.success("$buttonId-$OK_BUTTON_ID", "Replace!")
                    )
                    .queue()
                return OK_STATUS
            }
        }

        groupGuild.specialChannels.add(
            DiscordGuildChannel(newGroupChannel.idLong, GroupBotChannelType.GROUPS_CHANNEL)
        )
        jdaUtils.saveGuild(groupGuild)
        event.reply("The channel was set to ${newGroupChannel.asMention}").setEphemeral(true).queue()
        return OK_STATUS
    }

    /**
     * Will only be called when a channel was already set
     */
    override fun runButton(event: ButtonInteractionEvent): IStatus {
        deleteOriginal = false

        val guild = event.guild ?: return Status(StatusCode.ERROR, "This command can only be used for guilds")
        val guildId = guild.idLong

        val groupGuild = jdaUtils.getGuild(guildId)
        val buttonCommand = getButtonCommand(event.componentId)
        when (buttonCommand) {
            OK_BUTTON_ID -> {
                //Remove old channel
                groupGuild.specialChannels.removeIf { it.type == GroupBotChannelType.GROUPS_CHANNEL }
                //Add new channel
                groupGuild.specialChannels.add(
                    DiscordGuildChannel(newChannelId!!, GroupBotChannelType.GROUPS_CHANNEL)
                )

                jdaUtils.saveGuild(groupGuild)
                event.hook
                    .editOriginal("The channel was set to ${guild.getTextChannelById(newChannelId!!)?.asMention ?: "[*Channel not found*]"}")
                    .queue()
                return OK_STATUS
            }

            CANCEL_BUTTON_ID -> {
                event.hook.editOriginal("The channel was not changed").queue()
                return OK_STATUS
            }

            else -> {
                return Status(StatusCode.ERROR, "Unknown button")
            }
        }
    }

    override fun getCommandOptions(): List<OptionData> {
        return listOf(
            channelParam
        )
    }
}