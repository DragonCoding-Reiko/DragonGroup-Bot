package net.dragoncoding.groupbot.data

import net.dragoncoding.groupbot.common.utils.KeystoreReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@Configuration
@EntityScan("net.dragoncoding.groupbot.data.models")
@EnableJpaRepositories("net.dragoncoding.groupbot.data.repository")
@PropertySource("classpath:persistence.properties")
class PersistenceConfiguration {
    @Value("\${spring.datasource.driverClassName:#{null}}")
    var dbDriverClassName: String? = null

    @Value("\${spring.datasource.url:#{null}}")
    var dbUrl: String? = null

    @Value("\${spring.datasource.username:#{null}}")
    var dbUsername: String? = null

    @Value("\${groupbot.keystore.file}")
    lateinit var keystore: String

    @Value("\${groupbot.keystore.password}")
    lateinit var keystorePass: String

    @Bean
    fun dataSource(): DataSource {
        val dataSource = DriverManagerDataSource()

        dataSource.setDriverClassName(
            dbDriverClassName ?: throw IllegalStateException("No driverClassName configured")
        )
        dataSource.url = dbUrl ?: throw IllegalStateException("No db url configured")
        dataSource.username = dbUsername ?: throw IllegalStateException("No db user configured")
        dataSource.password = KeystoreReader.getValueFromKeystore("db-password", keystorePass, keystore)

        return dataSource
    }
}