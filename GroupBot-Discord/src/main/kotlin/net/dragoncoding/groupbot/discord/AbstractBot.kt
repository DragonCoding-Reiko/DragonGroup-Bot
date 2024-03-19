package net.dragoncoding.groupbot.discord

import net.dragoncoding.groupbot.common.utils.KeystoreReader
import net.dragoncoding.groupbot.common.utils.TimeUtils
import net.dragoncoding.groupbot.discord.interfaces.IDiscordBot
import net.dragoncoding.groupbot.discord.interfaces.IDiscordEventListener
import net.dragoncoding.groupbot.discord.utils.BotMessages
import net.dragoncoding.groupbot.discord.utils.BotMessages.BOT_SHUTDOWN_MESSAGE
import net.dragoncoding.groupbot.discord.utils.BotMessages.BOT_STARTUP_ACTIVITY_MESSAGE
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractBot : IDiscordBot {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${groupbot.keystore.file}")
    private lateinit var keystore: String

    @Value("\${groupbot.keystore.password}")
    private lateinit var keystorePass: String

    protected lateinit var shardManager: ShardManager

    /**
     * Starts the bot as Spring bean after Spring has loaded its context
     */
    override fun afterPropertiesSet() {
        val startTime = System.currentTimeMillis()

        shardManager = createShardManager() //open the connection to discord
        awaitJdaReady() //Wait until all Shards are ready
        initSlashCommands() //Add Slash-Commands to the JDAs

        onBotReady()

        shardManager.setStatus(OnlineStatus.ONLINE)
        shardManager.setActivity(getActivity())

        val durationInMs = System.currentTimeMillis() - startTime
        val startupMessage =
            BotMessages.format(BotMessages.BOT_STARTUP_MESSAGE, TimeUtils.formatMsToDurationInSeconds(durationInMs))
        logger.info(startupMessage)
    }

    /**
     * Stops the bot once Spring shuts down
     */
    override fun destroy() {
        val startTime = System.currentTimeMillis()

        onBotShutdown()

        shardManager.setStatus(OnlineStatus.OFFLINE)
        shardManager.shutdown()

        val durationInMs = System.currentTimeMillis() - startTime
        val shutdownMessage =
            BotMessages.format(BOT_SHUTDOWN_MESSAGE, TimeUtils.formatMsToDurationInSeconds(durationInMs))
        logger.info(shutdownMessage)
    }

    /**
     * Creates the ShardManager with the default implementation making it possible to
     * state [GatewayIntent]s, [CacheFlag]s and register EventListeners.
     *
     * @return the created [ShardManager]
     */
    protected fun createShardManager(): ShardManager {
        val builder = DefaultShardManagerBuilder.create(
            KeystoreReader.getValueFromKeystore("discord-token", keystorePass, keystore),
            getGatewayIntents()
        )

        builder.disableCache(getCacheFlagsToDisable())

        //Showing Startup-message and Idle Status til the bot starts up
        builder.setStatus(OnlineStatus.IDLE)
        builder.setActivity(Activity.playing(BOT_STARTUP_ACTIVITY_MESSAGE))

        builder.addEventListeners(getEventListeners())

        return builder.build()
    }

    /**
     * Blocks until the [ShardManager]'s [net.dv8tion.jda.api.JDA]s have connected.
     */
    private fun awaitJdaReady() {
        shardManager.shards.forEach { jda: JDA ->
            try {
                jda.awaitReady()
            } catch (e: InterruptedException) {
                logger.error(e.message, e)
            }
        }
    }

    /**
     * Updates all global commands for this bot.
     * So far only SlashCommands are allowed.
     *
     * @see net.dv8tion.jda.api.interactions.commands.build.Commands.slash
     */
    protected fun initSlashCommands() {
        shardManager.shards.forEach { jda: JDA ->
            val commands = jda.updateCommands()
            commands.addCommands(getCommands()).queue()
        }
    }

    /**
     * Provides all global commands this bot should have
     * @return a collection of [CommandData]
     *
     * @see AbstractBot.initSlashCommands
     * @see JDA.updateCommands
     * @see net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction.addCommands
     */
    protected abstract fun getCommands(): Collection<SlashCommandData>

    /**
     * Provides the required [GatewayIntent]s for this bot
     * @return an [EnumSet] containing the [GatewayIntent]s
     *
     * @see DefaultShardManagerBuilder.create
     */
    protected abstract fun getGatewayIntents(): EnumSet<GatewayIntent>

    /**
     * Provides the [CacheFlag]s that should be disabled on the bot
     * @return an [Collection] containing the [CacheFlag]s
     *
     * @see DefaultShardManagerBuilder.disableCache
     */
    protected abstract fun getCacheFlagsToDisable(): Collection<CacheFlag>

    /**
     * Provides the EventListeners that should be registered for this bot
     * @return an [Collection] containing the [IDiscordEventListener]s
     *
     * @see DefaultShardManagerBuilder.addEventListeners
     */
    protected abstract fun getEventListeners(): Collection<IDiscordEventListener>

    /**
     * Provides the activity the bot should show once being initialized and online
     * @return the [Activity] to show
     *
     * @see ShardManager.setActivity
     * @see DefaultShardManagerBuilder.setActivity
     */
    protected abstract fun getActivity(): Activity

    /**
     * Gets called when the bot has initialized and is connected to JDA.
     * Will be called before the bots [OnlineStatus] switches to [OnlineStatus.ONLINE]
     *
     * @see AbstractBot.afterPropertiesSet
     */
    @Suppress("EmptyMethod")
    protected open fun onBotReady() {
    }

    /**
     * Gets called before the bots [ShardManager] gets shut down
     *
     * @see AbstractBot.destroy
     * @see ShardManager.shutdown
     */
    @Suppress("EmptyMethod")
    protected open fun onBotShutdown() {
    }
}