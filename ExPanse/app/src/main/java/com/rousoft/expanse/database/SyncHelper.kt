package com.rousoft.expanse.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun syncOfflineDataToFirebase(transactionDao: TransactionDao) {
    // Jalankan di jalur background (IO) agar tidak membuat aplikasi lag
    withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()

            // 1. Ambil semua data dari Room yang isSynced = 0 (false)
            val unsyncedList = transactionDao.getUnsyncedTransactions()

            if (unsyncedList.isEmpty()) {
                Log.d("SyncHelper", "Data sudah tersinkronisasi semua. Aman!")
                return@withContext
            }

            // 2. Loop untuk mengirim data satu per satu
            for (transaction in unsyncedList) {
                // Bungkus data menjadi Map untuk Firebase
                val firebaseData = hashMapOf(
                    "local_id" to transaction.id,
                    "title" to transaction.title,
                    "amount" to transaction.amount,
                    "category" to transaction.category,
                    "date" to transaction.date,
                    "type" to transaction.type
                )

                // 3. Kirim ke Firebase Firestore di dalam koleksi "transactions"
                // (Jika ada sistem login, kamu bisa menambahkan UID user ke path koleksinya)
                firestore.collection("transactions")
                    .add(firebaseData)
                    .await() // Menunggu hingga pengiriman ke cloud benar-benar sukses

                // 4. Jika berhasil melewati .await() (tidak error/tidak putus koneksi),
                // maka tandai data ini di Room lokal sebagai sudah tersinkron.
                transactionDao.markAsSynced(transaction.id)

                Log.d("SyncHelper", "Transaksi ${transaction.title} berhasil naik ke cloud!")
            }
        } catch (e: Exception) {
            // Jika tidak ada internet atau gagal, proses akan berhenti di sini,
            // isSynced akan tetap 0, dan sistem akan mencobanya lagi di lain waktu.
            Log.e("SyncHelper", "Gagal sinkronisasi: ${e.message}")
        }
    }
}