package com.rousoft.expanse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackContent() {
    // State untuk menyimpan teks yang diketik user
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Label dan Input Email
        Text(
            text = stringResource(R.string.label_email),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = montserrat,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(stringResource(R.string.placeholder_email), color = Color.Gray, fontFamily = montserrat) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF5F5F5), // Abu-abu muda seperti desain
                focusedBorderColor = Color(0xFFFFA500), // Garis jadi oranye saat diklik
                unfocusedBorderColor = Color.LightGray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Label dan Input Pesan (Message)
        Text(
            text = stringResource(R.string.label_message),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = montserrat,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            placeholder = { Text(stringResource(R.string.placeholder_message), color = Color.Gray, fontFamily = montserrat) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), // Dibuat tinggi karena ini kotak pesan (textarea)
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color(0xFFFFA500),
                unfocusedBorderColor = Color.LightGray
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Tombol Send (Kirim)
        Button(
            onClick = { /* TODO: Logika kirim ke server / email */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Send",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_send),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = montserrat
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FeedbackPreview() {
    FeedbackContent()
}