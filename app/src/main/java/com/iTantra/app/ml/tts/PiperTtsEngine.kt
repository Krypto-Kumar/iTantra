package com.iTantra.app.ml.tts

import android.content.Context
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import java.io.File
import java.io.FileOutputStream

class PiperTtsEngine(
    private val context: Context
) {

    private val tts: OfflineTts

    init {
        val dataDir = copyEspeakData()

        val modelConfig = OfflineTtsModelConfig(
            vits = OfflineTtsVitsModelConfig(
                model = "models/tts/hi-IN/hi_IN-priyamvada-medium.onnx",
                tokens = "models/tts/hi-IN/tokens.txt",
                dataDir = dataDir
            )
        )

        val config = OfflineTtsConfig(
            model = modelConfig
        )

        tts = OfflineTts(
            assetManager = context.assets,
            config = config
        )
    }

    private fun copyEspeakData(): String {
        val assetPath = "models/tts/hi-IN/espeak-ng-data"
        val targetDir = File(
            context.getExternalFilesDir(null),
            assetPath
        )

        if (!targetDir.exists()) {
            copyAssetDirectory(assetPath, targetDir)
        }

        return targetDir.absolutePath
    }

    private fun copyAssetDirectory(
        assetPath: String,
        targetDir: File
    ) {
        targetDir.mkdirs()

        val files = context.assets.list(assetPath) ?: return

        for (file in files) {
            val sourcePath = "$assetPath/$file"
            val destination = File(targetDir, file)

            val children = context.assets.list(sourcePath)

            if (!children.isNullOrEmpty()) {
                copyAssetDirectory(sourcePath, destination)
            } else {
                context.assets.open(sourcePath).use { input ->
                    FileOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    fun synthesize(text: String) =
        tts.generate(text = text)

    fun release() {
        tts.release()
    }
}