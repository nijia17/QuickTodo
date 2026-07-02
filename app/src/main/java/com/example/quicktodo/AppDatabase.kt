package com.example.quicktodo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task_table ADD COLUMN deadline INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE task_table ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
        database.execSQL("ALTER TABLE task_table ADD COLUMN priority TEXT NOT NULL DEFAULT 'LOW'")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task_table ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE task_table ADD COLUMN recurType TEXT NOT NULL DEFAULT 'NONE'")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE task_table ADD COLUMN completedAt INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [Task::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "todo_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
                INSTANCE = db
                db
            }
        }
    }
}
