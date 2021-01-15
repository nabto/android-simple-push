package com.nabto.simplepush


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nabto.simplepush.dao.PairedDeviceEntity
import com.nabto.simplepush.dao.PairedDevicesDao

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `paired_devices` ADD COLUMN `updated_fcm_token` INTEGER NOT NULL default 0")
    }
}

@Database(entities = [PairedDeviceEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pairedDevicesDao(): PairedDevicesDao


}

fun buildDb(context : Context) : AppDatabase {
    return Room.databaseBuilder(context,
        AppDatabase::class.java,
        "simple-push")
        .addMigrations(MIGRATION_1_2)
        .build();
}