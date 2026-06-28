package com.rousoft.expanse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rousoft.expanse.database.AppDatabase
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportScreen() {
    val context = LocalContext.current

    // Inisialisasi Database
    val db = remember { AppDatabase.getDatabase(context) }
    val transactionDao = db.transactionDao()

    // Ambil Data dari Database secara Reaktif
    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())
    val totalExpenseFlow by transactionDao.getTotalExpense().collectAsState(initial = 0.0)

    // Konversi nilai Double dari totalExpenseFlow
    val totalExpense = totalExpenseFlow ?: 0.0

    // Format Rupiah
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))
    val formattedTotal = formatter.format(totalExpense)

    // Menghitung jumlah transaksi
    val expenseCount = transactions.size

    // Menentukan kategori pengeluaran terbesar (Top Expense)
    val topExpenseTransaction = transactions.maxByOrNull { it.amount }

    // Menghitung jumlah per kategori untuk Donut Chart
    val categorySumMap = transactions.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }

    // Kamus Warna Kategori
    val categoryColors = mapOf(
        "Transportation" to Color.Blue,
        "Healthcare" to Color.Red,
        "Entertainment" to Color.Yellow,
        "Recurring Payments" to Color.Magenta,
        "Shopping" to Color.Green,
        "Other Expenses" to Color.Cyan
    )

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(130.dp)) {
                if (totalExpense == 0.0) {
                    drawArc(
                        color = Color(0xFFE0E0E0),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx())
                    )
                } else {
                    // Menggambar Pie Chart berwarna berdasarkan kategori
                    var currentStartAngle = -90f
                    val total = totalExpense.toFloat()

                    categorySumMap.forEach { (category, amount) ->
                        val sweepAngle = (amount.toFloat() / total) * 360f
                        val color = categoryColors[category] ?: Color.Gray

                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 16.dp.toPx())
                        )
                        currentStartAngle += sweepAngle
                    }
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                BadgeItem(stringResource(R.string.title_lifetime_analysis), Color(0xFFFFCC80), isTextBadge = true)
                Spacer(modifier = Modifier.height(12.dp))
                BadgeItem(stringResource(R.string.cat_transportation), Color.Blue)
                BadgeItem(stringResource(R.string.cat_healthcare), Color.Red)
                BadgeItem(stringResource(R.string.cat_entertainment), Color.Yellow)
                BadgeItem(stringResource(R.string.cat_recurring), Color.Magenta)
                BadgeItem(stringResource(R.string.cat_shopping), Color.Green)
                BadgeItem(stringResource(R.string.cat_other), Color.Cyan)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BadgeItem(stringResource(R.string.label_lifetime_expenses), Color(0xFFFFA500), isTextBadge = true)
                    Row {
                        BadgeItem(stringResource(R.string.label_expense_count), Color(0xFFFFCC80), isTextBadge = true)
                        Spacer(modifier = Modifier.width(4.dp))
                        BadgeItem(expenseCount.toString(), Color(0xFFFFA500), isTextBadge = true) // Menampilkan jumlah transaksi
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Rp $formattedTotal", fontSize = 32.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.title_top_expense), fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat)
                    BadgeItem("Terbesar", Color(0xFFFFA500), isTextBadge = true)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (topExpenseTransaction == null) {
                    Text(stringResource(R.string.empty_top_expense), color = Color.Gray, fontSize = 14.sp, fontFamily = montserrat)
                } else {
                    Text(topExpenseTransaction.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = montserrat)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        BadgeItem("Rp ${formatter.format(topExpenseTransaction.amount)}", Color(0xFFFFA500), isTextBadge = true)
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeItem(topExpenseTransaction.category, Color(0xFFFFCC80), isTextBadge = true)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(text: String, color: Color, isTextBadge: Boolean = false) {
    if (isTextBadge) {
        Box(modifier = Modifier.background(color, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = montserrat)
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 10.sp, color = Color.DarkGray, fontFamily = montserrat)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportPreview() { ReportScreen() }