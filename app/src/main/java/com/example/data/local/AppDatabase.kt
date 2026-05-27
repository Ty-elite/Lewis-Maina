package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        PropertyEntity::class,
        BookmarkEntity::class,
        InquiryEntity::class,
        ChatMessageEntity::class,
        SavedSearchEntity::class,
        ReviewEntity::class,
        LandlordNotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun propertyDao(): PropertyDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun inquiryDao(): InquiryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun reviewDao(): ReviewDao
    abstract fun landlordNotificationDao(): LandlordNotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kenyarent_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
