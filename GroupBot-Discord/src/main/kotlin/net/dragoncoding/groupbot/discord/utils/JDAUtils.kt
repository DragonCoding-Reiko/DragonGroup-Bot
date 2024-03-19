package net.dragoncoding.groupbot.discord.utils

import net.dragoncoding.groupbot.data.models.DiscordGuild
import net.dragoncoding.groupbot.data.repository.DiscordGuildRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JDAUtils {
    @Autowired
    lateinit var guildRepository: DiscordGuildRepository

    fun onGuildLeave(guildId: Long): DiscordGuild {
        val guild = guildRepository.findById(guildId).orElse(DiscordGuild(guildId = guildId))

        if (guild.joined)
            guild.joined = false

        guildRepository.save(guild)

        return guild
    }

    fun onGuildJoin(guildId: Long): DiscordGuild {
        val guild = guildRepository.findById(guildId).orElse(DiscordGuild(guildId = guildId))

        if (!guild.joined)
            guild.joined = true

        guildRepository.save(guild)

        return guild
    }
}