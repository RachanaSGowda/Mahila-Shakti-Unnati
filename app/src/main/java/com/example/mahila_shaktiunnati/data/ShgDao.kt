package com.example.mahila_shaktiunnati.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShgDao {
    @Query("SELECT * FROM members")
    fun getAllMembers(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Int): Flow<Member?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavings(savings: Savings)

    @Query("SELECT * FROM savings WHERE memberId = :memberId")
    fun getSavingsForMember(memberId: Int): Flow<List<Savings>>

    @Query("UPDATE members SET totalSavings = totalSavings + :amount WHERE id = :memberId")
    suspend fun updateMemberSavings(memberId: Int, amount: Double)

    @Query("UPDATE members SET totalSavings = totalSavings - :amount WHERE id = :memberId")
    suspend fun deductMemberSavings(memberId: Int, amount: Double)

    @Query("UPDATE members SET activeLoan = :amount WHERE id = :memberId")
    suspend fun updateMemberActiveLoan(memberId: Int, amount: Double)

    @Query("UPDATE members SET activeLoan = activeLoan - :amount WHERE id = :memberId")
    suspend fun decreaseMemberActiveLoan(memberId: Int, amount: Double)

    @Query("SELECT SUM(totalSavings) FROM members")
    fun getTotalGroupSavings(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM savings WHERE memberId = :memberId AND status = 'Paid'")
    suspend fun getTotalSavings(memberId: Int): Double?
}