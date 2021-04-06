package com.example.moneytracker
import MainActivity2
import androidx.room.*

@Database (entities = [(UserEntity::class)],version = 1,exportSchema = false)

abstract class AppDB : RoomDatabase(){
    abstract fun userDAO():UserDAO



    companion object{
        //        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                // Since we didn't alter the table, there's nothing else to do here.
//            }
//        }
        @Volatile
        private var INSTANCE : AppDB? = null
        fun getInstance(context: MainActivity):AppDB{
            synchronized(this){
                var instance = INSTANCE
                if(instance==null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java,
                        "Users"
                        //.addMigrations(MIGRATION_3_4)
                    ).build()
                }
                return instance
            }
        }

    }
}