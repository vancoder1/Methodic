package com.vio_0x.methodic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vio_0x.methodic.data.converters.DateConverter
import com.vio_0x.methodic.data.converters.StringListConverter

@Database(entities = [ToDoItem::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun todoItemDao(): ToDoItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "methodic_database"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this basic implementation.
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}