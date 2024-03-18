package net.dragoncoding.groupbot.common.utils

import java.io.FileInputStream
import java.nio.file.Paths
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.SecretKeyEntry
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


object KeystoreReader {
    private const val KEYSTORE_TYPE = "PKCS12"
    private const val PASSWORD_ALGORITHM = "PBEWithMD5AndDES"


    fun getValueFromKeystore(
        alias: String,
        storePassword: String,
        keystore: String,
        keyPassword: String = storePassword
    ): String {
        val path = Paths.get(keystore).toFile()
        FileInputStream(path).use {
            val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
            keyStore.load(it, storePassword.toCharArray())

            val passwordProtection = PasswordProtection(keyPassword.toCharArray())
            val secretKeyEntry = keyStore.getEntry(alias, passwordProtection)
                ?: throw IllegalStateException("No value for alias '$alias' in keystore '$path'!")

            val secretKeyFactory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM)
            val keySpec = secretKeyFactory.getKeySpec(
                (secretKeyEntry as SecretKeyEntry).secretKey,
                PBEKeySpec::class.java
            ) as PBEKeySpec

            return String(keySpec.password)
        }
    }
}