package com.rousoft.expanse.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Menyimpan data baru
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    // Mengambil semua data dari yang terbaru
    @Query("SELECT * FROM transaction_table ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // Mengambil total pengeluaran
    @Query("SELECT SUM(amount) FROM transaction_table WHERE type = 'Expense'")
    fun getTotalExpense(): Flow<Double?>

    // Mengambil data yang belum di-sync ke Firebase
    @Query("SELECT * FROM transaction_table WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<Transaction>

    // Menandai data sudah di-sync
    @Query("UPDATE transaction_table SET isSynced = 1 WHERE id = :transactionId")
    suspend fun markAsSynced(transactionId: Int)
}