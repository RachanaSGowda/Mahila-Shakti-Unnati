package com.example.mahila_shaktiunnati.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Member::class, Loan::class, Savings::class, LoanRepayment::class], version = 3, exportSchema = false)
abstract class ShgDatabase : RoomDatabase() {
    abstract fun shgDao(): ShgDao
    abstract fun loanDao(): LoanDao
    abstract fun repaymentDao(): RepaymentDao

    companion object {
        @Volatile private var INSTANCE: ShgDatabase? = null

        fun getDatabase(context: Context): ShgDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShgDatabase::class.java,
                    "shg_database"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
