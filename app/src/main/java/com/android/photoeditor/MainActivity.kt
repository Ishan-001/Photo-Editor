package com.android.photoeditor

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.android.photoeditor.databinding.ActivityMainBinding
import com.android.photoeditor.viewmodel.MyViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val mBinding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val model: MyViewModel by lazy { ViewModelProvider(this).get(MyViewModel::class.java) }

    private val cropActivityResultContract = object : ActivityResultContract<Any?, Bitmap?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return createCropIntent(mUri)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
           return intent?.extras?.getParcelable("data")!!
        }
    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    private val selectImageFromGalleryResultContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                mUri = uri
                val mBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                model.selectImage(mBitmap)
        }
    }

    private lateinit var mUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        initViews()
        initObservers()
        initLaunchers()
    }

    private fun initLaunchers() {
        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContract) {
            it?.let { model.addCropTask(it) }
        }
    }

    private fun initObservers() {
        model.bitmap.observe(this, {
            mBinding.imageView.setImageBitmap(it)
        })
    }

    private fun initViews() {
        mBinding.undoButton.setOnClickListener { model.undoTask() }
        mBinding.selectImageButton.setOnClickListener { selectImageFromGallery() }
        mBinding.rotateImageButton.setOnClickListener { model.addRotateTask() }
        mBinding.cropImageButton.setOnClickListener { if (model.getTaskLength() > 0) cropActivityResultLauncher.launch(null) }
        mBinding.saveButton.setOnClickListener {
            model.saveImage(baseContext)
            makeSnackBar()
        }
    }

    private fun makeSnackBar() {
        Snackbar.make(mBinding.root, "Saved", Snackbar.LENGTH_SHORT).show()
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResultContract.launch("image/*")

    private fun createCropIntent(uri : Uri) : Intent {
        val cropIntent = Intent("com.android.camera.action.CROP")
        cropIntent.setDataAndType(uri, "image/*")
        cropIntent.putExtra("crop", true)
        cropIntent.putExtra("aspectX", 1)
        cropIntent.putExtra("aspectY", 1)
        cropIntent.putExtra("outputX", 128)
        cropIntent.putExtra("outputY", 128)
        cropIntent.putExtra("return-data", true)
        return cropIntent
    }

}