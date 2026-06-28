package com.rousoft.expanse

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.ai.client.generativeai.GenerativeModel
import com.rousoft.expanse.database.syncOfflineDataToFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

// IMPORT DATABASE KAMU
import com.rousoft.expanse.database.AppDatabase
import com.rousoft.expanse.database.Transaction

@Composable
fun ScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraPreviewOverlay(onBack = onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Izin kamera diperlukan untuk fitur Scan.", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Berikan Izin", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CameraPreviewOverlay(onBack: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Coroutine untuk Gemini & Database

    var isFlashOn by remember { mutableStateOf(false) }

    // STATE UNTUK AI & HASIL SCAN
    val imageCapture = remember { ImageCapture.Builder().build() }
    var isProcessing by remember { mutableStateOf(false) }
    var extractedTotal by remember { mutableStateOf("") }
    var extractedCategory by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    // KONFIGURASI GEMINI API
    val geminiApiKey = "secret"
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-3.1-flash-lite", // Menggunakan versi Lite yang cepat & hemat kuota
            apiKey = geminiApiKey
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. TAMPILAN KAMERA ASLI
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = androidx.camera.core.Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (exc: Exception) {
                        Log.e("CameraX", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx) as Executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. OVERLAY TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
            Text("Scan Receipt", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { isFlashOn = !isFlashOn }) {
                Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Flash", tint = Color.White)
            }
        }

        // 3. BINGKAI VIEWFINDER
        Box(
            modifier = Modifier.align(Alignment.Center).width(280.dp).height(400.dp)
                .border(2.dp, Color(0xFFFFA500), RoundedCornerShape(16.dp)).background(Color.Transparent)
        )

        // 4. TOMBOL CAPTURE
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp).size(72.dp)
                .background(if (isProcessing) Color.Gray else Color.White, CircleShape).padding(4.dp)
                .border(3.dp, Color(0xFFFFA500), CircleShape)
                .clickable(enabled = !isProcessing) {
                    isProcessing = true

                    val executor = ContextCompat.getMainExecutor(context)
                    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            processImageWithMLKit(
                                imageProxy = imageProxy,
                                onSuccess = { text ->
                                    // 1. Ambil Total secara Offline
                                    extractedTotal = extractTotalFromReceipt(text)

                                    // 2. Tentukan Kategori dengan Gemini
                                    scope.launch {
                                        try {
                                            val prompt = """
                                                Analisis teks struk belanja berikut. Tentukan HANYA SATU kategori pengeluaran yang paling cocok dari daftar ini: [Transportation, Healthcare, Entertainment, Recurring Payments, Shopping, Other Expenses].
                                                Jika kamu mendeteksi minimarket/supermarket/makanan, masukkan ke 'Shopping'.
                                                Jika tidak yakin, jawab 'Other Expenses'.
                                                Jangan beri penjelasan tambahan, cukup tulis 1 nama kategorinya saja.
                                                Teks struk:
                                                $text
                                            """.trimIndent()

                                            val response = generativeModel.generateContent(prompt)
                                            extractedCategory = response.text?.trim() ?: "Other Expenses"
                                        } catch (e: Exception) {
                                            extractedCategory = "Other Expenses"
                                            Log.e("GeminiAPI", "Gagal memanggil Gemini", e)
                                        } finally {
                                            isProcessing = false
                                            showResultDialog = true
                                        }
                                    }
                                },
                                onError = {
                                    extractedTotal = "-"
                                    extractedCategory = "-"
                                    isProcessing = false
                                    showResultDialog = true
                                }
                            )
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                            isProcessing = false
                        }
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color(0xFFFFA500), modifier = Modifier.size(36.dp))
            }
        }
    }

    // ==========================================
    // POP-UP UI BERSERTIFIKAT (BERSIH) & SIMPAN KE ROOM DB
    // ==========================================
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("Scan Berhasil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Kategori:", fontSize = 14.sp, color = Color.Gray)
                    Text(text = extractedCategory, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Total Pengeluaran:", fontSize = 14.sp, color = Color.Gray)
                    Text(text = extractedTotal, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Gunakan Application Context agar Database tidak error
                        val appContext = context.applicationContext

                        scope.launch {
                            try {
                                // 1. Bersihkan teks "Rp 55.900" menjadi angka murni "55900.0"
                                val cleanTotal = extractedTotal.replace(Regex("[^0-9]"), "")
                                val amount = cleanTotal.toDoubleOrNull() ?: 0.0

                                // Cegah menyimpan jika angkanya 0 (misal karena gagal baca)
                                if (amount <= 0.0) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Gagal disimpan: Nominal 0", Toast.LENGTH_SHORT).show()
                                    }
                                    return@launch
                                }

                                // 2. Buat objek data sesuai struktur tabelmu
                                val newTransaction = Transaction(
                                    title = extractedCategory,
                                    amount = amount,
                                    category = extractedCategory,
                                    date = System.currentTimeMillis(),
                                    type = "Expense",
                                    isSynced = false
                                )

                                // 3. Simpan ke Database
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(appContext)
                                    db.transactionDao().insertTransaction(newTransaction)
                                }

                                // 4. Eksekusi Insert ke Database (Jalur Background/IO)
                                withContext(Dispatchers.IO) {
                                    val db = AppDatabase.getDatabase(appContext)
                                    val dao = db.transactionDao()

                                    // Simpan ke lokal (Room)
                                    dao.insertTransaction(newTransaction)

                                    // Langsung tembak ke Firebase agar real-time!
                                    syncOfflineDataToFirebase(dao)
                                }

                                // 4. Tutup Pop-up & Eksekusi onBack()
                                withContext(Dispatchers.Main) {
                                    showResultDialog = false
                                    Toast.makeText(context, "Berhasil! Rp $amount disimpan", Toast.LENGTH_LONG).show()
                                    onBack() // Memanggil navigasi kembali
                                }

                            } catch (e: Exception) {
                                // JIKA GAGAL, MUNCULKAN ERRORNYA DI LAYAR
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error Database: ${e.message}", Toast.LENGTH_LONG).show()
                                    Log.e("DB_ERROR", "Gagal menyimpan ke Room", e)
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                ) {
                    Text("Simpan Transaksi", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResultDialog = false }) {
                    Text("Batal", color = Color.Black)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ==========================================
// FUNGSI ML KIT (PEMBACA GAMBAR)
// ==========================================
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun processImageWithMLKit(
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText -> onSuccess(visionText.text) }
            .addOnFailureListener { e -> onError(e) }
            .addOnCompleteListener { imageProxy.close() }
    } else {
        imageProxy.close()
    }
}

// ==========================================
// FUNGSI PENYARING LOKAL (LOGIKA MATEMATIKA STRUK)
// ==========================================
fun extractTotalFromReceipt(rawText: String): String {
    val numberRegex = Regex("""\b\d{1,3}(?:[.,]\s?\d{3})+\b|\b\d{4,}\b""")
    val rawNumbers = numberRegex.findAll(rawText)
        .map { it.value.replace(Regex("""[^0-9]"""), "") }
        .mapNotNull { it.toLongOrNull() }
        .filter { it in 1000..99999999 }
        .toList()

    var detectedTotal: Long? = null

    if (rawNumbers.size >= 3) {
        for (i in 0 until rawNumbers.size - 2) {
            val n1 = rawNumbers[i]
            val n2 = rawNumbers[i+1]
            val n3 = rawNumbers[i+2]
            if (n2 - n1 == n3) { detectedTotal = n1; break }
            if (n1 - n2 == n3) { detectedTotal = n2; break }
        }
    }

    if (detectedTotal == null && rawNumbers.size >= 2) {
        for (i in 0 until rawNumbers.size - 1) {
            if (rawNumbers[i] == rawNumbers[i+1]) {
                detectedTotal = rawNumbers[i]; break
            }
        }
    }

    if (detectedTotal == null && rawNumbers.isNotEmpty()) {
        val lines = rawText.lowercase().split("\n")
        val keywords = listOf("total", "jumlah", "tagihan", "bayar", "amount")

        val keywordIndex = lines.indexOfFirst { line -> keywords.any { line.contains(it) } }
        if (keywordIndex != -1) {
            val linesToCheck = lines.subList(keywordIndex, Math.min(keywordIndex + 6, lines.size)).joinToString(" ")
            val fallbackNumbers = numberRegex.findAll(linesToCheck)
                .map { it.value.replace(Regex("""[^0-9]"""), "") }
                .mapNotNull { it.toLongOrNull() }
                .filter { it in 1000..99999999 }
                .toList()

            if (fallbackNumbers.isNotEmpty()) {
                detectedTotal = fallbackNumbers.first()
            }
        }
    }

    return if (detectedTotal != null) {
        try {
            val formatter = java.text.NumberFormat.getInstance(java.util.Locale("id", "ID"))
            "Rp " + formatter.format(detectedTotal)
        } catch (e: Exception) {
            "Rp $detectedTotal"
        }
    } else {
        "Manual Input"
    }
}