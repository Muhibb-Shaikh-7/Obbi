package com.example.obby.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.obby.data.local.dao.FolderDao
import com.example.obby.data.local.dao.NoteDao
import com.example.obby.data.local.dao.NoteLinkDao
import com.example.obby.data.local.dao.TagDao
import com.example.obby.data.local.entity.*

@Database(
    entities = [
        Note::class,
        Folder::class,
        Tag::class,
        NoteTagCrossRef::class,
        NoteLink::class
    ],
    version = 2,
    exportSchema = true
)
abstract class ObbyDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun noteLinkDao(): NoteLinkDao

    companion object {
        @Volatile
        private var INSTANCE: ObbyDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for hidden notes feature
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN hiddenCategoryAlias TEXT DEFAULT NULL"
                )
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN encryptedContentIv BLOB DEFAULT NULL"
                )
            }
        }

        fun getDatabase(context: Context): ObbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ObbyDatabase::class.java,
                    "obby_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
