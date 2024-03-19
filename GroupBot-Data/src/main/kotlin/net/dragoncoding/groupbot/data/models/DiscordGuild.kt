package net.dragoncoding.groupbot.data.models

import jakarta.persistence.*

@Entity
class DiscordGuild(
    @Id
    @Column(name = "guildId", unique = true, nullable = false, updatable = false)
    val guildId: Long,

    @Column(name = "joined")
    var joined: Boolean = true,

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL])
    val specialChannels: List<DiscordGuildChannel> = ArrayList()
)