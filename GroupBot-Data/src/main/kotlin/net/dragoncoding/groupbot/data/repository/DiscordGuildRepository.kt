package net.dragoncoding.groupbot.data.repository

import net.dragoncoding.groupbot.data.models.DiscordGuild
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DiscordGuildRepository : JpaRepository<DiscordGuild, Long> {
    fun findFirstByGuildIdAndJoinedTrue(guildId: Long): Optional<DiscordGuild>
}