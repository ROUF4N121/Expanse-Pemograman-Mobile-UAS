package com.rousoft.expanse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. SAKLAR NAVIGASI (Daftar Halaman yang Tersedia)
enum class AnalysisRoute { MAIN, DAILY, WEEKLY, MONTHLY }

@Composable
fun AnalysisScreen() {
    // 2. STATE UNTUK MENGINGAT HALAMAN SAAT INI
    var currentRoute by remember { mutableStateOf(AnalysisRoute.MAIN) }

    // 3. LOGIKA PERPINDAHAN HALAMAN
    when (currentRoute) {
        AnalysisRoute.MAIN -> AnalysisMainContent(
            onDailyClick = { currentRoute = AnalysisRoute.DAILY },
            onWeeklyClick = { currentRoute = AnalysisRoute.WEEKLY },
            onMonthlyClick = { currentRoute = AnalysisRoute.MONTHLY }
        )
        // Memanggil halaman dari file-file yang sudah kamu buat sebelumnya
        AnalysisRoute.DAILY -> DailyAnalysisScreen(onBack = { currentRoute = AnalysisRoute.MAIN })
        AnalysisRoute.WEEKLY -> WeeklyAnalysisScreen(onBack = { currentRoute = AnalysisRoute.MAIN })
        AnalysisRoute.MONTHLY -> MonthlyAnalysisScreen(onBack = { currentRoute = AnalysisRoute.MAIN })
    }
}

// 4. ISI HALAMAN UTAMA (Kode aslimu yang dibungkus fungsi baru)
@Composable
fun AnalysisMainContent(
    onDailyClick: () -> Unit,
    onWeeklyClick: () -> Unit,
    onMonthlyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.title_basic_analysis),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = montserrat
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memberikan aksi klik ke masing-masing kotak
            CalendarBox(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.label_daily),
                icon = Icons.Default.CalendarToday,
                onClick = onDailyClick
            )
            CalendarBox(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.label_weekly),
                icon = Icons.Default.DateRange,
                onClick = onWeeklyClick
            )
            CalendarBox(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.label_monthly),
                icon = Icons.Default.CalendarMonth,
                onClick = onMonthlyClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.title_predictive_analysis),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = montserrat
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.desc_predictive_analysis),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp,
                    fontFamily = montserrat
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0B2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.btn_start_analysis), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = montserrat)
                }
            }
        }
    }
}

// 5. PENAMBAHAN FUNGSI ONCLICK DI CALENDAR BOX
@Composable
fun CalendarBox(modifier: Modifier = Modifier, title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        // Menambahkan properti clickable agar bisa ditekan
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(0xFFF0F0F0), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.DarkGray,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            fontFamily = montserrat
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalysisPreview() { AnalysisScreen() }