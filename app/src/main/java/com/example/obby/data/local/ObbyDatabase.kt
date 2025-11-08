package com.example.obby.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 1,
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

        fun getDatabase(context: Context): ObbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ObbyDatabase::class.java,
                    "obby_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
