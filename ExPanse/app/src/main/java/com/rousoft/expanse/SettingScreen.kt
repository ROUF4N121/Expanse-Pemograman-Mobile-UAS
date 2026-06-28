package com.rousoft.expanse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Agar bisa di-scroll jika layar kecil
            .padding(horizontal = 24.dp)
    ) {

        // --- SECTION: PERSONALIZATION ---
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.header_personalization),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = montserrat
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(title = stringResource(R.string.setting_language), subtitle = stringResource(R.string.setting_language_desc))
        SettingItem(title = stringResource(R.string.setting_theme), subtitle = stringResource(R.string.setting_theme_desc))

        // --- SECTION: CACHE ---
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.header_cache),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = montserrat
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(title = stringResource(R.string.setting_delete_cache), subtitle = stringResource(R.string.setting_delete_cache_desc))
        SettingItem(title = stringResource(R.string.setting_cache_size), subtitle = stringResource(R.string.setting_cache_size_desc))

        // --- SECTION: CREDIT ---
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.header_credit),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontFamily = montserrat
        )
        Spacer(modifier = Modifier.height(8.dp))
        SettingItem(title = stringResource(R.string.setting_special_credit), subtitle = stringResource(R.string.setting_special_credit_desc))

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Komponen Reusable untuk setiap baris pengaturan
@Composable
fun SettingItem(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Aksi saat item diklik */ }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            fontFamily = montserrat
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = Color.Gray,
            fontFamily = montserrat
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingPreview() {
    SettingContent()
}