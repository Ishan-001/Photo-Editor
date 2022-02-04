package com.android.photoeditor.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.android.photoeditor.R
import java.io.File
import java.io.File.separator
import java.io.FileOutputStream
import java.io.OutputStream

class Editor {
    companion object {
        fun rotateImage(image: Bitmap) : Bitmap {
            val matrix = Matrix().apply { postRotate(90f) }
            val scaledBitmap = Bitmap.createScaledBitmap(image, image.width, image.height, true)

            return Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
        }

        fun saveImage(bitmap: Bitmap, context: Context) {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val values = contentValues()
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/" + context.getString(R.string.app_name)
                )
                values.put(MediaStore.Images.Media.IS_PENDING, true)

                val uri: Uri? = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                if (uri != null) {
                    saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    context.contentResolver.update(uri, values, null, null)
                }
            } else {
                val directory = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + separator + context.getString(
                        R.string.app_name
                    )
                )
                // getExternalStorageDirectory is deprecated in API 29

                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val fileName = System.currentTimeMillis().toString() + ".jpg"
                val file = File(directory, fileName)
                saveImageToStream(bitmap, FileOutputStream(file))
                val values = contentValues()
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }

        private fun contentValues() : ContentValues {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            return values
        }

        private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
            if (outputStream != null) {
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}