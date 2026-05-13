package com.example.mahila_shaktiunnati.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "loans",
    foreignKeys = [ForeignKey(entity = Member::class, parentColumns = ["id"], childColumns = ["memberId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("memberId")]
)
data class Loan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val principalAmount: Double,
    val interestRate: Double,
    val timePeriodMonths: Int,
    val interestAmount: Double,
    val totalRepayment: Double,
    var currentBalance: Double = 0.0, // Track remaining amount
    val status: String = "Active" // "Active" or "Paid"
)

@Dao
interface LoanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: Loan)

    @Query("SELECT * FROM loans WHERE memberId = :memberId")
    fun getLoansForMember(memberId: Int): Flow<List<Loan>>

    @Query("SELECT * FROM loans WHERE memberId = :memberId AND status = 'Active' LIMIT 1")
    suspend fun getActiveLoanForMember(memberId: Int): Loan?

    @Query("UPDATE loans SET status = 'Paid', currentBalance = 0.0 WHERE id = :loanId")
    suspend fun payOffLoan(loanId: Int)
    
    @Query("UPDATE loans SET currentBalance = currentBalance - :amount WHERE id = :loanId")
    suspend fun updateLoanBalance(loanId: Int, amount: Double)
}
