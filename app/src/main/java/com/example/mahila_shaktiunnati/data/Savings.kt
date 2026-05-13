package com.example.mahila_shaktiunnati.data

import androidx.room.*

@Entity(
    tableName = "savings",
    foreignKeys = [ForeignKey(
        entity = Member::class,
        parentColumns = ["id"],
        childColumns = ["memberId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Savings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val amount: Double,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "Savings",
    val status: String = "Paid"
)