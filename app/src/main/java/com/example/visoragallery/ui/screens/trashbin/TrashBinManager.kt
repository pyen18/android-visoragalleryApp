package com.example.visoragallery.ui.screens.trashbin
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

class TrashBinManager(private val context: Context) {

    private val trashBinPath: String

    init {
        trashBinPath = initTrashBin()
    }

    private fun initTrashBin(): String {
        val internalStorage = context.filesDir
        val trashBinDir = File(internalStorage, "trash_bin")

        if (!trashBinDir.exists()) {
            trashBinDir.mkdir()
        }

        // Create .nomedia file to hide from gallery
        val nomediaFile = File(trashBinDir, ".nomedia")
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile()
        }

        return trashBinDir.absolutePath
    }

    suspend fun moveToTrash(photoPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(photoPath)
            if (!sourceFile.exists()) return@withContext false

            val trashFile = File(trashBinPath, sourceFile.name)

            // If file with same name exists, add timestamp
            val finalTrashFile = if (trashFile.exists()) {
                val timestamp = System.currentTimeMillis()
                val nameWithoutExt = sourceFile.nameWithoutExtension
                val ext = sourceFile.extension
                File(trashBinPath, "${nameWithoutExt}_${timestamp}.${ext}")
            } else {
                trashFile
            }

            // Move file
            moveFile(sourceFile, finalTrashFile)

            // Notify media scanner
            notifyMediaScanner(photoPath)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restorePhoto(trashPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val trashFile = File(trashPath)
            if (!trashFile.exists()) return@withContext false

            // Restore to Pictures directory
            val picturesDir = File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                ), "VisoraGallery"
            )

            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }

            val restoredFile = File(picturesDir, trashFile.name)

            // If file with same name exists, add timestamp
            val finalRestoredFile = if (restoredFile.exists()) {
                val timestamp = System.currentTimeMillis()
                val nameWithoutExt = trashFile.nameWithoutExtension
                val ext = trashFile.extension
                File(picturesDir, "${nameWithoutExt}_restored_${timestamp}.${ext}")
            } else {
                restoredFile
            }

            moveFile(trashFile, finalRestoredFile)

            // Notify media scanner
            notifyMediaScanner(finalRestoredFile.absolutePath)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun permanentlyDelete(trashPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(trashPath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getTrashFiles(): List<File> {
        val trashBinDir = File(trashBinPath)

        if (!trashBinDir.exists() || !trashBinDir.isDirectory) {
            return emptyList()
        }

        return trashBinDir.listFiles()
            ?.filter { !it.name.equals(".nomedia", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    suspend fun emptyTrash(): Boolean = withContext(Dispatchers.IO) {
        try {
            val files = getTrashFiles()
            files.forEach { it.delete() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun moveFile(source: File, destination: File) {
        destination.parentFile?.mkdirs()

        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.channel.transferTo(
                    0,
                    input.channel.size(),
                    output.channel
                )
            }
        }

        source.delete()
    }


    private fun notifyMediaScanner(path: String) {
        val file = File(path)
        val uri = Uri.fromFile(file)
        val scanFileIntent = android.content.Intent(
            android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
            uri
        )
        context.sendBroadcast(scanFileIntent)
    }

    companion object {
        private var instance: TrashBinManager? = null

        fun getInstance(context: Context): TrashBinManager {
            return instance ?: synchronized(this) {
                instance ?: TrashBinManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}