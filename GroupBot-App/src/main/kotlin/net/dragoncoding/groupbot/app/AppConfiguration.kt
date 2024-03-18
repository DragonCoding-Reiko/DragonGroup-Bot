package net.dragoncoding.groupbot.app

import net.dragoncoding.groupbot.data.PersistenceConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(PersistenceConfiguration::class)
@ComponentScan(basePackages = ["net.dragoncoding.groupbot"])
class AppConfiguration {
}