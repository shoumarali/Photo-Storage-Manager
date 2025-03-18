package com.alishoumar.androidstorage.data.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
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
        load(null)
    }

    private fun createKey():SecretKey{

        val keyGenerator = KeyGenParameterSpec
            .Builder("secret", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()

        return KeyGenerator.getInstance(ALGORITHM).apply { init(keyGenerator) }.generateKey()
    }

    private fun getKey():SecretKey{
        val existingKey = keyStore.getEntry("secret",null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }


    fun encrypt(bytes: ByteArray, outputStream: OutputStream): ByteArray {
        return try {
            val encryptionCipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, getKey())
            }
            val encryptedBytes = encryptionCipher.doFinal(bytes)

            val dataOutputStream = DataOutputStream(outputStream)
            dataOutputStream.use {
                it.write(encryptionCipher.iv)
                it.writeInt(encryptedBytes.size)
                it.write(encryptedBytes)
            }
            encryptedBytes
        } catch (e: Exception) {
            Log.e("CryptoManager", "Encryption failed", e)
            ByteArray(0)
        }
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        return try {
            inputStream.use {
                val dataInputStream = DataInputStream(inputStream)
                val iv = ByteArray(16)
                dataInputStream.readFully(iv)
                val encryptedBytesSize = dataInputStream.readInt()

                val encryptedBytes = ByteArray(encryptedBytesSize)
                dataInputStream.readFully(encryptedBytes)

                val key = getKey()
                Log.d("tag", "decrypt: $key")

                val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
                }

                val decryptedBytes = cipher.doFinal(encryptedBytes)

                return decryptedBytes
            }
        } catch (e: Exception) {
            Log.e("CryptoManager", "Decryption failed", e)
            ByteArray(0)
        }
    }
}