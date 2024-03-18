package net.dragoncoding.groupbot.data

import net.dragoncoding.groupbot.common.utils.KeystoreReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
@PropertySource("classpath:persistence.properties")
@PropertySource("classpath:liquibase.properties")
class PersistenceConfiguration {
    companion object {
        var logger: Logger = LoggerFactory.getLogger(PersistenceConfiguration::class.java)
    }

    @Autowired
    lateinit var env: Environment

    @Value("\${groupbot.keystore.file}")
    lateinit var keystore: String

    @Value("\${groupbot.keystore.password}")
    lateinit var keystorePass: String

    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()

        dataSource.setDriverClassName(
            env.getProperty("driverClassName")
                ?: throw IllegalStateException("No driverClassName configured")
        )
        dataSource.url = env.getProperty("url") ?: throw IllegalStateException("No db url configured")
        dataSource.username = env.getProperty("user") ?: throw IllegalStateException("No db user configured")
        dataSource.password = KeystoreReader.getValueFromKeystore("db-password", keystorePass, keystore)

        return dataSource
    }
}