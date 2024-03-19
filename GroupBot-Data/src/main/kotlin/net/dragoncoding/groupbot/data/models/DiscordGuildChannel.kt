package net.dragoncoding.groupbot.data.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.Id

@Entity
class DiscordGuildChannel(
    @Id
    @Column(name = "channelId", unique = true, nullable = false, updatable = false)
    val channelId: Long,

    @Column(name = "type", nullable = false)
    @Enumerated(STRING)
    val type: GroupBotChannelType
)