package com.example.tracktogether.face

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

class TakeFrontCameraPreview : ActivityResultContract<Uri, Pair<Boolean, Uri>>() {
    private lateinit var imageUri: Uri

    @CallSuper
    override fun createIntent(context: Context, input: Uri): Intent {
        imageUri = input
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, input)
        intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1)
        return intent
    }

    override fun getSynchronousResult(
        context: Context,
        input: Uri
    ): SynchronousResult<Pair<Boolean, Uri>>? = null

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Boolean, Uri> {
        return (resultCode == Activity.RESULT_OK) to imageUri
    }


}