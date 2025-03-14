package com.alishoumar.androidstorage.data.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load (null)
    }

    private fun createKey(): SecretKey{
        val keyGenerator = KeyGenParameterSpec.Builder(
            "secretKey", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(keyGenerator)
        }.generateKey()
    }

    private fun getKey(): SecretKey{
        val existingKey = keyStore.getKey("secretKey",null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }


    private val encryptionCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptionCipherForIv(iv:ByteArray):Cipher{
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    fun encrypt(bytes:ByteArray, outputSteam:OutputStream):ByteArray{
        val encryptedBytes = encryptionCipher.doFinal(bytes)
        outputSteam.use {
            it.write(encryptionCipher.iv.size)
            it.write(encryptionCipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return encryptedBytes
    }

    fun decrypt(inputStream:InputStream): ByteArray{
        return inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes= ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            getDecryptionCipherForIv(iv).doFinal(encryptedBytes)
        }
    }
}