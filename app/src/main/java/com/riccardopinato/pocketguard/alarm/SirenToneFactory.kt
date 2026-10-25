package com.riccardopinato.pocketguard.alarm

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.sin

object SirenToneFactory {
    fun getOrCreate(context: Context): File {
        val file = File(context.cacheDir, "pocket_guard_siren.wav")

        if (file.exists() && file.length() > 44L) {
            return file
        }

        val sampleRate = 44_100
        val durationSeconds = 4
        val sampleCount = sampleRate * durationSeconds
        val pcm = ByteArray(sampleCount * 2)

        for (i in 0 until sampleCount) {
            val second = i.toDouble() / sampleRate
            val segment = ((second / 0.22).toInt() % 2)
            val frequency = if (segment == 0) 880.0 else 1320.0

            val envelope =
                if ((i % sampleRate) < 1500) {
                    (i % sampleRate) / 1500.0
                } else {
                    1.0
                }

            val sample = (
                sin(2.0 * PI * frequency * second) *
                    0.82 *
                    envelope *
                    Short.MAX_VALUE
                ).toInt().toShort()

            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] =
                ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        FileOutputStream(file).use { output ->
            writeWaveHeader(
                output = output,
                dataSize = pcm.size,
                sampleRate = sampleRate
            )
            output.write(pcm)
        }

        return file
    }

    private fun writeWaveHeader(
        output: FileOutputStream,
        dataSize: Int,
        sampleRate: Int
    ) {
        val byteRate = sampleRate * 2
        val header = ByteArray(44)

        fun putString(offset: Int, value: String) {
            value.toByteArray(Charsets.US_ASCII)
                .copyInto(header, offset)
        }

        fun putInt(offset: Int, value: Int) {
            header[offset] = (value and 0xFF).toByte()
            header[offset + 1] = ((value shr 8) and 0xFF).toByte()
            header[offset + 2] = ((value shr 16) and 0xFF).toByte()
            header[offset + 3] = ((value shr 24) and 0xFF).toByte()
        }

        fun putShort(offset: Int, value: Int) {
            header[offset] = (value and 0xFF).toByte()
            header[offset + 1] = ((value shr 8) and 0xFF).toByte()
        }

        putString(0, "RIFF")
        putInt(4, 36 + dataSize)
        putString(8, "WAVE")
        putString(12, "fmt ")
        putInt(16, 16)
        putShort(20, 1)
        putShort(22, 1)
        putInt(24, sampleRate)
        putInt(28, byteRate)
        putShort(32, 2)
        putShort(34, 16)
        putString(36, "data")
        putInt(40, dataSize)

        output.write(header)
    }
}
