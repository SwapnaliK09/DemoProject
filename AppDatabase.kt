package `in`.vakrangee.crmCalling.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import `in`.vakrangee.hrms.ui.salesTracker.database.AttachmentEntity
import `in`.vakrangee.hrms.ui.salesTracker.database.AttendeeEntity
import `in`.vakrangee.hrms.ui.salesTracker.database.SalesTrackerDao
import `in`.vakrangee.hrms.ui.salesTracker.database.SalesTrackerEntity

@Database(
    entities = [SmartTrackEntity::class, SalesTrackerEntity::class, AttendeeEntity::class, AttachmentEntity::class],
    version = 3, // ✅ bumped from 2 -> 3 for the new SmartTrackEntity columns
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smartTrackDao(): SmartTrackDao
    abstract fun salesTrackerDao(): SalesTrackerDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * ✅ Proper migration — adds the new SmartTrack context columns
         * WITHOUT destroying existing rows. This is what preserves any
         * offline/unsynced records sitting in the queue across an app update.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE smart_track ADD COLUMN accuracy REAL")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN bearing REAL")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN altitude REAL")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN provider TEXT")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN activityType TEXT")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN isMockLocation INTEGER")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN isGpsEnabled INTEGER")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN deviceID TEXT")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN appVersion TEXT")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN networkType TEXT")
                db.execSQL("ALTER TABLE smart_track ADD COLUMN signalStrength INTEGER")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_track_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    // ⚠️ Kept ONLY as a safety net for installs on version 1 or older
                    // that have no migration path defined (pre-dates this fix).
                    // Anyone already on version 2 will go through MIGRATION_2_3
                    // above and keep their data. Remove this fallback entirely
                    // once you're confident no version-1 installs remain in the field.
                    .fallbackToDestructiveMigrationFrom(1)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}