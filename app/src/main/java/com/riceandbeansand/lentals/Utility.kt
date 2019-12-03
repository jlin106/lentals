package com.riceandbeansand.lentals

import android.net.Uri
import android.util.Log
import com.facebook.FacebookSdk.getCacheDir
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun getImageFileFromGSUrlWithCache(gsPath: String, cacheDir: File, callback: (File) -> Unit) {
    val MAX_DOWNLOAD_SIZE = 20*1024*1024L
    val storage = FirebaseStorage.getInstance();
    val imageRef = storage.getReferenceFromUrl(gsPath)
    val imageFile = File(cacheDir.absolutePath + "/" + imageRef.name)
    if (imageFile.exists()) {
        callback(imageFile)
    } else {
        imageRef.getFile(imageFile).addOnSuccessListener {
            callback(imageFile)
        }
    }
}

fun setImageFileToGSUrlWithCache(gsPath: String, cacheDir: File, imageStream: InputStream) {
    val storage = FirebaseStorage.getInstance();
    val imageRef = storage.getReferenceFromUrl(gsPath)
    val imageFile = File(cacheDir.absolutePath + "/" + imageRef.name)
    val imageFileOutput = FileOutputStream(imageFile)

    val buffer = ByteArray(1024)
    while (imageStream.read(buffer, 0, buffer.size) >= 0) {
        imageFileOutput.write(buffer, 0, buffer.size)
    }
    imageFileOutput.close()

    val uploadTask = imageRef.putFile(Uri.fromFile(imageFile))
    uploadTask.addOnFailureListener{
        Log.d("app", "Failed to upload image blob", it);
    }
}
