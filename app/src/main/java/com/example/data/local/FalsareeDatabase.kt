package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        PartnerEntity::class,
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        OrderActivityLogEntity::class,
        OnboardingPageEntity::class,
        HomeSectionEntity::class,
        CouponEntity::class,
        DriverProfileEntity::class,
        SupportTicketEntity::class,
        CustomerAddressEntity::class,
        NotificationEntity::class,
        AppSettingsEntity::class,
        FavoritePartnerEntity::class,
        OrderReviewEntity::class,
        DriverPayoutRequestEntity::class,
        DriverShiftAssignmentEntity::class,
        DriverDispatchEventEntity::class,
        DriverPerformanceEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(FalsareeTypeConverters::class)
abstract class FalsareeDatabase : RoomDatabase() {

    abstract fun dao(): FalsareeDao

    companion object {
        @Volatile
        private var INSTANCE: FalsareeDatabase? = null

        fun getDatabase(context: Context): FalsareeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FalsareeDatabase::class.java,
                    "falsaree3_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
