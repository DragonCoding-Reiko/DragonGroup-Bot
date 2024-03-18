package net.dragoncoding.groupbot.discord.command.annotations

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Scope("prototype")
@Component
annotation class DiscordCommand()
