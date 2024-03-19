package net.dragoncoding.groupbot.discord.tasks

import net.dragoncoding.groupbot.data.models.DiscordGuild
import net.dragoncoding.groupbot.data.repository.DiscordGuildRepository
import net.dragoncoding.groupbot.discord.utils.JDAUtils

class SyncGuildsOnStartupTask(
    private val guildIds: List<Long>,
    private val guildRepository: DiscordGuildRepository,
    private val jdaUtils: JDAUtils
) : Runnable {


    override fun run() {
        val guilds = guildRepository.findAll()
        val checkedGuilds = ArrayList<DiscordGuild>()

        guildIds.forEach { id ->
            val guild = jdaUtils.onGuildJoin(id)
            checkedGuilds.add(guild)
        }

        guilds.forEach { guild ->
            if (checkedGuilds.find { it.guildId == guild.guildId } != null) return@forEach

            jdaUtils.onGuildLeave(guild.guildId)
            checkedGuilds.add(guild)
        }
    }
}