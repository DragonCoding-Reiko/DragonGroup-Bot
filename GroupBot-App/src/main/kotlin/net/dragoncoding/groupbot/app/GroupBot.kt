package net.dragoncoding.groupbot.app

import net.dragoncoding.groupbot.data.repository.DiscordGuildRepository
import net.dragoncoding.groupbot.discord.AbstractBot
import net.dragoncoding.groupbot.discord.controllers.CommandManager
import net.dragoncoding.groupbot.discord.controllers.GuildListener
import net.dragoncoding.groupbot.discord.controllers.SlashCommandListener
import net.dragoncoding.groupbot.discord.interfaces.IDiscordEventListener
import net.dragoncoding.groupbot.discord.tasks.SyncGuildsOnStartupTask
import net.dragoncoding.groupbot.discord.utils.JDAUtils
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.*

@Component
class GroupBot : AbstractBot() {

    @Autowired
    private lateinit var context: ApplicationContext

    override fun getCommands(): Collection<SlashCommandData> {
        return context.getBean(CommandManager::class.java).jdaCommandManager.buildJDACommands()
    }

    override fun getGatewayIntents(): EnumSet<GatewayIntent> {
        return EnumSet.of(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.MESSAGE_CONTENT
        )
    }

    override fun getCacheFlagsToDisable(): Collection<CacheFlag> {
        return listOf(
            CacheFlag.ACTIVITY,
            CacheFlag.EMOJI,
            CacheFlag.STICKER,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.SCHEDULED_EVENTS
        )
    }

    override fun getEventListeners(): Collection<IDiscordEventListener> {
        return listOf(
            context.getBean(SlashCommandListener::class.java),
            context.getBean(GuildListener::class.java)
        )
    }

    override fun getActivity(): Activity {
        return Activity.watching("you all play games :3")
    }

    override fun onBotReady() {
        SyncGuildsOnStartupTask(
            shardManager.guilds.map { it.idLong },
            context.getBean(DiscordGuildRepository::class.java),
            context.getBean(JDAUtils::class.java)
        ).run()
    }

    override fun onBotShutdown() {
        super.onBotShutdown()
    }
}