package ru.bendricks.qrappilcation.ui.generator

import android.graphics.Bitmap
import android.media.Image
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GeneratorViewModel : ViewModel() {

    val bitmap = MutableLiveData<Bitmap>()

    fun getBitmap() = bitmap.value

    fun updateBitmap(newBitmap: Bitmap){
        bitmap.value = newBitmap
    }

}