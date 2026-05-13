package com.example.mahila_shaktiunnati.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "loan_repayments",
    foreignKeys = [ForeignKey(
        entity = Loan::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LoanRepayment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val loanId: Int,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Dao
interface RepaymentDao {
    @Insert
    suspend fun insertRepayment(repayment: LoanRepayment)

    @Query("SELECT * FROM loan_repayments WHERE loanId = :loanId ORDER BY timestamp DESC")
    fun getRepaymentsForLoan(loanId: Int): Flow<List<LoanRepayment>>
}
