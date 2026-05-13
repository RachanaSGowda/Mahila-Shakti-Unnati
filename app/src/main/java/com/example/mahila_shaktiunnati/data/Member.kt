package com.example.mahila_shaktiunnati.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val role: String = "Member",
    val phone: String = "",
    val totalSavings: Double = 0.0,
    val activeLoan: Double = 0.0
)