package net.dragoncoding.groupbot.data.repository

import net.dragoncoding.groupbot.data.models.DiscordGuild
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DiscordGuildRepository : JpaRepository<DiscordGuild, Long>