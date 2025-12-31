package com.example.visoragallery.data.database
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AlbumDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "albums.db"
        private const val DATABASE_VERSION = 1

        // Albums table
        private const val TABLE_ALBUMS = "albums"
        private const val COLUMN_ALBUM_ID = "id"
        private const val COLUMN_ALBUM_NAME = "name"
        private const val COLUMN_ALBUM_CREATED_AT = "created_at"

        // Album photos junction table
        private const val TABLE_ALBUM_PHOTOS = "album_photos"
        private const val COLUMN_AP_ALBUM_ID = "album_id"
        private const val COLUMN_AP_PHOTO_PATH = "photo_path"

        // Favorites table
        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_FAV_ID = "id"
        private const val COLUMN_FAV_PATH = "path"

        // Private photos table
        private const val TABLE_PRIVATE = "private_photos"
        private const val COLUMN_PRIVATE_ID = "id"
        private const val COLUMN_PRIVATE_PATH = "path"

        @Volatile
        private var instance: AlbumDatabase? = null

        fun getInstance(context: Context): AlbumDatabase {
            return instance ?: synchronized(this) {
                instance ?: AlbumDatabase(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create albums table
        db.execSQL("""
            CREATE TABLE $TABLE_ALBUMS (
                $COLUMN_ALBUM_ID TEXT PRIMARY KEY,
                $COLUMN_ALBUM_NAME TEXT NOT NULL,
                $COLUMN_ALBUM_CREATED_AT INTEGER NOT NULL
            )
        """)

        // Create album_photos junction table
        db.execSQL("""
            CREATE TABLE $TABLE_ALBUM_PHOTOS (
                $COLUMN_AP_ALBUM_ID TEXT NOT NULL,
                $COLUMN_AP_PHOTO_PATH TEXT NOT NULL,
                PRIMARY KEY ($COLUMN_AP_ALBUM_ID, $COLUMN_AP_PHOTO_PATH),
                FOREIGN KEY ($COLUMN_AP_ALBUM_ID) REFERENCES $TABLE_ALBUMS($COLUMN_ALBUM_ID)
                    ON DELETE CASCADE
            )
        """)

        // Create favorites table
        db.execSQL("""
            CREATE TABLE $TABLE_FAVORITES (
                $COLUMN_FAV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FAV_PATH TEXT UNIQUE NOT NULL
            )
        """)

        // Create private photos table
        db.execSQL("""
            CREATE TABLE $TABLE_PRIVATE (
                $COLUMN_PRIVATE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PRIVATE_PATH TEXT UNIQUE NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUM_PHOTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALBUMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRIVATE")
        onCreate(db)
    }

    // ==================== ALBUM OPERATIONS ====================

    fun createAlbum(albumId: String, albumName: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_ALBUM_ID, albumId)
                put(COLUMN_ALBUM_NAME, albumName)
                put(COLUMN_ALBUM_CREATED_AT, System.currentTimeMillis())
            }
            db.insert(TABLE_ALBUMS, null, values) != -1L
        } catch (e: Exception) {
            android.util.Log.e("AlbumDatabase", "Error creating album", e)
            false
        }
    }

    fun getUserAlbums(): List<Pair<String, String>> {
        val albums = mutableListOf<Pair<String, String>>()
        val db = readableDatabase

        db.query(
            TABLE_ALBUMS,
            arrayOf(COLUMN_ALBUM_ID, COLUMN_ALBUM_NAME),
            null, null, null, null,
            "$COLUMN_ALBUM_CREATED_AT DESC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ALBUM_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(COLUMN_ALBUM_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex)
                albums.add(Pair(id, name))
            }
        }

        return albums
    }

    fun getAlbumPhotoCount(albumId: String): Int {
        val db = readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_ALBUM_PHOTOS WHERE $COLUMN_AP_ALBUM_ID = ?",
            arrayOf(albumId)
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getInt(0)
            }
        }
        return 0
    }

    fun getAlbumPhotoPaths(albumId: String): List<String> {
        val paths = mutableListOf<String>()
        val db = readableDatabase

        db.query(
            TABLE_ALBUM_PHOTOS,
            arrayOf(COLUMN_AP_PHOTO_PATH),
            "$COLUMN_AP_ALBUM_ID = ?",
            arrayOf(albumId),
            null, null, null
        ).use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(COLUMN_AP_PHOTO_PATH)

            while (cursor.moveToNext()) {
                paths.add(cursor.getString(pathIndex))
            }
        }

        return paths
    }

    fun addPhotosToAlbum(albumId: String, photoPaths: List<String>): Boolean {
        val db = writableDatabase
        return try {
            db.beginTransaction()

            photoPaths.forEach { path ->
                val values = ContentValues().apply {
                    put(COLUMN_AP_ALBUM_ID, albumId)
                    put(COLUMN_AP_PHOTO_PATH, path)
                }
                db.insertWithOnConflict(
                    TABLE_ALBUM_PHOTOS,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            android.util.Log.e("AlbumDatabase", "Error adding photos to album", e)
            false
        } finally {
            db.endTransaction()
        }
    }

    fun deleteAlbum(albumId: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(TABLE_ALBUMS, "$COLUMN_ALBUM_ID = ?", arrayOf(albumId)) > 0
        } catch (e: Exception) {
            android.util.Log.e("AlbumDatabase", "Error deleting album", e)
            false
        }
    }

    fun renameAlbum(albumId: String, newName: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_ALBUM_NAME, newName)
            }
            db.update(
                TABLE_ALBUMS,
                values,
                "$COLUMN_ALBUM_ID = ?",
                arrayOf(albumId)
            ) > 0
        } catch (e: Exception) {
            android.util.Log.e("AlbumDatabase", "Error renaming album", e)
            false
        }
    }

    // ==================== FAVORITES OPERATIONS ====================

    fun addToFavorites(photoPath: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_FAV_PATH, photoPath)
            }
            db.insertWithOnConflict(
                TABLE_FAVORITES,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            ) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun removeFromFavorites(photoPath: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(TABLE_FAVORITES, "$COLUMN_FAV_PATH = ?", arrayOf(photoPath)) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun isFavorite(photoPath: String): Boolean {
        val db = readableDatabase
        db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAV_ID),
            "$COLUMN_FAV_PATH = ?",
            arrayOf(photoPath),
            null, null, null
        ).use { cursor ->
            return cursor.count > 0
        }
    }

    fun getFavoritePaths(): List<String> {
        val paths = mutableListOf<String>()
        val db = readableDatabase

        db.query(
            TABLE_FAVORITES,
            arrayOf(COLUMN_FAV_PATH),
            null, null, null, null, null
        ).use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(COLUMN_FAV_PATH)

            while (cursor.moveToNext()) {
                paths.add(cursor.getString(pathIndex))
            }
        }

        return paths
    }

    // ==================== PRIVATE PHOTOS OPERATIONS ====================

    fun addToPrivate(photoPath: String): Boolean {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_PRIVATE_PATH, photoPath)
            }
            db.insertWithOnConflict(
                TABLE_PRIVATE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            ) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun removeFromPrivate(photoPath: String): Boolean {
        val db = writableDatabase
        return try {
            db.delete(TABLE_PRIVATE, "$COLUMN_PRIVATE_PATH = ?", arrayOf(photoPath)) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getPrivatePaths(): List<String> {
        val paths = mutableListOf<String>()
        val db = readableDatabase

        db.query(
            TABLE_PRIVATE,
            arrayOf(COLUMN_PRIVATE_PATH),
            null, null, null, null, null
        ).use { cursor ->
            val pathIndex = cursor.getColumnIndexOrThrow(COLUMN_PRIVATE_PATH)

            while (cursor.moveToNext()) {
                paths.add(cursor.getString(pathIndex))
            }
        }

        return paths
    }
}