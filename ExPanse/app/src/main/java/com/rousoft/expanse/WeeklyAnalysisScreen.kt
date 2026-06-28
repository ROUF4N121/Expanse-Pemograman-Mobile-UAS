package com.rousoft.expanse

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyAnalysisScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val transactionDao = db.transactionDao()
    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfWeek = calendar.timeInMillis

    calendar.add(Calendar.DAY_OF_YEAR, 7)
    val endOfWeek = calendar.timeInMillis

    val weeklyTransactions = transactions.filter { it.date in startOfWeek until endOfWeek }
    val totalExpenseWeekly = weeklyTransactions.sumOf { it.amount }

    val categorySumMap = weeklyTransactions.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
    val topCategory = categorySumMap.maxByOrNull { it.value }?.key ?: "-"

    val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))
    val mostSpentDayEntry = weeklyTransactions.groupBy { dayFormat.format(Date(it.date)) }
        .mapValues { entry -> entry.value.sumOf { it.amount } }.maxByOrNull { it.value }
    val mostSpentDay = mostSpentDayEntry?.key ?: "-"

    val formatter = NumberFormat.getInstance(Locale("id", "ID"))

    // Kamus Warna Kategori
    val categoryColors = mapOf(
        "Transportation" to Color.Blue,
        "Healthcare" to Color.Red,
        "Entertainment" to Color.Yellow,
        "Recurring Payments" to Color.Magenta,
        "Shopping" to Color.Green,
        "Other Expenses" to Color.Cyan
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Text(stringResource(R.string.title_weekly_analysis), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = montserrat, color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GRAFIK DONAT
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(120.dp)) {
                if (weeklyTransactions.isEmpty()) {
                    drawArc(color = Color(0xFFE0E0E0), startAngle = 0f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 14.dp.toPx()))
                } else {
                    var currentStartAngle = -90f
                    val total = totalExpenseWeekly.toFloat()

                    categorySumMap.forEach { (category, amount) ->
                        val sweepAngle = (amount.toFloat() / total) * 360f
                        val color = categoryColors[category] ?: Color.Gray

                        drawArc(color = color, startAngle = currentStartAngle, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = 14.dp.toPx()))
                        currentStartAngle += sweepAngle
                    }
                }
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column {
                WeeklyCategoryBadge(stringResource(R.string.cat_transportation), Color.Blue)
                WeeklyCategoryBadge(stringResource(R.string.cat_healthcare), Color.Red)
                WeeklyCategoryBadge(stringResource(R.string.cat_entertainment), Color.Yellow)
                WeeklyCategoryBadge(stringResource(R.string.cat_recurring), Color.Magenta)
                WeeklyCategoryBadge(stringResource(R.string.cat_shopping), Color.Green)
                WeeklyCategoryBadge(stringResource(R.string.cat_other), Color.Cyan)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GRAFIK BATANG
        if (weeklyTransactions.isEmpty()) {
            WeeklyEmptyBarChart()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxExpense = categorySumMap.values.maxOrNull()?.toFloat() ?: 1f
                categorySumMap.forEach { (category, amount) ->
                    val barHeight = (amount.toFloat() / maxExpense) * 120f
                    Box(modifier = Modifier.width(24.dp).height(barHeight.dp).background(Color(0xFFFFA500), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF3E0), RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Lightbulb, contentDescription = "Insight", tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(stringResource(R.string.title_weekly_insight), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100), fontFamily = montserrat)
                Spacer(modifier = Modifier.height(4.dp))
                val insightText = if (weeklyTransactions.isEmpty()) stringResource(R.string.desc_weekly_insight) else "Puncak pengeluaranmu minggu ini terjadi pada hari $mostSpentDay."
                Text(insightText, fontSize = 12.sp, color = Color(0xFF5D4037), lineHeight = 18.sp, fontFamily = montserrat)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeeklyStatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.label_total_expense_weekly), value = "Rp ${formatter.format(totalExpenseWeekly)}")
            WeeklyStatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.label_most_spent_category), value = topCategory)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeeklyStatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.label_compared_to_last_week), value = "0%", valueColor = Color.Gray)
            WeeklyStatCard(modifier = Modifier.weight(1f), title = stringResource(R.string.label_most_spent_day), value = mostSpentDay)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WeeklyEmptyBarChart() {
    Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            repeat(4) { HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 1.dp) }
        }
        Text("No expenses recorded this week.", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium, fontFamily = montserrat, modifier = Modifier.background(Color.White).padding(horizontal = 8.dp))
    }
}

@Composable
fun WeeklyStatCard(modifier: Modifier = Modifier, title: String, value: String, valueColor: Color = Color.Black) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium, fontFamily = montserrat, lineHeight = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor, fontFamily = montserrat)
        }
    }
}

@Composable
fun WeeklyCategoryBadge(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 11.sp, color = Color.DarkGray, fontFamily = montserrat)
    }
}