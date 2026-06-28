package com.rousoft.expanse.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_table")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,        // Nama pengeluaran (misal: "Makan Siang")
    val amount: Double,       // Nominal (misal: 25000.0)
    val category: String,     // Kategori (misal: "Food")
    val date: Long,           // Tanggal (disimpan dalam format angka/timestamp)
    val type: String,         // "Expense" (Pengeluaran) atau "Income" (Pemasukan)
    val isSynced: Boolean = false // Penanda apakah sudah masuk Firebase (0 = Belum, 1 = Sudah)
)