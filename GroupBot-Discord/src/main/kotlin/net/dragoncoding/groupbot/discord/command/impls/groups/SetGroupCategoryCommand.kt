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
class SetGroupCategoryCommand : AbstractDiscordSlashCommand() {
    companion object {
        const val OK_BUTTON_ID = "ok"
        const val CANCEL_BUTTON_ID = "cancel"
    }

    @Autowired
    lateinit var jdaUtils: JDAUtils

    override val parentCommandName: String? = "set"
    override val name: String = "groups-category"
    override val description: String = "Sets the category where group channels will be created"

    override val acknowledgeCommand: Boolean = false
    override val acknowledgeButton: Boolean = true

    val channelParam =
        OptionData(OptionType.CHANNEL, "category", "Sets the category where group channels will be created", true)

    var newCategoryId: Long? = null
    override fun runCommand(event: SlashCommandInteractionEvent): IStatus {
        val guild = event.guild ?: return Status(StatusCode.ERROR, "This command can only be used for guilds")
        val guildId = guild.idLong

        val groupGuild = jdaUtils.getGuild(guildId)
        val groupsCategory = groupGuild.specialChannels.firstOrNull { it.type == GroupBotChannelType.GROUPS_CATEGORY }

        val channelParam = event.getOption(channelParam.name)!!.asChannel
        if (channelParam.type != ChannelType.CATEGORY)
            return Status(StatusCode.ERROR, "The parameter 'category' has to be a category")

        val newGroupCategory = channelParam.asCategory()

        if (groupsCategory != null) {
            val oldGroupsCategory = guild.getCategoryById(groupsCategory.channelId)

            if (oldGroupsCategory != null) {
                if (oldGroupsCategory.idLong == newGroupCategory.idLong) {
                    event.reply("The category is already set as groups category").setEphemeral(true).queue()
                    return OK_STATUS
                }

                buttonId = generateUuid()
                newCategoryId = newGroupCategory.idLong
                deleteOriginal = true
                event.reply("Are you sure you want to replace the current guild category (${oldGroupsCategory.asMention}) with ${newGroupCategory.asMention}")
                    .setEphemeral(true)
                    .addActionRow(
                        Button.primary("$buttonId-$CANCEL_BUTTON_ID", "Keep Old Category"),
                        Button.success("$buttonId-$OK_BUTTON_ID", "Replace!")
                    )
                    .queue()
                return OK_STATUS
            }
        }

        groupGuild.specialChannels.add(
            DiscordGuildChannel(newGroupCategory.idLong, GroupBotChannelType.GROUPS_CHANNEL)
        )
        jdaUtils.saveGuild(groupGuild)
        event.reply("The category was set to ${newGroupCategory.asMention}").setEphemeral(true).queue()
        return OK_STATUS
    }

    /**
     * Will only be called when a category was already set
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
                groupGuild.specialChannels.removeIf { it.type == GroupBotChannelType.GROUPS_CATEGORY }
                //Add new channel
                groupGuild.specialChannels.add(
                    DiscordGuildChannel(newCategoryId!!, GroupBotChannelType.GROUPS_CATEGORY)
                )

                jdaUtils.saveGuild(groupGuild)
                event.hook
                    .editOriginal("The category was set to ${guild.getTextChannelById(newCategoryId!!)?.asMention ?: "[*Category not found*]"}")
                    .queue()
                return OK_STATUS
            }

            CANCEL_BUTTON_ID -> {
                event.hook.editOriginal("The category was not changed").queue()
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