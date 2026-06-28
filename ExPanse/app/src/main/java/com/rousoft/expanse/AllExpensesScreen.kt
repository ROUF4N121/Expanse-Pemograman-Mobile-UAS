package com.rousoft.expanse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rousoft.expanse.database.AppDatabase
import com.rousoft.expanse.database.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AllExpensesContent() {
    val context = LocalContext.current

    // 1. Inisialisasi Database
    val db = remember { AppDatabase.getDatabase(context) }
    val transactionDao = db.transactionDao()

    // 2. Ambil Semua Data dari Database
    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())

    // State untuk mengingat tab mana yang diklik (0=Daily, 1=Weekly, 2=Monthly)
    var selectedFilter by remember { mutableIntStateOf(1) }

    // 3. Logika Filter Waktu (Harian, Mingguan, Bulanan)
    val calendar = Calendar.getInstance()
    val (startTime, endTime) = when (selectedFilter) {
        0 -> { // Daily (Hari Ini)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val end = calendar.timeInMillis
            Pair(start, end)
        }
        1 -> { // Weekly (Minggu Ini)
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            val end = calendar.timeInMillis
            Pair(start, end)
        }
        2 -> { // Monthly (Bulan Ini)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val end = calendar.timeInMillis
            Pair(start, end)
        }
        else -> Pair(0L, Long.MAX_VALUE)
    }

    // 4. Daftar Transaksi yang Sudah Difilter
    val filteredTransactions = transactions.filter { it.date in startTime until endTime }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Kotak Utama (Card Putih)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().weight(1f) // Gunakan weight agar list bisa digulir
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // 1. Custom Tab (Segmented Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCC80), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = listOf(
                        stringResource(R.string.label_daily),
                        stringResource(R.string.label_weekly),
                        stringResource(R.string.label_monthly)
                    )

                    filters.forEachIndexed { index, title ->
                        val isSelected = selectedFilter == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color(0xFFFFA500) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFilter = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.Black,
                                fontFamily = montserrat
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Daftar Transaksi atau Tampilan Kosong
                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_all_expenses),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontFamily = montserrat
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTransactions) { transaction ->
                            ExpenseListItem(transaction = transaction)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Komponen Desain untuk Satu Baris Transaksi
@Composable
fun ExpenseListItem(transaction: Transaction) {
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    val formattedAmount = "Rp ${formatter.format(transaction.amount)}"

    // Mengubah format timestamp menjadi tanggal yang mudah dibaca
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val dateString = dateFormat.format(Date(transaction.date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = montserrat,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${transaction.category} • $dateString",
                fontSize = 11.sp,
                color = Color.Gray,
                fontFamily = montserrat
            )
        }

        Box(
            modifier = Modifier
                .background(Color(0xFFFFE0B2), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = formattedAmount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                fontFamily = montserrat
            )
        }
    }
}