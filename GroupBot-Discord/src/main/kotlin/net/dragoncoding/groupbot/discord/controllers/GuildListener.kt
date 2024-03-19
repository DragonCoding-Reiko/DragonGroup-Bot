package net.dragoncoding.groupbot.discord.controllers

import net.dragoncoding.groupbot.discord.interfaces.IDiscordEventListener
import net.dragoncoding.groupbot.discord.utils.JDAUtils
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class GuildListener : ListenerAdapter(), IDiscordEventListener {

    @Autowired
    lateinit var jdaUtils: JDAUtils

    override fun onGuildJoin(event: GuildJoinEvent) {
        jdaUtils.onGuildJoin(event.guild.idLong)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        jdaUtils.onGuildLeave(event.guild.idLong)
    }
}