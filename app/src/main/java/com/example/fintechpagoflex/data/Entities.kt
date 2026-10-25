package com.example.fintechpagoflex.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class Commitment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val company: String,
    val concept: String,
    val amount: Double,
    val dueDate: Long,
    val status: String,
    val photoUri: String?
)

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commitmentId: Long,
    val amount: Double,
    val date: Long,
    val paymentMethod: String
)

@Entity(tableName = "recommendations")
data class Recommendation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val organizationName: String,
    val type: String,
    val reason: String
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val commitmentId: Long,
    val date: Long,
    val time: Long,
    val message: String
)

data class PaymentHistoryItem(
    val paymentId: Long,
    val company: String,
    val concept: String,
    val amount: Double,
    val date: Long,
    val paymentMethod: String
)