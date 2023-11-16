/*
 * Copyright 2021 Shubham Panchal
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.tracktogether.face.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer

// Mask Detection model
// Source -> https://github.com/achen353/Face-Mask-Detector
class MaskDetectionModel(context: Context) {

    val mask = "mask"
    val noMask = "no mask"

    private val imgSize = 224
    private val numClasses = 2
    private val classIndexToLabel = mapOf(
        0 to mask,
        1 to noMask,
    )
    private val modelName = "mask_detector.tflite"

    private var interpreter: Interpreter
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add(ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))
        .build()

    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            // Add the GPU Delegate if supported.
            // See -> https://www.tensorflow.org/lite/performance/gpu#android
            if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                addDelegate(GpuDelegate(CompatibilityList().bestOptionsForThisDevice))
            } else {
                // Number of threads for computation
                setNumThreads(4)
            }
            setUseXNNPACK(true)
        }
        interpreter = Interpreter(FileUtil.loadMappedFile(context, modelName), interpreterOptions)
    }


    // Predict the emotion given the cropped Bitmap
    fun detectMask(image: Bitmap): String {
        return classIndexToLabel[runModel(processTensorImage(image)).argmax()]!!
    }


    // Kotlin Extension function for arg max.
    // See https://kotlinlang.org/docs/extensions.html
    private fun FloatArray.argmax(): Int {
        return this.indexOfFirst { it == this.maxOrNull()!! }
    }


    // Run the mask detection model.
    private fun runModel(inputs: Any): FloatArray {
        val t1 = System.currentTimeMillis()
        val modelOutputs = Array(1) { FloatArray(numClasses) }
        interpreter.run(inputs, modelOutputs)
        Log.i(
            "Performance",
            "Mask detection model inference speed in ms : ${System.currentTimeMillis() - t1}"
        )
        return modelOutputs[0]
    }


    // Process the given bitmap and convert it to a ByteBuffer
    private fun processTensorImage(image: Bitmap): ByteBuffer {
        return imageTensorProcessor.process(TensorImage.fromBitmap(image)).buffer
    }


}