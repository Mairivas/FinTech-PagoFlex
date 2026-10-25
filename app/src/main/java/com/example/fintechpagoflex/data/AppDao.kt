package com.example.fintechpagoflex.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert
    suspend fun insertCommitment(commitment: Commitment): Long

    @Insert
    suspend fun insertPayment(payment: Payment): Long

    @Insert
    suspend fun insertRecommendation(recommendation: Recommendation)

    @Insert
    suspend fun insertReminder(reminder: Reminder): Long

    @Insert
    suspend fun insertCommitments(commitments: List<Commitment>): List<Long>

    @Insert
    suspend fun insertRecommendations(recommendations: List<Recommendation>)

    @Query("SELECT * FROM commitments")
    fun getAllCommitments(): Flow<List<Commitment>>

    @Query("SELECT * FROM commitments WHERE id = :id")
    fun getCommitmentById(id: Long): Flow<Commitment>

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentByIdDirect(id: Long): Commitment?

    @Query("SELECT COUNT(*) FROM commitments")
    suspend fun getCommitmentCount(): Int

    @Query("UPDATE commitments SET photoUri = :uri WHERE id = :id")
    suspend fun updateCommitmentPhoto(id: Long, uri: String)

    @Query("UPDATE commitments SET status = :status WHERE id = :id")
    suspend fun updateCommitmentStatus(id: Long, status: String)

    @Query("SELECT p.id as paymentId, c.company, c.concept, p.amount, p.date, p.paymentMethod FROM payments p INNER JOIN commitments c ON p.commitmentId = c.id ORDER BY p.date DESC")
    fun getPaymentHistory(): Flow<List<PaymentHistoryItem>>

    @Query("SELECT p.id as paymentId, c.company, c.concept, p.amount, p.date, p.paymentMethod FROM payments p INNER JOIN commitments c ON p.commitmentId = c.id WHERE p.id = :paymentId")
    fun getPaymentHistoryById(paymentId: Long): Flow<PaymentHistoryItem?>

    @Query("SELECT p.id as paymentId, c.company, c.concept, p.amount, p.date, p.paymentMethod FROM payments p INNER JOIN commitments c ON p.commitmentId = c.id WHERE p.commitmentId = :commitmentId ORDER BY p.id DESC LIMIT 1")
    fun getPaymentHistoryByCommitmentId(commitmentId: Long): Flow<PaymentHistoryItem?>

    @Query("SELECT id FROM payments WHERE commitmentId = :commitmentId ORDER BY id DESC LIMIT 1")
    suspend fun getPaymentIdForCommitment(commitmentId: Long): Long?

    @Query("SELECT * FROM recommendations ORDER BY id DESC")
    fun getAllRecommendations(): Flow<List<Recommendation>>

    @Query("SELECT * FROM reminders ORDER BY id DESC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Transaction
    suspend fun processPaymentTransaction(commitmentId: Long, amount: Double, paymentMethod: String, paymentDate: String): Boolean {
        val commitment = getCommitmentByIdDirect(commitmentId) ?: return false
        if (commitment.status.equals("Pagado", ignoreCase = true)) return false
        val dateLong = paymentDate.toLongOrNull() ?: System.currentTimeMillis()
        insertPayment(Payment(commitmentId = commitmentId, amount = amount, date = dateLong, paymentMethod = paymentMethod))
        updateCommitmentStatus(commitmentId, "Pagado")
        return true
    }
}