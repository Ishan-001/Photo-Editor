package com.android.photoeditor.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.photoeditor.model.Task
import com.android.photoeditor.util.Editor
import com.android.photoeditor.util.TaskType

class MyViewModel : ViewModel() {
    private val tasks: ArrayList<Task> = ArrayList()

    private val mBitmap = MutableLiveData<Bitmap>()
    val bitmap : LiveData<Bitmap>
        get() = mBitmap

    fun getTaskLength() = tasks.size

    fun addCropTask(image: Bitmap) {
        if (tasks.size > 0) {
            tasks.add(Task(TaskType.CROP, mBitmap.value!!))
            mBitmap.value = image
        }
    }

    fun addRotateTask() {
        if (tasks.size > 0) {
            tasks.add(Task(TaskType.ROTATE, mBitmap.value!!))
            mBitmap.value = Editor.rotateImage(mBitmap.value!!)
        }
    }

    fun undoTask() {
        if (tasks.size > 1) mBitmap.value = tasks.removeLast().image
    }

    fun saveImage(context: Context){
        if (tasks.size > 0) Editor.saveImage(mBitmap.value!!, context)
    }
    
    fun selectImage(image: Bitmap){
        tasks.clear()
        tasks.add(Task(TaskType.SELECT, image))
        mBitmap.value = image
    }
}